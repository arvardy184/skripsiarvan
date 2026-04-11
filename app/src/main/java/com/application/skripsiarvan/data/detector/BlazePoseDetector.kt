package com.application.skripsiarvan.data.detector

import android.graphics.Bitmap
import android.util.Log
import com.application.skripsiarvan.domain.detector.PoseDetector
import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.Keypoint
import com.application.skripsiarvan.domain.model.Person
import kotlin.math.exp
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

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
 *
 * Mapping: 33 BlazePose keypoints → 17 COCO keypoints untuk konsistensi dengan MoveNet.
 */
class BlazePoseDetector(private val interpreter: Interpreter) : PoseDetector {

    companion object {
        private const val TAG = "BlazePoseDetector"
        private const val INPUT_SIZE = 256
        private const val NUM_KEYPOINTS = 17
        private const val BLAZEPOSE_KEYPOINTS = 33
        // Threshold sama dengan MoveNet (0.3f) agar perbandingan fair.
        // Anti-ghost tetap terjaga via global presence gate (tensor ke-1) yang merupakan
        // metadata native model BlazePose — bukan filter artifisial.
        private const val CONFIDENCE_THRESHOLD = 0.3f

        // Minimal skor presence per-keypoint agar dianggap benar-benar ada di frame.
        // sigmoid(0) = 0.5, jadi threshold ini menyaring output noise saat tidak ada orang.
        private const val PRESENCE_THRESHOLD = 0.5f

        // Warm-up ditangani sepenuhnya oleh PoseImageAnalyzer (WARM_UP_FRAMES = 5) untuk
        // KEDUA model secara simetris. BlazePose tidak punya internal warm-up sendiri agar
        // behavior identik dengan MoveNet — penting untuk fairness perbandingan di skripsi.

        // Alpha EMA temporal smoothing — IDENTIK dengan MoveNet (0.2f) untuk perbandingan fair.
        // Nilai ini dikontrol sama di kedua model (Bab 3 Metodologi: post-processing pipeline identik).
        private const val EMA_ALPHA = 0.2f

        // Deadband: pergerakan < nilai ini (normalized [0,1]) diabaikan — tidak update posisi.
        // 0.008 ≈ <1% lebar layar: menekan micro-jitter yang tidak berarti secara visual.
        private const val EMA_DEADBAND = 0.008f

        // Keypoint dengan score di bawah ini dianggap "tidak terdeteksi dengan baik".
        private const val EMA_LOW_CONF_THRESHOLD = 0.4f

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
            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
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

    // ── Deteksi rentang koordinat (pixel vs normalized) ─────────────────────────────────────────
    private var coordsDivisor: Float = 1.0f
    private var rangeDetected = false
    private var rangeDetectionAttempts = 0

    // ── Counter frame — selalu increment setiap frame, terpisah dari rangeDetectionAttempts ─────
    private var frameCount = 0

    // ── Buffer EMA temporal smoothing ───────────────────────────────────────────────────────────
    private var previousKeypoints: FloatArray? = null

    // ── Buffer output tensor ke-1 (global pose presence score, opsional) ────────────────────────
    // BlazePose TFLite kadang punya output tensor kedua berisi skor kehadiran pose secara global.
    // Diinisialisasi null; jika model tidak memiliki tensor ke-1, tetap null dan diabaikan.
    private var globalPresenceBuffer: java.nio.ByteBuffer? = null
    private var hasGlobalPresenceTensor = false
    private var globalPresenceChecked = false
    private var firstDetectionLogged = false

    /**
     * Inisialisasi buffer output sekali berdasarkan shape tensor output model.
     * Dipanggil pada frame pertama karena interpreter harus sudah fully initialized.
     */
    private fun ensureBuffersInitialized() {
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

    override fun detectPose(bitmap: Bitmap): Person? {
        synchronized(lock) {
            if (isClosed) return null

            frameCount++ // Selalu increment — dipakai untuk logging dan warm-up

            try {
                // Inisialisasi buffer sekali (lazy init — aman karena di dalam synchronized)
                ensureBuffersInitialized()

                val outputBuf = cachedOutputBuffer ?: return null
                val outputArr = cachedOutputArray ?: return null
                val totalSize = cachedTotalSize
                val valuesPerKeypoint = cachedValuesPerKeypoint

                // Preprocessing citra
                var tensorImage = TensorImage.fromBitmap(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // Jalankan inferensi (latensi diukur di PoseImageAnalyzer, bukan di sini)
                outputBuf.rewind()
                if (hasGlobalPresenceTensor) {
                    // Jalankan dengan multi-output agar tensor ke-1 juga terisi
                    val outputs = mapOf(0 to outputBuf, 1 to globalPresenceBuffer!!)
                    interpreter.runForMultipleInputsOutputs(arrayOf(tensorImage.buffer), outputs)
                } else {
                    interpreter.run(tensorImage.buffer, outputBuf)
                }

                // Baca nilai output
                outputBuf.rewind()
                outputBuf.asFloatBuffer().get(outputArr)

                // ── Gate 1: Global pose presence (jika tensor ke-1 tersedia) ─────────────────
                // Ini filter paling awal — jika model sendiri bilang "tidak ada orang",
                // langsung return null tanpa parsing keypoints sama sekali.
                if (hasGlobalPresenceTensor) {
                    val presenceBuf = globalPresenceBuffer!!
                    presenceBuf.rewind()
                    val globalPresenceLogit = presenceBuf.asFloatBuffer().get()
                    val globalPresence = sigmoid(globalPresenceLogit)
                    if (globalPresence < PRESENCE_THRESHOLD) {
                        if (frameCount % 30 == 0) {
                            Log.v(TAG, "Global presence terlalu rendah: ${"%.3f".format(globalPresence)} — tidak ada orang")
                        }
                        resetTemporalSmoothing()
                        return null
                    }
                }

                // Auto-deteksi rentang koordinat pada frame awal
                if (!rangeDetected) {
                    if (rangeDetectionAttempts < 10) {
                        detectCoordinateRange(outputArr, valuesPerKeypoint)
                        rangeDetectionAttempts++
                    } else {
                        // Tidak dapat menentukan rentang dalam 10 frame — pakai default (1.0f)
                        Log.w(TAG,
                            "⚠️ Tidak dapat mendeteksi rentang koordinat dalam 10 frame. " +
                            "Default divisor=$coordsDivisor. Koordinat mungkin tidak akurat " +
                            "jika model output piksel [0, $INPUT_SIZE].")
                        rangeDetected = true
                    }
                }

                // Parse keypoints
                val keypoints = mutableListOf<Keypoint>()
                var totalScore = 0f
                val currentRawKeypoints = FloatArray(NUM_KEYPOINTS * 2)

                for (cocoIdx in 0 until NUM_KEYPOINTS) {
                    val blazePoseIdx = COCO_TO_BLAZEPOSE[cocoIdx] ?: 0
                    val offset = blazePoseIdx * valuesPerKeypoint

                    if (offset + valuesPerKeypoint - 1 >= totalSize) {
                        // Indeks di luar batas — tambah dummy keypoint agar list tetap 17 elemen
                        keypoints.add(Keypoint(x = 0f, y = 0f, score = 0f,
                            label = BodyPart.labels[cocoIdx]))
                        continue
                    }

                    // Ambil koordinat mentah dan normalisasi ke [0, 1]
                    val x = (outputArr[offset]     / coordsDivisor).coerceIn(0f, 1f)
                    val y = (outputArr[offset + 1] / coordsDivisor).coerceIn(0f, 1f)

                    // Visibility & Presence: keduanya logit dari BlazePose, perlu sigmoid.
                    // - visibility (index 3): apakah keypoint terlihat (tidak terhalang)
                    // - presence  (index 4): apakah keypoint benar-benar ada di frame
                    // Pakai min keduanya → skor efektif lebih ketat, menekan false positive.
                    val rawVisibility = if (valuesPerKeypoint >= 4) outputArr[offset + 3] else 0.5f
                    val rawPresence   = if (valuesPerKeypoint >= 5) outputArr[offset + 4] else rawVisibility
                    val visibility    = sigmoid(rawVisibility)
                    val presence      = sigmoid(rawPresence)
                    val effectiveScore = minOf(visibility, presence)

                    currentRawKeypoints[cocoIdx * 2]     = x
                    currentRawKeypoints[cocoIdx * 2 + 1] = y

                    keypoints.add(Keypoint(x = x, y = y, score = effectiveScore,
                        label = BodyPart.labels[cocoIdx]))
                    totalScore += effectiveScore
                }

                val avgScore = totalScore / NUM_KEYPOINTS

                // Terapkan EMA temporal smoothing
                val smoothedKeypoints = applyTemporalSmoothing(keypoints, currentRawKeypoints)

                return if (avgScore >= CONFIDENCE_THRESHOLD) {
                    if (!firstDetectionLogged) {
                        firstDetectionLogged = true
                        val nose      = smoothedKeypoints[0]
                        val lShoulder = smoothedKeypoints[5]
                        val rShoulder = smoothedKeypoints[6]
                        val lHip      = smoothedKeypoints[11]
                        Log.d(TAG, "✓ First detection: avg=${"%.3f".format(avgScore)} | divisor=$coordsDivisor | stride=$valuesPerKeypoint")
                        Log.d(TAG, "  nose      (${"%5.3f".format(nose.x)}, ${"%5.3f".format(nose.y)}) s=${"%4.2f".format(nose.score)}")
                        Log.d(TAG, "  L.shoulder(${"%5.3f".format(lShoulder.x)}, ${"%5.3f".format(lShoulder.y)}) s=${"%4.2f".format(lShoulder.score)}")
                        Log.d(TAG, "  R.shoulder(${"%5.3f".format(rShoulder.x)}, ${"%5.3f".format(rShoulder.y)}) s=${"%4.2f".format(rShoulder.score)}")
                        Log.d(TAG, "  L.hip     (${"%5.3f".format(lHip.x)}, ${"%5.3f".format(lHip.y)}) s=${"%4.2f".format(lHip.score)}")
                    }
                    Person(keypoints = smoothedKeypoints, score = avgScore)
                } else {
                    resetTemporalSmoothing()
                    null
                }

            } catch (e: Exception) {
                if (e is IllegalStateException && isClosed) {
                    Log.d(TAG, "Detector sudah ditutup saat inferensi berlangsung — diabaikan")
                } else {
                    try {
                        val shape = interpreter.getOutputTensor(0).shape().contentToString()
                        Log.e(TAG, "Error BlazePose. Shape output: $shape. Pesan: ${e.message}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error saat deteksi BlazePose: ${e.message}", e)
                    }
                }
                return null
            }
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
            if (kp.score < EMA_LOW_CONF_THRESHOLD) {
                // Keypoint confidence rendah → pakai posisi PREVIOUS yang stabil.
                // Menghindari jitter dari output model yang noisy untuk keypoint yang
                // setengah terlihat. Ghost tidak jadi masalah karena global presence gate
                // akan reset semua EMA saat orang benar-benar pergi dari frame.
                val sx = prevKps[idx]
                val sy = prevKps[idx + 1]
                smoothedValues[idx]     = sx
                smoothedValues[idx + 1] = sy
                smoothedKeypoints.add(kp.copy(x = sx, y = sy))
            } else {
                // EMA normal untuk keypoint yang terdeteksi dengan baik
                val candidateX = EMA_ALPHA * currentRawValues[idx]     + (1 - EMA_ALPHA) * prevKps[idx]
                val candidateY = EMA_ALPHA * currentRawValues[idx + 1] + (1 - EMA_ALPHA) * prevKps[idx + 1]

                // Deadband: jika pergerakan dari posisi sebelumnya sangat kecil (<0.8% layar),
                // pertahankan posisi lama — menghilangkan micro-jitter saat orang diam.
                val dx = kotlin.math.abs(candidateX - prevKps[idx])
                val dy = kotlin.math.abs(candidateY - prevKps[idx + 1])
                val sx = if (dx > EMA_DEADBAND) candidateX else prevKps[idx]
                val sy = if (dy > EMA_DEADBAND) candidateY else prevKps[idx + 1]

                smoothedValues[idx]     = sx
                smoothedValues[idx + 1] = sy
                smoothedKeypoints.add(kp.copy(x = sx, y = sy))
            }
        }

        previousKeypoints = smoothedValues
        return smoothedKeypoints
    }

    /**
     * Reset buffer EMA agar posisi keypoint orang yang sudah pergi tidak "hantu"
     * ke frame berikutnya. Dipanggil saat pose tidak terdeteksi.
     */
    private fun resetTemporalSmoothing() {
        previousKeypoints = null
    }

    /**
     * Sigmoid: konversi logit → probabilitas [0, 1].
     * BlazePose mengeluarkan visibility/presence sebagai logit, bukan probabilitas langsung.
     */
    private fun sigmoid(x: Float): Float = 1.0f / (1.0f + exp(-x))

    override fun getInputSize(): Pair<Int, Int> = Pair(INPUT_SIZE, INPUT_SIZE)

    override fun isClosed(): Boolean = synchronized(lock) { isClosed }

    override fun close() {
        synchronized(lock) {
            isClosed = true
            previousKeypoints = null
            // Buffer output tidak di-null karena ByteBuffer native memory akan dibebaskan
            // oleh GC setelah referensi hilang bersama instance ini
        }
    }
}
