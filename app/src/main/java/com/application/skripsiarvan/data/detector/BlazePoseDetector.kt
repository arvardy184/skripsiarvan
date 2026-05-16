package com.application.skripsiarvan.data.detector

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import com.application.skripsiarvan.domain.detector.PoseDetector
import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.Keypoint
import com.application.skripsiarvan.domain.model.Person
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage

/**
 * MediaPipe BlazePose Lite detector implementation.
 *
 * Digunakan sebagai salah satu model dalam benchmarking performa delegate TFLite
 * (sesuai Bab 3 Metodologi — variabel model: BlazePose Lite FP16).
 *
 * Input  : 256×256 RGB image, normalisasi ke [-1, 1]
 * Output : [1, N] tensor — 33 landmarks × stride nilai (x, y, z, visibility, presence)
 *
 * Implementasi:
 * 1. Auto-deteksi rentang koordinat (pixel vs normalized) dengan fallback warning
 * 2. Sigmoid pada visibility logits (BlazePose output logits, bukan probabilitas)
 * 3. Temporal smoothing (EMA) untuk meredam jitter antar-frame
 * 4. Stride output dihitung via integer division (bukan threshold cascade)
 * 5. frameCount terpisah dari rangeDetectionAttempts — logging tidak spam setiap frame
 * 6. Buffer output di-cache di level kelas — tidak dialokasi ulang tiap frame (menghindari GC pressure)
 * 7. Warm-up 5 frame awal (sesuai Bab 4.5.1 Strategi Pengukuran Latensi)
 * 8. ROI tracking manual dari keypoints frame sebelumnya, sesuai arsitektur asli MediaPipe
 *    dua tahap untuk model BlazePose landmark-only.
 *
 * Mapping: 33 BlazePose keypoints → 17 COCO keypoints untuk konsistensi dengan MoveNet.
 */
class BlazePoseDetector(private val interpreter: Interpreter) : PoseDetector {

    companion object {
        private const val TAG = "BlazePoseDetector"
        private const val INPUT_SIZE = 256
        private const val NUM_KEYPOINTS = 17
        private const val MIN_BLAZEPOSE_KEYPOINTS = 33
        private const val MAX_BLAZEPOSE_KEYPOINTS = 50
        private const val LANDMARK_VALUES_PER_KEYPOINT = 5
        private const val BLAZEPOSE_KEYPOINTS = MIN_BLAZEPOSE_KEYPOINTS
        // Dinaikkan 0.15 → 0.25 untuk menekan ghost detection saat tidak ada orang.
        // Noise output model saat frame kosong menghasilkan avgScore ~0.10–0.18;
        // threshold 0.25 memastikan hanya deteksi nyata yang lolos.
        private const val CONFIDENCE_THRESHOLD = 0.25f

        // Threshold global presence tensor (output ke-1 BlazePose).
        // Diturunkan 0.5 → 0.35 agar push-up DOWN phase (presence sering ~0.4) tetap lolos.
        private const val PRESENCE_THRESHOLD = 0.35f
        // Diaktifkan kembali — gate ini adalah filter anti-ghost paling akurat karena
        // langsung dari model, bukan heuristik koordinat.
        private const val ENABLE_GLOBAL_PRESENCE_GATE = true
        private const val MIN_VALID_KEYPOINTS = 5
        private const val MIN_VALID_KEYPOINT_SCORE = 0.2f

        // Warm-up ditangani sepenuhnya oleh PoseImageAnalyzer (WARM_UP_FRAMES = 5) untuk
        // KEDUA model secara simetris. BlazePose tidak punya internal warm-up sendiri agar
        // behavior identik dengan MoveNet — penting untuk fairness perbandingan di skripsi.

        // Alpha EMA — dinaikkan 0.2 → 0.4 agar responsif untuk gerakan dinamis (squat).
        // Di 15fps, alpha=0.2 menghasilkan lag ~5 frame (80% bobot ke frame lama). Untuk
        // squat 3 detik turun, sudut lutut raw sudah ~100° tapi smoothed masih ~135°
        // sehingga threshold 110° tidak pernah tersentuh. Identik dengan MoveNet fix.
        private const val EMA_ALPHA = 0.4f

        // Alpha untuk keypoint low-confidence — tetap smoothing, tapi pelan.
        // Sebelumnya: keypoint <0.4 di-FREEZE ke posisi prev → saat orang squat,
        // visibility hip/knee/ankle sering dip <0.4 dan koordinatnya terkunci di
        // posisi berdiri, sehingga sudut lutut stuck di ~170°. Ini root cause
        // "0 reps kehitung" di BlazePose.
        private const val EMA_ALPHA_LOW = 0.1f

        // Deadband: pergerakan < nilai ini (normalized [0,1]) diabaikan.
        private const val EMA_DEADBAND = 0.008f

        // Threshold "sangat rendah" — HARUS << CONFIDENCE_THRESHOLD (0.3) supaya
        // tidak ada kondisi "person detected tapi keypoint dibekukan".
        private const val EMA_LOW_CONF_THRESHOLD = 0.15f

        // Diturunkan 10 → 4: EMA history di-clear lebih cepat saat orang meninggalkan frame,
        // mencegah keypoints "melayang" selama beberapa detik setelah objek hilang.
        private const val MAX_CONSECUTIVE_FAILURES = 4
        private const val VERBOSE_KEYPOINT_LOG = false

        // Manual ROI tracking untuk landmark-only BlazePose. Model ini dilatih pada crop ketat,
        // sehingga frame penuh hanya dipakai sebagai bootstrap/fallback.
        private const val ROI_KEYPOINT_SCORE_THRESHOLD = 0.15f
        private const val ROI_MIN_VALID_KEYPOINTS = 4
        private const val ROI_PADDING_RATIO = 0.20f
        private const val ROI_MIN_SIDE = 0.55f
        private const val ROI_EMA_ALPHA = 0.6f

        // Pemetaan indeks COCO 17 → indeks BlazePose 33
        // Referensi urutan BlazePose: 0=nose, 1=left_eye_inner, 2=left_eye, 3=left_eye_outer,
        //   4=right_eye_inner, 5=right_eye, 6=right_eye_outer, 7=left_ear, 8=right_ear, ...
        private val COCO_TO_BLAZEPOSE = mapOf(
            0  to 0,  // nose
            1  to 2,  // left_eye  ← BlazePose index 2 (left_eye, bukan inner)
            2  to 5,  // right_eye ← BlazePose index 5 (right_eye, bukan inner)
            3  to 7,  // left_ear
            4  to 8,  // right_ear
            5  to 11, // left_shoulder
            6  to 12, // right_shoulder
            7  to 13, // left_elbow
            8  to 14, // right_elbow
            9  to 15, // left_wrist
            10 to 16, // right_wrist
            11 to 23, // left_hip
            12 to 24, // right_hip
            13 to 25, // left_knee
            14 to 26, // right_knee
            15 to 27, // left_ankle
            16 to 28  // right_ankle
        )
    }

