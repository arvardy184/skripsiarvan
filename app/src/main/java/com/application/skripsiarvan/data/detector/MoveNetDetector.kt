package com.application.skripsiarvan.data.detector

import android.graphics.Bitmap
import android.util.Log
import com.application.skripsiarvan.domain.detector.PoseDetector
import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.Keypoint
import com.application.skripsiarvan.domain.model.Person
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

/**
 * MoveNet Lightning pose detector implementation.
 *
 * Digunakan sebagai model pembanding (baseline) dalam benchmarking performa delegate TFLite
 * (sesuai Bab 3 Metodologi — variabel model: MoveNet Lightning INT8/FP32).
 *
 * Input  : 192×192 RGB image (INT8 atau FLOAT32)
 * Output : [1, 1, 17, 3] tensor — 17 COCO keypoints × [y, x, score]
 *          Koordinat y dan x sudah ternormalisasi ke [0, 1].
 *
 * Implementasi dibuat setara dengan BlazePoseDetector untuk keadilan benchmark:
 * 1. Buffer output di-cache di level kelas — tidak dialokasi ulang tiap frame (menghindari GC pressure)
 * 2. Warm-up 5 frame awal (sesuai Bab 4.5.1 — menghindari outlier inisialisasi cache)
 * 3. Temporal smoothing (EMA) α=0.6 — sama dengan BlazePoseDetector
 * 4. Validasi minimal keypoint valid (MIN_VALID_KEYPOINTS) — sama dengan BlazePoseDetector
 *
 * Catatan threshold: CONFIDENCE_THRESHOLD MoveNet (0.3f) ≠ BlazePose (0.65f) karena
 * output score MoveNet adalah probabilitas langsung [0,1], sedangkan BlazePose mengeluarkan
 * logit yang perlu sigmoid. Ini adalah perbedaan arsitektur model, bukan ketidakadilan benchmark.
 */
class MoveNetDetector(private val interpreter: Interpreter) : PoseDetector {

    companion object {
        private const val TAG = "MoveNetDetector"
        private const val INPUT_SIZE = 192
        private const val NUM_KEYPOINTS = 17

        // MoveNet output adalah probabilitas langsung [0, 1], bukan logit.
        // Threshold 0.3f setara dengan "30% confidence" — ini nilai rekomendasi resmi MoveNet.
        // BlazePose menggunakan 0.65f karena outputnya adalah sigmoid(logit), bukan probabilitas.
        private const val CONFIDENCE_THRESHOLD = 0.3f

        // Minimal jumlah keypoint valid agar pose dianggap terdeteksi.
        // Sama dengan BlazePoseDetector (8 dari 17) untuk keadilan benchmark.
        private const val MIN_VALID_KEYPOINTS = 8
        // Threshold per-keypoint disesuaikan dengan skala output MoveNet (probabilitas langsung).
        // Proporsi: MIN_KEYPOINT_SCORE / CONFIDENCE_THRESHOLD ≈ 0.67 (sama dengan BlazePose: 0.35/0.65)
        private const val MIN_KEYPOINT_SCORE = 0.2f

        // Warm-up 5 frame — sama dengan BlazePoseDetector (Bab 4.5.1 skripsi).
        // Menghindari outlier latensi akibat inisialisasi cache JIT dan model.
        private const val WARMUP_FRAMES = 5

        // Alpha EMA temporal smoothing — identik dengan BlazePoseDetector untuk keadilan.
        // Formula: smoothed = α × current + (1 - α) × previous
        private const val EMA_ALPHA = 0.6f
    }

