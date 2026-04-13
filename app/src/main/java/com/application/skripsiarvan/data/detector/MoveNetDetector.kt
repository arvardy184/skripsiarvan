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
 * MoveNet Lightning pose detector implementation Input: 192x192 RGB image Output: [1, 1, 17, 3]
 * tensor (1 person, 17 keypoints, [y, x, score])
 */
class MoveNetDetector(private val interpreter: Interpreter) : PoseDetector {

    companion object {
        private const val TAG = "MoveNetDetector"
        private const val INPUT_SIZE = 192
        private const val NUM_KEYPOINTS = 17

        // Avg-score threshold: orang dianggap "ada" jika rata-rata score ≥ ini.
        private const val CONFIDENCE_THRESHOLD = 0.3f

        // EMA alpha — dinaikkan dari 0.2 → 0.4 agar responsif untuk gerakan dinamis (squat).
        // Rasional: di 15fps, alpha=0.2 membuat smoothed signal lag ~5 frame di balik posisi
        // sebenarnya (80% bobot ke frame lama). Untuk squat 3 detik turun + 3 detik naik,
        // sudut lutut yang sebenarnya sudah menyentuh 100° tapi smoothed masih ~135°.
        // Alpha 0.4 menyeimbangkan noise suppression dan responsiveness (lag ~2 frame).
        private const val EMA_ALPHA = 0.4f

        // Alpha untuk keypoint dengan confidence rendah — tetap smoothing tapi izinkan gerakan.
        // Sebelumnya: keypoint <0.4 di-FREEZE ke posisi sebelumnya → hip/knee/ankle
        // terkunci di posisi berdiri sepanjang squat (MoveNet Lightning sering beri score
        // <0.4 saat lower-body foreshortened dari kamera depan). Ini penyebab 0 reps.
        private const val EMA_ALPHA_LOW = 0.1f

        // Deadband kecil untuk meredam jitter sub-pixel saat diam.
        private const val EMA_DEADBAND = 0.008f

        // Threshold "sangat rendah" — HARUS << CONFIDENCE_THRESHOLD (0.3).
        // Kalau avgScore lolos 0.3 berarti orang terdeteksi; tidak masuk akal lalu
        // memfreeze keypoint pada threshold lebih tinggi (0.4) — itu yang membuat
        // "person detected tapi lower-body tidak bergerak".
        // 0.15 hanya menyaring keypoint yang benar-benar junk (mis. di luar frame).
        private const val EMA_LOW_CONF_THRESHOLD = 0.15f

        // Batas kegagalan berturut-turut sebelum EMA history di-clear sepenuhnya.
        // Selama ≤N frame gagal, kita pertahankan prev keypoints sebagai base EMA
        // agar angle tidak melompat saat avgScore dip sesaat (sering di fase bawah squat).
        private const val MAX_CONSECUTIVE_FAILURES = 10
    }