    // Preprocessing: resize ke 256×256, normalisasi ke [-1, 1] untuk model FP16 BlazePose
    private val imageProcessor =
        ImageProcessor.Builder()
            .add(NormalizeOp(127.5f, 127.5f))
            .build()

    private val lock = Any()
    private var isClosed = false

    // ── Buffer output di-cache (dialokasi sekali, dipakai ulang tiap frame) ──────────────────────
    // Menghindari ByteBuffer.allocateDirect() berulang yang menekan GC dan menyebabkan stuttering.
    private var cachedOutputBuffer: java.nio.ByteBuffer? = null
    private var cachedOutputArray: FloatArray? = null
    private var cachedTotalSize: Int = 0
    private var cachedValuesPerKeypoint: Int = 0
    private var landmarkOutputIndex: Int = 0

    // ── Deteksi rentang koordinat (pixel vs normalized) ─────────────────────────────────────────
    private var coordsDivisor: Float = 1.0f
    private var rangeDetected = false
    private var rangeDetectionAttempts = 0

    // ── Counter frame — selalu increment setiap frame, terpisah dari rangeDetectionAttempts ─────
    private var frameCount = 0

    // ── Buffer EMA temporal smoothing ───────────────────────────────────────────────────────────
    private var previousKeypoints: FloatArray? = null
    private var consecutiveFailureCount = 0
    private var rawLogCounter = 0

    // ── Buffer output tensor ke-1 (global pose presence score, opsional) ────────────────────────
    // BlazePose TFLite kadang punya output tensor kedua berisi skor kehadiran pose secara global.
    // Diinisialisasi null; jika model tidak memiliki tensor ke-1, tetap null dan diabaikan.
    private var globalPresenceBuffer: java.nio.ByteBuffer? = null
    private var hasGlobalPresenceTensor = false
    private var globalPresenceOutputIndex: Int = -1
    private var globalPresenceChecked = false
    private var firstDetectionLogged = false
    private var trackedRoi: Roi? = null

    private data class Roi(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    ) {
        val width: Float get() = right - left
        val height: Float get() = bottom - top

        fun clamped(): Roi {
            val l = left.coerceIn(0f, 1f)
            val t = top.coerceIn(0f, 1f)
            val r = right.coerceIn(0f, 1f)
            val b = bottom.coerceIn(0f, 1f)
            return if (r > l && b > t) Roi(l, t, r, b) else FULL
        }

        fun isFullFrame(): Boolean =
            left <= 0f && top <= 0f && right >= 1f && bottom >= 1f

        fun shortString(): String =
            "(%.2f,%.2f)-(%.2f,%.2f)".format(left, top, right, bottom)

