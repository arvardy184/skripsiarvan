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
 * MediaPipe BlazePose Lite detector implementation
 * Input: 256x256 RGB image
 * Output: [1, 195] tensor (landmarks + visibility + presence)
 * Note: BlazePose outputs 33 keypoints, but we map to COCO 17 format for consistency
 */
class BlazePoseDetector(
    private val interpreter: Interpreter
) : PoseDetector {

    companion object {
        private const val TAG = "BlazePoseDetector"
        private const val INPUT_SIZE = 256
        private const val NUM_KEYPOINTS = 17
        private const val CONFIDENCE_THRESHOLD = 0.5f

        // Mapping from BlazePose 33 keypoints to COCO 17 keypoints
        private val BLAZEPOSE_TO_COCO = mapOf(
            0 to 0,   // nose
            2 to 1,   // left_eye_inner -> left_eye
            5 to 2,   // right_eye_inner -> right_eye
            7 to 3,   // left_ear
            8 to 4,   // right_ear
            11 to 5,  // left_shoulder
            12 to 6,  // right_shoulder
            13 to 7,  // left_elbow
            14 to 8,  // right_elbow
            15 to 9,  // left_wrist
            16 to 10, // right_wrist
            23 to 11, // left_hip
            24 to 12, // right_hip
            25 to 13, // left_knee
            26 to 14, // right_knee
            27 to 15, // left_ankle
            28 to 16  // right_ankle
        )
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

            // Prepare output buffer: [1, 195] = 33 landmarks * 5 (x, y, z, visibility, presence) + extra
            // Simplified: We'll use [1, 33, 5] for easier parsing
            val output = Array(1) {
                Array(33) {
                    FloatArray(5)
                }
            }

            // Run inference
            interpreter.run(tensorImage.buffer, output)

            // Map BlazePose 33 keypoints to COCO 17 format
            val keypoints = mutableListOf<Keypoint>()
            var totalScore = 0f

            for (cocoIdx in 0 until NUM_KEYPOINTS) {
                val blazePoseIdx = BLAZEPOSE_TO_COCO.entries.find { it.value == cocoIdx }?.key ?: 0

                val x = output[0][blazePoseIdx][0] // Normalized x [0-1]
                val y = output[0][blazePoseIdx][1] // Normalized y [0-1]
                val visibility = output[0][blazePoseIdx][3] // Visibility score

                keypoints.add(
                    Keypoint(
                        x = x,
                        y = y,
                        score = visibility,
                        label = BodyPart.labels[cocoIdx]
                    )
                )
                totalScore += visibility
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