    private val imageProcessor by lazy {
        val inputType = interpreter.getInputTensor(0).dataType()
        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()

        Log.d(TAG, "Init: in=${inputShape.contentToString()} $inputType | out=${outputShape.contentToString()}")

        val builder = ImageProcessor.Builder()
            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))

        if (inputType == org.tensorflow.lite.DataType.FLOAT32) {
            builder.add(NormalizeOp(0f, 255f))
        }

        builder.build()
    }

    private val lock = Any()
    private var isClosed = false
    private var firstDetectionLogged = false

    // Buffer EMA: menyimpan koordinat [x0,y0, x1,y1, ..., x16,y16] dari frame sebelumnya
    private var previousKeypoints: FloatArray? = null
    private var consecutiveFailureCount = 0
    private var rawLogCounter = 0

    override fun detectPose(bitmap: Bitmap): Person? {
        synchronized(lock) {
            if (isClosed) return null

            try {
                var tensorImage = TensorImage.fromBitmap(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                val output = Array(1) { Array(1) { Array(NUM_KEYPOINTS) { FloatArray(3) } } }

                interpreter.run(tensorImage.buffer, output)

                val keypoints = mutableListOf<Keypoint>()
                var totalScore = 0f

                for (i in 0 until NUM_KEYPOINTS) {
                    val y = output[0][0][i][0]
                    val x = output[0][0][i][1]
                    val score = output[0][0][i][2]

                    keypoints.add(Keypoint(x = x, y = y, score = score, label = BodyPart.labels[i]))
                    totalScore += score
                }

                val avgScore = totalScore / NUM_KEYPOINTS

                // Terapkan EMA smoothing
                val currentRaw = FloatArray(NUM_KEYPOINTS * 2) { i ->
                    val kpIdx = i / 2
                    if (i % 2 == 0) keypoints[kpIdx].x else keypoints[kpIdx].y
                }
                val prevKps = previousKeypoints
                val smoothedKeypoints = if (prevKps == null) {
                    keypoints
                } else {
                    keypoints.mapIndexed { i, kp ->
                        val idx = i * 2
                        // Low-conf keypoint: gunakan alpha kecil (0.1) — tetap bergerak
                        // pelan mengikuti sinyal baru, tapi jangan di-freeze (bug lama).
                        val alpha = if (kp.score < EMA_LOW_CONF_THRESHOLD) EMA_ALPHA_LOW else EMA_ALPHA
                        val candidateX = alpha * currentRaw[idx]     + (1 - alpha) * prevKps[idx]
                        val candidateY = alpha * currentRaw[idx + 1] + (1 - alpha) * prevKps[idx + 1]
                        val dx = kotlin.math.abs(candidateX - prevKps[idx])
                        val dy = kotlin.math.abs(candidateY - prevKps[idx + 1])
                        val sx = if (dx > EMA_DEADBAND) candidateX else prevKps[idx]
                        val sy = if (dy > EMA_DEADBAND) candidateY else prevKps[idx + 1]
                        kp.copy(x = sx, y = sy)
                    }
                }

                // Per-10-frame raw keypoint log: verifikasi model menerima gambar bagus
                // dan memproduksi nilai mentah yang masuk akal (sebelum EMA).
                rawLogCounter++
                if (rawLogCounter % 10 == 0) {
                    val lh = keypoints[11]; val rh = keypoints[12]
                    val lk = keypoints[13]; val rk = keypoints[14]
                    val la = keypoints[15]; val ra = keypoints[16]
                    Log.d(TAG, ("RAW f=%d avg=%.2f | " +
                        "LHip[y=%.2f x=%.2f s=%.2f] RHip[y=%.2f x=%.2f s=%.2f] | " +
                        "LKnee[y=%.2f x=%.2f s=%.2f] RKnee[y=%.2f x=%.2f s=%.2f] | " +
                        "LAnk[y=%.2f x=%.2f s=%.2f] RAnk[y=%.2f x=%.2f s=%.2f]").format(
                        rawLogCounter, avgScore,
                        lh.y, lh.x, lh.score, rh.y, rh.x, rh.score,
                        lk.y, lk.x, lk.score, rk.y, rk.x, rk.score,
                        la.y, la.x, la.score, ra.y, ra.x, ra.score))
                }

                return if (avgScore >= CONFIDENCE_THRESHOLD) {
                    consecutiveFailureCount = 0
                    previousKeypoints = FloatArray(NUM_KEYPOINTS * 2) { i ->
                        val kpIdx = i / 2
                        if (i % 2 == 0) smoothedKeypoints[kpIdx].x else smoothedKeypoints[kpIdx].y
                    }
                    if (!firstDetectionLogged) {
                        firstDetectionLogged = true
                        val nose = smoothedKeypoints[0]
                        val lShoulder = smoothedKeypoints[5]
                        val rShoulder = smoothedKeypoints[6]
                        Log.d(TAG, "✓ First detection: avg=${"%.3f".format(avgScore)} | " +
                            "nose=(${"%5.3f".format(nose.x)}, ${"%5.3f".format(nose.y)}) s=${"%4.2f".format(nose.score)} | " +
                            "L.shoulder=(${"%5.3f".format(lShoulder.x)}, ${"%5.3f".format(lShoulder.y)}) | " +
                            "R.shoulder=(${"%5.3f".format(rShoulder.x)}, ${"%5.3f".format(rShoulder.y)})")
                    }
                    Person(keypoints = smoothedKeypoints, score = avgScore)
                } else {
                    // Jangan langsung reset EMA history. Kegagalan sesaat (avgScore dip
                    // di <0.3 saat fase bawah squat) sangat umum — reset menyebabkan
                    // angle melompat drastis begitu deteksi kembali. Pertahankan posisi
                    // terakhir sebagai base EMA sampai N frame gagal berturut-turut.
                    consecutiveFailureCount++
                    if (consecutiveFailureCount > MAX_CONSECUTIVE_FAILURES) {
                        previousKeypoints = null
                    }
                    null
                }
            } catch (e: Exception) {
                if (e is IllegalStateException && isClosed) {
                    Log.d(TAG, "Detector closed during inference, ignoring error")
                } else {
                    Log.e(TAG, "Error during pose detection", e)
                }
                return null
            }
        }
    }

    override fun getInputSize(): Pair<Int, Int> = Pair(INPUT_SIZE, INPUT_SIZE)

    override fun isClosed(): Boolean = synchronized(lock) { isClosed }

    override fun close() {
        synchronized(lock) { isClosed = true }
    }
}
