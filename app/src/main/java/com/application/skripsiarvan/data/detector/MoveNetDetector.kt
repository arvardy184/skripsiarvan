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
        private const val CONFIDENCE_THRESHOLD = 0.3f

        // EMA smoothing — IDENTIK dengan BlazePose (0.2f) untuk perbandingan fair.
        // Post-processing pipeline dikontrol sama di kedua model (Bab 3 Metodologi).
        private const val EMA_ALPHA = 0.2f

        // Deadband — IDENTIK dengan BlazePose (0.008f).
        private const val EMA_DEADBAND = 0.008f

        // Threshold confidence rendah — IDENTIK dengan BlazePose (0.4f).
        // Keypoint dengan score < nilai ini dibekukan ke posisi sebelumnya (tidak di-EMA).
        private const val EMA_LOW_CONF_THRESHOLD = 0.4f
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
                        if (kp.score < EMA_LOW_CONF_THRESHOLD) {
                            // Confidence rendah → bekukan ke posisi sebelumnya (identik dengan BlazePose)
                            kp.copy(x = prevKps[idx], y = prevKps[idx + 1])
                        } else {
                            val candidateX = EMA_ALPHA * currentRaw[idx]     + (1 - EMA_ALPHA) * prevKps[idx]
                            val candidateY = EMA_ALPHA * currentRaw[idx + 1] + (1 - EMA_ALPHA) * prevKps[idx + 1]
                            val dx = kotlin.math.abs(candidateX - prevKps[idx])
                            val dy = kotlin.math.abs(candidateY - prevKps[idx + 1])
                            val sx = if (dx > EMA_DEADBAND) candidateX else prevKps[idx]
                            val sy = if (dy > EMA_DEADBAND) candidateY else prevKps[idx + 1]
                            kp.copy(x = sx, y = sy)
                        }
                    }
                }

                return if (avgScore >= CONFIDENCE_THRESHOLD) {
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
                    previousKeypoints = null  // reset EMA saat orang tidak terdeteksi
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