        companion object {
            val FULL = Roi(0f, 0f, 1f, 1f)
        }
    }

    private data class CropInput(
        val bitmap: Bitmap,
        val roiInOriginal: Roi,
        val ownsBitmap: Boolean
    )

    private data class LetterboxResult(
        val bitmap: Bitmap,
        val scale: Float,
        val padX: Float,
        val padY: Float,
        val targetW: Float,
        val targetH: Float,
        val srcW: Float,
        val srcH: Float
    )

    private data class InferenceResult(
        val keypoints: List<Keypoint>,
        val rawKeypointValues: FloatArray,
        val avgScore: Float,
        val roiCandidate: Roi?,
        val usedRoi: Roi
    )

    /**
     * Inisialisasi buffer output sekali berdasarkan shape tensor output model.
     * Dipanggil pada frame pertama karena interpreter harus sudah fully initialized.
     */
    private fun ensureBuffersInitializedLegacy() {
        if (cachedOutputBuffer != null) return

        val numOutputs = interpreter.outputTensorCount
        val inputShape  = interpreter.getInputTensor(0).shape()
        val inputType   = interpreter.getInputTensor(0).dataType()
        val outputShape = interpreter.getOutputTensor(0).shape()
        val totalSize   = outputShape.reduce { acc, i -> acc * i }
        val stride      = totalSize / BLAZEPOSE_KEYPOINTS

        // ── Satu kali log saat init — cukup untuk verifikasi model di Logcat ────────────────────
        Log.d(TAG, "── BlazePose Init ──────────────────────────────")
        Log.d(TAG, "  in : ${inputShape.contentToString()} $inputType")
        Log.d(TAG, "  out[0]: ${outputShape.contentToString()} → totalSize=$totalSize, stride=$stride")
        Log.d(TAG, "  numOutputs=$numOutputs | ema=$EMA_ALPHA | thr=$CONFIDENCE_THRESHOLD (warmup handled by PoseImageAnalyzer)")
        Log.d(TAG, "────────────────────────────────────────────────")

        if (stride < 3) {
            Log.e(TAG, "Output tensor tidak valid: stride=$stride (min 3) — model salah?")
            return
        }

        cachedTotalSize = totalSize
        cachedValuesPerKeypoint = stride
        cachedOutputArray = FloatArray(totalSize)
        cachedOutputBuffer = java.nio.ByteBuffer.allocateDirect(totalSize * 4).also {
            it.order(java.nio.ByteOrder.nativeOrder())
        }

        if (!globalPresenceChecked) {
            globalPresenceChecked = true
            try {
                if (numOutputs > 1) {
                    val presenceShape = interpreter.getOutputTensor(1).shape()
                    val presenceSize  = presenceShape.reduce { acc, i -> acc * i }
                    globalPresenceBuffer = java.nio.ByteBuffer.allocateDirect(presenceSize * 4)
                        .also { it.order(java.nio.ByteOrder.nativeOrder()) }
                    hasGlobalPresenceTensor = true
                    Log.d(TAG, "  out[1]: ${presenceShape.contentToString()} → global presence tensor ✓")
                } else {
                    Log.d(TAG, "  out[1]: tidak ada → filter per-keypoint saja")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal cek global presence tensor: ${e.message}")
            }
        }
    }

    private fun ensureBuffersInitialized() {
        if (cachedOutputBuffer != null) return

        val numOutputs = interpreter.outputTensorCount
        val inputShape = interpreter.getInputTensor(0).shape()
        val inputType = interpreter.getInputTensor(0).dataType()
        var landmarkShape: IntArray? = null
        var totalSize = 0

        Log.d(TAG, "BlazePose Init")
        Log.d(TAG, "  in : ${inputShape.contentToString()} $inputType")

        for (i in 0 until numOutputs) {
            val tensor = interpreter.getOutputTensor(i)
            val shape = tensor.shape()
            val size = shape.reduce { acc, value -> acc * value }
            Log.d(TAG, "  out[$i]: ${shape.contentToString()} ${tensor.dataType()} totalSize=$size")

            val keypointCount = size / LANDMARK_VALUES_PER_KEYPOINT
            val looksLikeLandmarks =
                    size % LANDMARK_VALUES_PER_KEYPOINT == 0 &&
                            keypointCount in MIN_BLAZEPOSE_KEYPOINTS..MAX_BLAZEPOSE_KEYPOINTS

            if (looksLikeLandmarks && landmarkShape == null) {
                landmarkOutputIndex = i
                landmarkShape = shape
                totalSize = size
            }

            if (size == 1 && globalPresenceOutputIndex == -1) {
                globalPresenceOutputIndex = i
            }
        }

        if (landmarkShape == null) {
            Log.e(TAG, "Output landmark BlazePose tidak ditemukan. Cek file model .tflite yang dipakai.")
            return
        }

        cachedTotalSize = totalSize
        cachedValuesPerKeypoint = LANDMARK_VALUES_PER_KEYPOINT
        cachedOutputArray = FloatArray(totalSize)
        cachedOutputBuffer = java.nio.ByteBuffer.allocateDirect(totalSize * 4).also {
            it.order(java.nio.ByteOrder.nativeOrder())
        }

        globalPresenceChecked = true
        if (globalPresenceOutputIndex >= 0 && globalPresenceOutputIndex != landmarkOutputIndex) {
            val presenceShape = interpreter.getOutputTensor(globalPresenceOutputIndex).shape()
            val presenceSize = presenceShape.reduce { acc, value -> acc * value }
            globalPresenceBuffer = java.nio.ByteBuffer.allocateDirect(presenceSize * 4).also {
                it.order(java.nio.ByteOrder.nativeOrder())
            }
            hasGlobalPresenceTensor = true
            Log.d(TAG, "  presence out[$globalPresenceOutputIndex]: ${presenceShape.contentToString()} global presence tensor")
        } else {
            Log.d(TAG, "  presence tensor tidak ada, pakai filter per-keypoint saja")
        }

        Log.d(
                TAG,
                "  landmarks out[$landmarkOutputIndex]: ${landmarkShape.contentToString()} " +
                        "totalSize=$totalSize, stride=$LANDMARK_VALUES_PER_KEYPOINT"
        )
    }

    override fun handlesPreprocessingInternally(): Boolean = true

    override fun detectPose(bitmap: Bitmap): Person? {
        synchronized(lock) {
            if (isClosed) return null

            frameCount++ // Selalu increment — dipakai untuk logging dan warm-up

            try {
                // Inisialisasi buffer sekali (lazy init — aman karena di dalam synchronized)
                ensureBuffersInitialized()

                val roiAtFrameStart = trackedRoi
                var inference = runInference(bitmap, roiAtFrameStart)

                if (roiAtFrameStart != null && (inference == null || !isAcceptedDetection(inference))) {
                    trackedRoi = null
                    inference = runInference(bitmap, null)
                    if (frameCount % 30 == 0) {
                        Log.v(TAG, "ROI lost/invalid, fallback ke full-frame inference")
                    }
                }

                if (inference == null) {
                    handleDetectionFailure()
                    return null
                }

                val keypoints = inference.keypoints
                val avgScore = inference.avgScore
                val enoughValidKeypoints = hasEnoughValidKeypoints(keypoints)
                val detected = avgScore >= CONFIDENCE_THRESHOLD && enoughValidKeypoints

                if (frameCount % 30 == 0) {
                    val lShoulder = keypoints[5]
                    val lHip = keypoints[11]
                    val lKnee = keypoints[13]
                    Log.d(
                            TAG,
                            "BlazePose test frame=$frameCount avg=${"%.3f".format(avgScore)} " +
                                    "div=$coordsDivisor stride=$cachedValuesPerKeypoint roi=${inference.usedRoi.shortString()} | " +
                                    "L.sh=(${"%.2f".format(lShoulder.x)},${"%.2f".format(lShoulder.y)}) s=${"%.2f".format(lShoulder.score)} " +
                                    "L.hip=(${"%.2f".format(lHip.x)},${"%.2f".format(lHip.y)}) s=${"%.2f".format(lHip.score)} " +
                                    "L.kn=(${"%.2f".format(lKnee.x)},${"%.2f".format(lKnee.y)}) s=${"%.2f".format(lKnee.score)}"
                    )
                }

                // Log raw keypoint hip/knee/ankle tiap 10 frame — untuk verifikasi
                // model menghasilkan koordinat yang bergerak saat squat (sebelum EMA).
                rawLogCounter++
                if (VERBOSE_KEYPOINT_LOG && rawLogCounter % 10 == 0) {
                    val lh = keypoints[11]; val rh = keypoints[12]
                    val lk = keypoints[13]; val rk = keypoints[14]
                    val la = keypoints[15]; val ra = keypoints[16]
                    Log.d(TAG, ("RAW f=%d avg=%.2f | " +
                        "LHip[y=%.2f s=%.2f] RHip[y=%.2f s=%.2f] | " +
                        "LKnee[y=%.2f s=%.2f] RKnee[y=%.2f s=%.2f] | " +
                        "LAnk[y=%.2f s=%.2f] RAnk[y=%.2f s=%.2f]").format(
                        rawLogCounter, avgScore,
                        lh.y, lh.score, rh.y, rh.score,
                        lk.y, lk.score, rk.y, rk.score,
                        la.y, la.score, ra.y, ra.score))
                }

                // Terapkan EMA temporal smoothing
                val smoothedKeypoints = applyTemporalSmoothing(keypoints, inference.rawKeypointValues)

                if (detected && inference.roiCandidate != null) {
                    updateTrackedRoi(inference.roiCandidate)
                }

                return if (detected) {
                    consecutiveFailureCount = 0
                    if (!firstDetectionLogged) {
                        firstDetectionLogged = true
                        val nose      = smoothedKeypoints[0]
                        val lShoulder = smoothedKeypoints[5]
                        val rShoulder = smoothedKeypoints[6]
                        val lHip      = smoothedKeypoints[11]
                        Log.d(TAG, "✓ First detection: avg=${"%.3f".format(avgScore)} | divisor=$coordsDivisor | stride=$cachedValuesPerKeypoint | roi=${trackedRoi?.shortString() ?: "full"}")
                        Log.d(TAG, "  nose      (${"%5.3f".format(nose.x)}, ${"%5.3f".format(nose.y)}) s=${"%4.2f".format(nose.score)}")
                        Log.d(TAG, "  L.shoulder(${"%5.3f".format(lShoulder.x)}, ${"%5.3f".format(lShoulder.y)}) s=${"%4.2f".format(lShoulder.score)}")
                        Log.d(TAG, "  R.shoulder(${"%5.3f".format(rShoulder.x)}, ${"%5.3f".format(rShoulder.y)}) s=${"%4.2f".format(rShoulder.score)}")
                        Log.d(TAG, "  L.hip     (${"%5.3f".format(lHip.x)}, ${"%5.3f".format(lHip.y)}) s=${"%4.2f".format(lHip.score)}")
                    }
                    Person(keypoints = smoothedKeypoints, score = avgScore)
                } else {
                    handleDetectionFailure()
                    null
                }

            } catch (e: Exception) {
                if (e is IllegalStateException && isClosed) {
                    Log.d(TAG, "Detector sudah ditutup saat inferensi berlangsung — diabaikan")
                } else {
                    try {
                        val shape = interpreter.getOutputTensor(landmarkOutputIndex).shape().contentToString()
                        Log.e(TAG, "Error BlazePose. Shape output: $shape. Pesan: ${e.message}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error saat deteksi BlazePose: ${e.message}", e)
                    }
                }
                return null
            }
        }
    }

    private fun isAcceptedDetection(inference: InferenceResult): Boolean =
        inference.avgScore >= CONFIDENCE_THRESHOLD && hasEnoughValidKeypoints(inference.keypoints)

    private fun runInference(sourceBitmap: Bitmap, requestedRoi: Roi?): InferenceResult? {
        val outputBuf = cachedOutputBuffer ?: return null
        val outputArr = cachedOutputArray ?: return null
        val totalSize = cachedTotalSize
        val valuesPerKeypoint = cachedValuesPerKeypoint
        if (valuesPerKeypoint < 3) return null

        val cropInput = createCropInput(sourceBitmap, requestedRoi ?: Roi.FULL)
        val letterbox = createLetterboxedBitmap(cropInput.bitmap, INPUT_SIZE, INPUT_SIZE)

        try {
            var tensorImage = TensorImage.fromBitmap(letterbox.bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            outputBuf.rewind()
            val outputs = HashMap<Int, Any>()
            outputs[landmarkOutputIndex] = outputBuf

            if (ENABLE_GLOBAL_PRESENCE_GATE && hasGlobalPresenceTensor) {
                val presenceBuffer = globalPresenceBuffer ?: return null
                presenceBuffer.rewind()
                outputs[globalPresenceOutputIndex] = presenceBuffer
            }

            interpreter.runForMultipleInputsOutputs(arrayOf(tensorImage.buffer), outputs)

            outputBuf.rewind()
            outputBuf.asFloatBuffer().get(outputArr)

            if (ENABLE_GLOBAL_PRESENCE_GATE && hasGlobalPresenceTensor) {
                val presenceBuf = globalPresenceBuffer!!
                presenceBuf.rewind()
                val rawGlobalPresence = presenceBuf.asFloatBuffer().get()
                val globalPresence = toProbability(rawGlobalPresence)
                if (globalPresence < PRESENCE_THRESHOLD) {
                    if (frameCount % 30 == 0) {
                        Log.v(TAG, "Global presence terlalu rendah: ${"%.3f".format(globalPresence)} — tidak ada orang")
                    }
                    return null
                }
            }

            if (!rangeDetected) {
                if (rangeDetectionAttempts < 10) {
                    detectCoordinateRange(outputArr, valuesPerKeypoint)
                    rangeDetectionAttempts++
                } else {
                    Log.w(TAG,
                        "⚠️ Tidak dapat mendeteksi rentang koordinat dalam 10 frame. " +
                        "Default divisor=$coordsDivisor. Koordinat mungkin tidak akurat " +
                        "jika model output piksel [0, $INPUT_SIZE].")
                    rangeDetected = true
                }
            }

            val keypoints = mutableListOf<Keypoint>()
            var totalScore = 0f
            val currentRawKeypoints = FloatArray(NUM_KEYPOINTS * 2)

            for (cocoIdx in 0 until NUM_KEYPOINTS) {
                val blazePoseIdx = COCO_TO_BLAZEPOSE[cocoIdx] ?: 0
                val offset = blazePoseIdx * valuesPerKeypoint

                if (offset + valuesPerKeypoint - 1 >= totalSize) {
                    keypoints.add(Keypoint(x = 0f, y = 0f, score = 0f,
                        label = BodyPart.labels[cocoIdx]))
                    continue
                }

                val modelX = (outputArr[offset] / coordsDivisor).coerceIn(0f, 1f)
                val modelY = (outputArr[offset + 1] / coordsDivisor).coerceIn(0f, 1f)
                val cropPoint = unprojectLetterboxPoint(modelX, modelY, letterbox)
                val x = (cropInput.roiInOriginal.left + cropPoint.first * cropInput.roiInOriginal.width)
                    .coerceIn(0f, 1f)
                val y = (cropInput.roiInOriginal.top + cropPoint.second * cropInput.roiInOriginal.height)
                    .coerceIn(0f, 1f)

                val rawVisibility = if (valuesPerKeypoint >= 4) outputArr[offset + 3] else 0.5f
                val rawPresence = if (valuesPerKeypoint >= 5) outputArr[offset + 4] else rawVisibility
                val visibility = toProbability(rawVisibility)
                val presence = toProbability(rawPresence)
                val effectiveScore = minOf(visibility, presence)

                currentRawKeypoints[cocoIdx * 2] = x
                currentRawKeypoints[cocoIdx * 2 + 1] = y

                keypoints.add(Keypoint(x = x, y = y, score = effectiveScore,
                    label = BodyPart.labels[cocoIdx]))
                totalScore += effectiveScore
            }

            return InferenceResult(
                keypoints = keypoints,
                rawKeypointValues = currentRawKeypoints,
                avgScore = totalScore / NUM_KEYPOINTS,
                roiCandidate = computeRoiFromKeypoints(keypoints),
                usedRoi = cropInput.roiInOriginal
            )
        } finally {
            letterbox.bitmap.recycle()
            if (cropInput.ownsBitmap) {
                cropInput.bitmap.recycle()
            }
        }
    }

    private fun createCropInput(source: Bitmap, requestedRoi: Roi): CropInput {
        val roi = requestedRoi.clamped()
        if (roi.isFullFrame()) {
            return CropInput(source, Roi.FULL, ownsBitmap = false)
        }

        val sourceW = source.width
        val sourceH = source.height
        val left = (roi.left * sourceW).toInt().coerceIn(0, sourceW - 1)
        val top = (roi.top * sourceH).toInt().coerceIn(0, sourceH - 1)
        val right = ceil(roi.right * sourceW).toInt().coerceIn(left + 1, sourceW)
        val bottom = ceil(roi.bottom * sourceH).toInt().coerceIn(top + 1, sourceH)
        val crop = Bitmap.createBitmap(source, left, top, right - left, bottom - top)
        val actualRoi = Roi(
            left = left / sourceW.toFloat(),
            top = top / sourceH.toFloat(),
            right = right / sourceW.toFloat(),
            bottom = bottom / sourceH.toFloat()
        )

        return CropInput(crop, actualRoi, ownsBitmap = true)
    }

    private fun createLetterboxedBitmap(
        source: Bitmap,
        targetW: Int,
        targetH: Int
    ): LetterboxResult {
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val scale = min(targetW / srcW, targetH / srcH)
        val scaledW = (srcW * scale).toInt()
        val scaledH = (srcH * scale).toInt()
        val padX = (targetW - scaledW) / 2f
        val padY = (targetH - scaledH) / 2f

        val letterboxed = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(letterboxed)
        canvas.drawColor(Color.BLACK)

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(padX, padY)
        canvas.drawBitmap(source, matrix, null)

        return LetterboxResult(
            bitmap = letterboxed,
            scale = scale,
            padX = padX,
            padY = padY,
            targetW = targetW.toFloat(),
            targetH = targetH.toFloat(),
            srcW = srcW,
            srcH = srcH
        )
    }

    private fun unprojectLetterboxPoint(
        x: Float,
        y: Float,
        letterbox: LetterboxResult
    ): Pair<Float, Float> {
        val pixelX = x * letterbox.targetW
        val pixelY = y * letterbox.targetH
        val scaledW = letterbox.srcW * letterbox.scale
        val scaledH = letterbox.srcH * letterbox.scale
        val cropX = (pixelX - letterbox.padX) / scaledW
        val cropY = (pixelY - letterbox.padY) / scaledH
        return Pair(cropX.coerceIn(0f, 1f), cropY.coerceIn(0f, 1f))
    }

    private fun computeRoiFromKeypoints(keypoints: List<Keypoint>): Roi? {
        var minX = 1f
        var minY = 1f
        var maxX = 0f
        var maxY = 0f
        var validCount = 0

        for (keypoint in keypoints) {
            if (keypoint.score <= ROI_KEYPOINT_SCORE_THRESHOLD) continue
            minX = minOf(minX, keypoint.x)
            minY = minOf(minY, keypoint.y)
            maxX = maxOf(maxX, keypoint.x)
            maxY = maxOf(maxY, keypoint.y)
            validCount++
        }

        if (validCount < ROI_MIN_VALID_KEYPOINTS) return null

        val bboxW = maxX - minX
        val bboxH = maxY - minY
        if (bboxW <= 0f || bboxH <= 0f) return null

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f

        // BlazePose landmark model expects a square-ish tight person crop. A raw keypoint bbox
        // can collapse to torso/arms during push-up, so keep a minimum square side.
        val paddedSide = max(bboxW, bboxH) * (1f + 2f * ROI_PADDING_RATIO)
        val side = max(paddedSide, ROI_MIN_SIDE).coerceAtMost(1f)
        return squareRoi(centerX, centerY, side)
    }

    private fun squareRoi(centerX: Float, centerY: Float, side: Float): Roi {
        val half = side / 2f
        var left = centerX - half
        var top = centerY - half
        var right = centerX + half
        var bottom = centerY + half

        if (left < 0f) {
            right -= left
            left = 0f
        }
        if (right > 1f) {
            left -= right - 1f
            right = 1f
        }
        if (top < 0f) {
            bottom -= top
            top = 0f
        }
        if (bottom > 1f) {
            top -= bottom - 1f
            bottom = 1f
        }

        return Roi(left, top, right, bottom).clamped()
    }

    private fun updateTrackedRoi(candidate: Roi) {
        val previous = trackedRoi
        trackedRoi =
            if (previous == null) {
                candidate.clamped()
            } else {
                Roi(
                    left = ROI_EMA_ALPHA * candidate.left + (1 - ROI_EMA_ALPHA) * previous.left,
                    top = ROI_EMA_ALPHA * candidate.top + (1 - ROI_EMA_ALPHA) * previous.top,
                    right = ROI_EMA_ALPHA * candidate.right + (1 - ROI_EMA_ALPHA) * previous.right,
                    bottom = ROI_EMA_ALPHA * candidate.bottom + (1 - ROI_EMA_ALPHA) * previous.bottom
                ).clamped()
            }
    }

    /**
     * Auto-deteksi apakah koordinat dalam rentang piksel [0, INPUT_SIZE] atau normalized [0, 1].
     *
     * Heuristik: sampel beberapa keypoint kunci. Jika mayoritas nilai > 1.5 → koordinat piksel,
     * perlu dibagi INPUT_SIZE. Jika dalam [-0.5, 1.5] → sudah normalized, divisor tetap 1.0.
     */
    private fun detectCoordinateRange(values: FloatArray, stride: Int) {
        var pixelRangeCount = 0
        var normalizedCount = 0
        // Sampel: nose, shoulder kiri-kanan, hip kiri-kanan, knee kiri-kanan
        val sampleIndices = listOf(0, 11, 12, 23, 24, 25, 26)

        for (bpIdx in sampleIndices) {
            val offset = bpIdx * stride
            if (offset + 1 >= values.size) continue

            val x = values[offset]
            val y = values[offset + 1]

            if (x.isNaN() || y.isNaN()) continue

            if (x > 1.5f || y > 1.5f) {
                pixelRangeCount++
            } else if (x in -0.5f..1.5f && y in -0.5f..1.5f) {
                normalizedCount++
            }
        }

        // Butuh minimal 3 sampel valid untuk konsensus
        if (pixelRangeCount + normalizedCount >= 3) {
            coordsDivisor = if (pixelRangeCount > normalizedCount) {
                Log.d(TAG, "✅ Koordinat PIKSEL [0, $INPUT_SIZE] terdeteksi — divisor=$INPUT_SIZE " +
                        "(pixel=$pixelRangeCount, norm=$normalizedCount)")
                INPUT_SIZE.toFloat()
            } else {
                Log.d(TAG, "✅ Koordinat NORMALIZED [0, 1] terdeteksi — divisor=1.0 " +
                        "(pixel=$pixelRangeCount, norm=$normalizedCount)")
                1.0f
            }
            rangeDetected = true
        } else {
            Log.v(TAG, "Deteksi rentang percobaan ke-${rangeDetectionAttempts + 1}: " +
                    "pixel=$pixelRangeCount, norm=$normalizedCount — konsensus belum cukup")
        }
    }

    /**
     * Terapkan EMA (Exponential Moving Average) smoothing untuk meredam jitter keypoint antar-frame.
     *
     * Formula: smoothed = α × current + (1 - α) × previous
     *
     * Penanganan khusus:
     * - Confidence < 0.2 (keypoint tidak terlihat): gunakan posisi frame sebelumnya seluruhnya.
     *   Data noise lebih buruk dari data stale.
     * - Frame pertama: tidak ada data sebelumnya, langsung kembalikan keypoints mentah.
     */
    private fun applyTemporalSmoothing(
        rawKeypoints: List<Keypoint>,
        currentRawValues: FloatArray
    ): List<Keypoint> {
        val prevKps = previousKeypoints
        if (prevKps == null) {
            previousKeypoints = currentRawValues.copyOf()
            return rawKeypoints
        }

        val smoothedKeypoints = mutableListOf<Keypoint>()
        val smoothedValues = FloatArray(currentRawValues.size)

        for (i in rawKeypoints.indices) {
            val idx = i * 2
            if (idx + 1 >= currentRawValues.size || idx + 1 >= prevKps.size) {
                smoothedKeypoints.add(rawKeypoints[i])
                continue
            }

            val kp = rawKeypoints[i]
            // Adaptive alpha: keypoint low-conf tetap ikut bergerak (alpha kecil),
            // tidak lagi di-freeze. Freeze lama menyebabkan 0 reps di squat karena
            // lower-body keypoint sering dip di bawah threshold saat foreshortened.
            val alpha = if (kp.score < EMA_LOW_CONF_THRESHOLD) EMA_ALPHA_LOW else EMA_ALPHA
            val candidateX = alpha * currentRawValues[idx]     + (1 - alpha) * prevKps[idx]
            val candidateY = alpha * currentRawValues[idx + 1] + (1 - alpha) * prevKps[idx + 1]

            val dx = kotlin.math.abs(candidateX - prevKps[idx])
            val dy = kotlin.math.abs(candidateY - prevKps[idx + 1])
            val sx = if (dx > EMA_DEADBAND) candidateX else prevKps[idx]
            val sy = if (dy > EMA_DEADBAND) candidateY else prevKps[idx + 1]

            smoothedValues[idx]     = sx
            smoothedValues[idx + 1] = sy
            smoothedKeypoints.add(kp.copy(x = sx, y = sy))
        }

        previousKeypoints = smoothedValues
        return smoothedKeypoints
    }

    /**
     * Tangani kegagalan deteksi. Tidak langsung me-null-kan `previousKeypoints` karena
     * dip sesaat di avgScore saat fase bawah squat akan menyebabkan angle melompat
     * setiap kembali terdeteksi. Baru clear setelah N frame berturut-turut gagal.
     */
    private fun handleDetectionFailure() {
        consecutiveFailureCount++
        if (consecutiveFailureCount > MAX_CONSECUTIVE_FAILURES) {
            previousKeypoints = null
            trackedRoi = null
        }
    }

    /**
     * Sigmoid: konversi logit → probabilitas [0, 1].
     * BlazePose mengeluarkan visibility/presence sebagai logit, bukan probabilitas langsung.
     */
    private fun toProbability(value: Float): Float =
        if (value in 0f..1f) value else sigmoid(value)

    private fun sigmoid(x: Float): Float = 1.0f / (1.0f + exp(-x))

    private fun hasEnoughValidKeypoints(keypoints: List<Keypoint>): Boolean {
        fun score(idx: Int) = (keypoints.getOrNull(idx)?.score ?: 0f) >= MIN_VALID_KEYPOINT_SCORE

        // Kuorum atas: shoulder + elbow + hip — cukup untuk push-up / plank (badan horizontal,
        // knee & ankle sering invisible karena dekat lantai atau di luar frame).
        val upperValid = listOf(
            BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER,
            BodyPart.LEFT_ELBOW,    BodyPart.RIGHT_ELBOW,
            BodyPart.LEFT_HIP,      BodyPart.RIGHT_HIP
        ).count { score(it) }
        if (upperValid >= 4) return true

        // Kuorum bawah: hip + knee + ankle — cukup untuk squat / berdiri.
        val lowerValid = listOf(
            BodyPart.LEFT_HIP,   BodyPart.RIGHT_HIP,
            BodyPart.LEFT_KNEE,  BodyPart.RIGHT_KNEE,
            BodyPart.LEFT_ANKLE, BodyPart.RIGHT_ANKLE
        ).count { score(it) }
        if (lowerValid >= 3) return true

        if (frameCount % 30 == 0)
            Log.v(TAG, "Pose ditolak: upper=$upperValid/4 lower=$lowerValid/3")
        return false
    }

    override fun getInputSize(): Pair<Int, Int> = Pair(INPUT_SIZE, INPUT_SIZE)

    override fun isClosed(): Boolean = synchronized(lock) { isClosed }

    override fun close() {
        synchronized(lock) {
            isClosed = true
            previousKeypoints = null
            trackedRoi = null
        }
    }
}