    private val imageProcessor by lazy {
        val builder =
                ImageProcessor.Builder()
                        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))

        // MoveNet tersedia dalam varian INT8 dan FLOAT32.
        // INT8: tidak perlu normalisasi (nilai sudah dalam rentang byte).
        // FLOAT32: normalisasi ke [0, 1] dengan membagi 255.
        if (interpreter.getInputTensor(0).dataType() == org.tensorflow.lite.DataType.FLOAT32) {
            builder.add(NormalizeOp(0f, 255f))
        }

        builder.build()
    }

    private val lock = Any()
    private var isClosed = false

    // ── Counter frame ────────────────────────────────────────────────────────────────────────────
    private var frameCount = 0

    // ── Buffer output di-cache (dialokasi sekali, dipakai ulang tiap frame) ──────────────────────
    // Menghindari Array allocation berulang tiap frame yang menyebabkan GC pressure dan stuttering.
    // Setara dengan strategi caching ByteBuffer di BlazePoseDetector.
    private val cachedOutput = Array(1) { Array(1) { Array(NUM_KEYPOINTS) { FloatArray(3) } } }

    // ── Buffer EMA temporal smoothing ───────────────────────────────────────────────────────────
    // Format: [cocoIdx*2] = x, [cocoIdx*2+1] = y (17 keypoints × 2 koordinat = 34 nilai)
    private var previousKeypoints: FloatArray? = null

    // ── Validasi model dijalankan sekali ────────────────────────────────────────────────────────
    private var modelValidated = false

    /**
     * Validasi bahwa model yang dimuat sesuai spesifikasi MoveNet Lightning SinglePose.
     *
     * Spesifikasi yang diharapkan:
     *   Input  : [1, 192, 192, 3] UINT8 (INT8) atau FLOAT32
     *   Output : [1, 1, 17, 3] FLOAT32 — singlepose, 17 COCO keypoints, [y, x, score]
     *
     * Multipose MoveNet outputnya berbeda: [1, 6, 56] — BUKAN yang kita pakai.
     * Log dengan tag "MoveNetDetector", filter: adb logcat -s MoveNetDetector
     */
    private fun validateModel() {
        if (modelValidated) return
        modelValidated = true

        val sep = "─".repeat(60)
        Log.i(TAG, sep)
        Log.i(TAG, "  VALIDASI MODEL: MoveNet Lightning SinglePose INT8")
        Log.i(TAG, sep)

        // ── Input tensor ─────────────────────────────────────────────────────
        try {
            val inputShape = interpreter.getInputTensor(0).shape()
            val inputType  = interpreter.getInputTensor(0).dataType()
            val inputOk    = inputShape.contentEquals(intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 3))
            Log.i(TAG, "  Input  : shape=${inputShape.contentToString()}, type=$inputType")
            if (inputOk) {
                Log.i(TAG, "  Input  : ✓ sesuai [1, $INPUT_SIZE, $INPUT_SIZE, 3]")
            } else {
                Log.w(TAG, "  Input  : ⚠️ TIDAK SESUAI — expected [1, $INPUT_SIZE, $INPUT_SIZE, 3]. " +
                    "Apakah ini benar-benar MoveNet Lightning 192×192?")
            }
        } catch (e: Exception) {
            Log.e(TAG, "  Input  : ⛔ Gagal baca tensor input: ${e.message}")
        }

        // ── Output tensor ─────────────────────────────────────────────────────
        try {
            val numOutputs  = interpreter.outputTensorCount
            val outShape    = interpreter.getOutputTensor(0).shape()
            val outType     = interpreter.getOutputTensor(0).dataType()
            Log.i(TAG, "  Output : jumlah tensor = $numOutputs")
            Log.i(TAG, "  Output0: shape=${outShape.contentToString()}, type=$outType")

            // Singlepose: [1, 1, 17, 3]
            val isSinglePose = outShape.contentEquals(intArrayOf(1, 1, NUM_KEYPOINTS, 3))
            // Multipose:  [1, 6, 56]
            val isMultiPose  = outShape.size == 3 && outShape[1] == 6 && outShape[2] == 56

            when {
                isSinglePose ->
                    Log.i(TAG, "  Output0: ✓ SINGLEPOSE [1, 1, 17, 3] — [y, x, score] per keypoint (benar)")
                isMultiPose ->
                    Log.e(TAG, "  Output0: ⛔ MULTIPOSE [1, 6, 56] terdeteksi! " +
                        "Kode ini hanya mendukung singlepose. Ganti ke movenet_lightning_int8.tflite")
                else ->
                    Log.e(TAG, "  Output0: ⛔ Shape tidak dikenal ${outShape.contentToString()}. " +
                        "Pastikan file model adalah MoveNet SinglePose Lightning.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "  Output : ⛔ Gagal baca tensor output: ${e.message}")
        }

        Log.i(TAG, sep)
    }

    override fun detectPose(bitmap: Bitmap): Person? {
        synchronized(lock) {
            if (isClosed) return null

            frameCount++

            // Validasi model sekali di frame pertama
            if (frameCount == 1) validateModel()

            try {
                // Preprocessing citra
                var tensorImage = TensorImage.fromBitmap(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // Jalankan inferensi — buffer di-reuse tiap frame (tidak ada alokasi baru)
                interpreter.run(tensorImage.buffer, cachedOutput)

                // Parse output: [1, 1, 17, 3] → [y, x, score] per keypoint
                val keypoints = mutableListOf<Keypoint>()
                var totalScore = 0f
                val currentRawValues = FloatArray(NUM_KEYPOINTS * 2)

                for (i in 0 until NUM_KEYPOINTS) {
                    val y     = cachedOutput[0][0][i][0] // Normalized y [0, 1]
                    val x     = cachedOutput[0][0][i][1] // Normalized x [0, 1]
                    val score = cachedOutput[0][0][i][2]

                    currentRawValues[i * 2]     = x
                    currentRawValues[i * 2 + 1] = y

                    keypoints.add(Keypoint(x = x, y = y, score = score, label = BodyPart.labels[i]))
                    totalScore += score
                }

                val avgScore = totalScore / NUM_KEYPOINTS

                // Hitung jumlah keypoint yang cukup yakin
                val validKeypointCount = keypoints.count { it.score >= MIN_KEYPOINT_SCORE }

                // Terapkan EMA temporal smoothing — identik dengan BlazePoseDetector
                val smoothedKeypoints = applyTemporalSmoothing(keypoints, currentRawValues)

                // Debug logging — sparse
                if (frameCount <= WARMUP_FRAMES || frameCount % 200 == 0) {
                    val nose = smoothedKeypoints.firstOrNull()
                    Log.d(TAG,
                        "Frame $frameCount [${bitmap.width}×${bitmap.height}]: " +
                        "nose=(${nose?.x?.let { "%.3f".format(it) }}, " +
                               "${nose?.y?.let { "%.3f".format(it) }}), " +
                        "score=${"%.3f".format(nose?.score ?: 0f)}, " +
                        "avg=${"%.3f".format(avgScore)}, " +
                        "validKps=$validKeypointCount/$NUM_KEYPOINTS"
                    )
                }

                // Warm-up: 5 frame awal tidak digunakan — sama dengan BlazePoseDetector
                if (frameCount <= WARMUP_FRAMES) return null

                // Gate: rata-rata score harus >= CONFIDENCE_THRESHOLD DAN
                // minimal MIN_VALID_KEYPOINTS keypoint harus cukup yakin.
                return if (avgScore >= CONFIDENCE_THRESHOLD && validKeypointCount >= MIN_VALID_KEYPOINTS) {
                    Person(keypoints = smoothedKeypoints, score = avgScore)
                } else {
                    if (frameCount % 60 == 0) {
                        Log.v(TAG, "Pose tidak valid: avg=${"%.4f".format(avgScore)}, " +
                            "validKps=$validKeypointCount (min=$MIN_VALID_KEYPOINTS)")
                    }
                    resetTemporalSmoothing()
                    null
                }

            } catch (e: Exception) {
                if (e is IllegalStateException && isClosed) {
                    Log.d(TAG, "Detector sudah ditutup saat inferensi berlangsung — diabaikan")
                } else {
                    Log.e(TAG, "Error saat deteksi MoveNet: ${e.message}", e)
                }
                return null
            }
        }
    }

    /**
     * Terapkan EMA temporal smoothing — implementasi identik dengan BlazePoseDetector.
     *
     * Keypoint dengan score < 0.3 × MIN_KEYPOINT_SCORE threshold dikembalikan dengan score=0
     * agar renderer tidak menampilkannya, sambil mempertahankan posisi buffer EMA dari frame
     * sebelumnya (tidak terkontaminasi noise).
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
            if (kp.score < MIN_KEYPOINT_SCORE * 0.6f) {
                // Keypoint tidak terlihat dengan baik — pertahankan posisi buffer sebelumnya
                // tapi kembalikan score=0 agar tidak dirender di posisi yang salah.
                smoothedValues[idx]     = prevKps[idx]
                smoothedValues[idx + 1] = prevKps[idx + 1]
                smoothedKeypoints.add(kp.copy(x = prevKps[idx], y = prevKps[idx + 1], score = 0f))
            } else {
                // EMA normal
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

    private fun resetTemporalSmoothing() {
        previousKeypoints = null
    }

    override fun getInputSize(): Pair<Int, Int> = Pair(INPUT_SIZE, INPUT_SIZE)

    override fun isClosed(): Boolean = synchronized(lock) { isClosed }

    override fun close() {
        synchronized(lock) {
            isClosed = true
            previousKeypoints = null
        }
    }
}
