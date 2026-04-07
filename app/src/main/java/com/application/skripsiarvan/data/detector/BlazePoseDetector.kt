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
        // Threshold dinaikkan dari 0.5 ke 0.65 untuk menekan false positive.
        // sigmoid(0) = 0.5 → nilai 0.5 sama saja tidak menyaring noise sama sekali.
        // sigmoid^-1(0.65) ≈ 0.62 logit, jadi model harus cukup yakin ada orang.
        private const val CONFIDENCE_THRESHOLD = 0.65f

        // Minimal skor presence per-keypoint agar dianggap benar-benar ada di frame.
        private const val PRESENCE_THRESHOLD = 0.65f

        // Minimal jumlah keypoint yang harus memiliki score >= MIN_KEYPOINT_SCORE
        // agar pose dianggap valid. Mencegah deteksi jika hanya beberapa titik terlihat.
        private const val MIN_VALID_KEYPOINTS = 8
        private const val MIN_KEYPOINT_SCORE = 0.35f

        // Warm-up frames — sesuai pseudocode di Bab 4.5.1 skripsi:
        // "IF frameCount < 5 THEN PerformWarmUp()" untuk menghindari outlier inisialisasi cache
        private const val WARMUP_FRAMES = 5

        // Alpha EMA temporal smoothing.
        // Dinaikkan dari 0.4 ke 0.6 agar lebih responsif — kurangi lag saat pose bergerak cepat.
        private const val EMA_ALPHA = 0.6f

        // Pemetaan indeks COCO 17 → indeks BlazePose 33
        private val COCO_TO_BLAZEPOSE = mapOf(
            0  to 0,  // nose
            1  to 2,  // left_eye  ← left_eye_inner
            2  to 5,  // right_eye ← right_eye_inner
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

    /**
     * Inisialisasi buffer output sekali berdasarkan shape tensor output model.
     * Dipanggil pada frame pertama karena interpreter harus sudah fully initialized.
     *
     * Sekaligus melakukan validasi model untuk memastikan file .tflite yang dimuat
     * benar-benar BlazePose Lite dengan spesifikasi yang diharapkan.
     */
    private fun ensureBuffersInitialized() {
        if (cachedOutputBuffer != null) return

        // ── Validasi model ────────────────────────────────────────────────────────────────────────
        validateModel()

        val outputShape = interpreter.getOutputTensor(0).shape()
        val totalSize = outputShape.reduce { acc, i -> acc * i }
        val stride = totalSize / BLAZEPOSE_KEYPOINTS

        if (stride < 3) {
            Log.e(TAG, "⛔ Output tensor tidak valid: totalSize=$totalSize, stride=$stride (min 3). " +
                "Pastikan file blazepose_lite_fp16.tflite yang dipakai benar.")
            return
        }

        cachedTotalSize = totalSize
        cachedValuesPerKeypoint = stride
        cachedOutputArray = FloatArray(totalSize)
        cachedOutputBuffer = java.nio.ByteBuffer.allocateDirect(totalSize * 4).also {
            it.order(java.nio.ByteOrder.nativeOrder())
        }

        // Cek apakah model memiliki output tensor ke-1 (global pose presence)
        if (!globalPresenceChecked) {
            globalPresenceChecked = true
            try {
                val numOutputs = interpreter.outputTensorCount
                if (numOutputs > 1) {
                    val presenceShape = interpreter.getOutputTensor(1).shape()
                    val presenceSize = presenceShape.reduce { acc, i -> acc * i }
                    globalPresenceBuffer = java.nio.ByteBuffer.allocateDirect(presenceSize * 4)
                        .also { it.order(java.nio.ByteOrder.nativeOrder()) }
                    hasGlobalPresenceTensor = true
                    Log.d(TAG, "  Global presence tensor: shape=${presenceShape.contentToString()} ✓")
                } else {
                    Log.d(TAG, "  Global presence tensor: tidak ada — filter per-keypoint saja")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal cek global presence tensor: ${e.message}")
            }
        }

        Log.d(TAG, "Buffer output diinisialisasi: totalSize=$totalSize, stride=$stride " +
                "(${BLAZEPOSE_KEYPOINTS} kp × $stride values/kp)")
    }

    /**
     * Validasi bahwa model yang dimuat sesuai spesifikasi BlazePose Lite.
     *
     * Spesifikasi yang diharapkan:
     *   Input  : [1, 256, 256, 3] FLOAT32, normalisasi [-1, 1]
     *   Output : tensor dengan totalSize = 33 × N  (N = 3..6 tergantung varian)
     *            Varian umum: N=5 → [x, y, z, visibility, presence]
     *
     * Log yang dihasilkan mudah dibaca di Logcat dengan tag "BlazePoseDetector".
     * Filter: adb logcat -s BlazePoseDetector
     */
    private fun validateModel() {
        val sep = "─".repeat(60)
        Log.i(TAG, sep)
        Log.i(TAG, "  VALIDASI MODEL: BlazePose Lite FP16")
        Log.i(TAG, sep)

        // ── Input tensor ─────────────────────────────────────────────────────
        try {
            val inputShape = interpreter.getInputTensor(0).shape()
            val inputType  = interpreter.getInputTensor(0).dataType()
            val inputOk    = inputShape.contentEquals(intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 3))
            Log.i(TAG, "  Input  : shape=${inputShape.contentToString()}, type=$inputType")
            if (inputOk) {
                Log.i(TAG, "  Input  : ✓ sesuai [1, $INPUT_SIZE, $INPUT_SIZE, 3] FLOAT32")
            } else {
                Log.w(TAG, "  Input  : ⚠️ TIDAK SESUAI — expected [1, $INPUT_SIZE, $INPUT_SIZE, 3]. " +
                    "Apakah ini benar-benar BlazePose Lite 256×256?")
            }
        } catch (e: Exception) {
            Log.e(TAG, "  Input  : ⛔ Gagal baca tensor input: ${e.message}")
        }

        // ── Output tensor(s) ─────────────────────────────────────────────────
        try {
            val numOutputs = interpreter.outputTensorCount
            Log.i(TAG, "  Output : jumlah tensor = $numOutputs")

            val outShape   = interpreter.getOutputTensor(0).shape()
            val outType    = interpreter.getOutputTensor(0).dataType()
            val totalSize  = outShape.reduce { acc, i -> acc * i }
            val stride     = totalSize / BLAZEPOSE_KEYPOINTS
            val remainder  = totalSize % BLAZEPOSE_KEYPOINTS

            Log.i(TAG, "  Output0: shape=${outShape.contentToString()}, type=$outType")
            Log.i(TAG, "  Output0: totalSize=$totalSize, stride=$stride (33 kp × $stride values), sisa=$remainder")

            when {
                remainder != 0 ->
                    Log.e(TAG, "  Output0: ⛔ totalSize=$totalSize tidak habis dibagi 33. " +
                        "Ini bukan BlazePose 33-keypoint. Periksa file model!")
                stride < 3 ->
                    Log.e(TAG, "  Output0: ⛔ stride=$stride terlalu kecil (min 3 = x,y,z). " +
                        "Model mungkin salah atau korup.")
                stride == 3 ->
                    Log.w(TAG, "  Output0: ⚠️ stride=3 → hanya [x,y,z], tanpa visibility/presence. " +
                        "Threshold confidence akan kurang akurat.")
                stride == 4 ->
                    Log.i(TAG, "  Output0: ✓ stride=4 → [x, y, z, visibility]")
                stride == 5 ->
                    Log.i(TAG, "  Output0: ✓ stride=5 → [x, y, z, visibility, presence] (ideal)")
                stride == 6 ->
                    Log.i(TAG, "  Output0: ✓ stride=6 → [x, y, z, visibility, presence, + extra]")
                else ->
                    Log.w(TAG, "  Output0: ⚠️ stride=$stride tidak dikenal — perlu investigasi")
            }
        } catch (e: Exception) {
            Log.e(TAG, "  Output : ⛔ Gagal baca tensor output: ${e.message}")
        }

        Log.i(TAG, sep)
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

                // Hitung jumlah keypoint yang cukup yakin (score >= MIN_KEYPOINT_SCORE)
                val validKeypointCount = keypoints.count { it.score >= MIN_KEYPOINT_SCORE }

                // Terapkan EMA temporal smoothing
                val smoothedKeypoints = applyTemporalSmoothing(keypoints, currentRawKeypoints)

                // Debug logging — sparse: 5 frame pertama (warm-up) + setiap 200 frame
                if (frameCount <= WARMUP_FRAMES || frameCount % 200 == 0) {
                    val nose = smoothedKeypoints.firstOrNull()
                    Log.d(TAG,
                        "Frame $frameCount [${bitmap.width}×${bitmap.height}]: " +
                        "nose=(${nose?.x?.let { "%.3f".format(it) }}, " +
                               "${nose?.y?.let { "%.3f".format(it) }}), " +
                        "vis=${"%.3f".format(nose?.score ?: 0f)}, " +
                        "avg=${"%.3f".format(avgScore)}, " +
                        "validKps=$validKeypointCount/$NUM_KEYPOINTS, " +
                        "divisor=$coordsDivisor, stride=$valuesPerKeypoint"
                    )
                }

                // Warm-up: 5 frame awal tidak digunakan untuk deteksi
                // (sesuai Bab 4.5.1 — menghindari outlier akibat inisialisasi cache GPU)
                if (frameCount <= WARMUP_FRAMES) return null

                // Gate: rata-rata score harus >= CONFIDENCE_THRESHOLD DAN
                // minimal MIN_VALID_KEYPOINTS keypoint harus terlihat dengan baik.
                // Ini mencegah false positive saat hanya sebagian kecil keypoint terdeteksi.
                return if (avgScore >= CONFIDENCE_THRESHOLD && validKeypointCount >= MIN_VALID_KEYPOINTS) {
                    Person(keypoints = smoothedKeypoints, score = avgScore)
                } else {
                    if (frameCount % 60 == 0) {
                        Log.v(TAG, "Pose tidak valid: avg=${"%.4f".format(avgScore)}, " +
                            "validKps=$validKeypointCount (min=$MIN_VALID_KEYPOINTS)")
                    }
                    // Reset EMA agar posisi lama tidak "hantu" ke frame berikutnya
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
     * - Confidence < 0.3 (keypoint tidak terlihat dengan baik): simpan posisi frame sebelumnya
     *   di buffer, kembalikan keypoint dengan score=0 agar renderer tidak menampilkannya.
     *   Ini mencegah keypoint "muncul di tempat salah" karena posisi noise bercampur ke EMA.
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
            if (kp.score < 0.3f) {
                // Keypoint tidak terlihat dengan baik — tidak bisa dipercaya posisinya.
                // Pertahankan posisi sebelumnya di buffer EMA (agar jika keypoint muncul lagi,
                // smoothing tetap konsisten), tapi kembalikan score=0 ke renderer.
                smoothedValues[idx]     = prevKps[idx]
                smoothedValues[idx + 1] = prevKps[idx + 1]
                // score=0 memberi sinyal ke renderer untuk tidak menampilkan titik ini
                smoothedKeypoints.add(kp.copy(x = prevKps[idx], y = prevKps[idx + 1], score = 0f))
            } else {
                // EMA normal — blend posisi current dengan previous
                val sx = EMA_ALPHA * currentRawValues[idx]     + (1 - EMA_ALPHA) * prevKps[idx]
                val sy = EMA_ALPHA * currentRawValues[idx + 1] + (1 - EMA_ALPHA) * prevKps[idx + 1]
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
