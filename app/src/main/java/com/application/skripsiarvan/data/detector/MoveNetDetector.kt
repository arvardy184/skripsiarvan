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
import java.nio.ByteBuffer

/**
 * MoveNet Lightning pose detector implementation
 * Input: 192x192 RGB image
 * Output: [1, 1, 17, 3] tensor (1 person, 17 keypoints, [y, x, score])
 */
class MoveNetDetector(
    private val interpreter: Interpreter
) : PoseDetector {

    companion object {
        private const val TAG = "MoveNetDetector"
        private const val INPUT_SIZE = 192
        private const val NUM_KEYPOINTS = 17
        private const val CONFIDENCE_THRESHOLD = 0.3f
    }

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f)) // Normalize to [0, 1]
        .build()

    override fun detectPose(bitmap: Bitmap): Person? {
        try {
            // Preprocess image
            var tensorImage = TensorImage.fromBitmap(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // Prepare output buffer: [1, 1, 17, 3]
            val outputShape = interpreter.getOutputTensor(0).shape()
            val output = Array(1) {
                Array(1) {
                    Array(NUM_KEYPOINTS) {
                        FloatArray(3)
                    }
                }
            }

            // Run inference
            interpreter.run(tensorImage.buffer, output)

            // Parse output
            val keypoints = mutableListOf<Keypoint>()
            var totalScore = 0f

            for (i in 0 until NUM_KEYPOINTS) {
                val y = output[0][0][i][0] // Normalized y [0-1]
                val x = output[0][0][i][1] // Normalized x [0-1]
                val score = output[0][0][i][2]

                keypoints.add(
                    Keypoint(
                        x = x,
                        y = y,
                        score = score,
                        label = BodyPart.labels[i]
                    )
                )
                totalScore += score
            }

            val avgScore = totalScore / NUM_KEYPOINTS

            // Only return if average confidence is above threshold
            return if (avgScore >= CONFIDENCE_THRESHOLD) {
                Person(keypoints = keypoints, score = avgScore)
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during pose detection", e)
            return null
        }
    }

    override fun getInputSize(): Pair<Int, Int> = Pair(INPUT_SIZE, INPUT_SIZE)

    override fun close() {
        // Interpreter is managed by TFLiteHelper
    }
}
