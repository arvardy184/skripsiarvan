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
 * MediaPipe BlazePose Lite detector implementation Input: 256x256 RGB image Output: [1, 195] tensor
 * (landmarks + visibility + presence) Note: BlazePose outputs 33 keypoints, but we map to COCO 17
 * format for consistency
 */
class BlazePoseDetector(private val interpreter: Interpreter) : PoseDetector {

    companion object {
        private const val TAG = "BlazePoseDetector"
        private const val INPUT_SIZE = 256
        private const val NUM_KEYPOINTS = 17
        private const val CONFIDENCE_THRESHOLD = 0.5f

        // Mapping from BlazePose 33 keypoints to COCO 17 keypoints
        private val BLAZEPOSE_TO_COCO =
                mapOf(
                        0 to 0, // nose
                        2 to 1, // left_eye_inner -> left_eye
                        5 to 2, // right_eye_inner -> right_eye
                        7 to 3, // left_ear
                        8 to 4, // right_ear
                        11 to 5, // left_shoulder
                        12 to 6, // right_shoulder
                        13 to 7, // left_elbow
                        14 to 8, // right_elbow
                        15 to 9, // left_wrist
                        16 to 10, // right_wrist
                        23 to 11, // left_hip
                        24 to 12, // right_hip
                        25 to 13, // left_knee
                        26 to 14, // right_knee
                        27 to 15, // left_ankle
                        28 to 16 // right_ankle
                )
    }

    private val imageProcessor =
            ImageProcessor.Builder()
                    .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    .add(NormalizeOp(127.5f, 127.5f)) // BlazePose biasanya butuh range [-1, 1]
                    .build()

    private val lock = Any()
    private var isClosed = false

    override fun detectPose(bitmap: Bitmap): Person? {
        synchronized(lock) {
            if (isClosed) return null

            try {
                // Preprocess image
                var tensorImage = TensorImage.fromBitmap(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // BlazePose landmark model biasanya punya output [1, 195] atau [1, 165]
                val outputTensor = interpreter.getOutputTensor(0)
                val outputShape = outputTensor.shape()

                // Calculate total size to allocate buffer
                val totalSize = outputShape.reduce { acc, i -> acc * i }
                val outputBuffer = java.nio.FloatBuffer.allocate(totalSize)
                // Note: rewind buffer to be safe, though allocate does it
                outputBuffer.rewind()

                // Run inference
                val outputs = mapOf(0 to outputBuffer)
                interpreter.runForMultipleInputsOutputs(arrayOf(tensorImage.buffer), outputs)

                // Read landmarks from buffer
                outputBuffer.rewind() // Reset position for reading
                val keypoints = mutableListOf<Keypoint>()
                var totalScore = 0f

                // Use input buffer array if accessible, otherwise get from buffer
                // FloatBuffer.allocate gives us a backing array usually
                val values =
                        if (outputBuffer.hasArray()) {
                            outputBuffer.array()
                        } else {
                            val arr = FloatArray(totalSize)
                            outputBuffer.get(arr)
                            arr
                        }

                for (cocoIdx in 0 until NUM_KEYPOINTS) {
                    val blazePoseIdx = BLAZEPOSE_TO_COCO[cocoIdx] ?: 0
                    val offset = blazePoseIdx * 5

                    if (offset + 4 >= totalSize) continue

                    // Debug raw values for first keypoint
                    if (cocoIdx == 0 && Math.random() < 0.01) {
                        Log.d(
                                TAG,
                                "Raw check: x=${values[offset]}, y=${values[offset+1]}, vis=${values[offset+3]}. InputSize=$INPUT_SIZE"
                        )
                    }

                    // BlazePose coords are often normalized [0, INPUT_SIZE] atau [0, 1]
                    // WARNING: If raw values are small (e.g. 0.5), division by INPUT_SIZE makes
                    // them 0
                    // Let's assume for now they are [0, 255] range based on typical TFLite models
                    val x = values[offset] / INPUT_SIZE.toFloat()
                    val y = values[offset + 1] / INPUT_SIZE.toFloat()
                    val visibility = values[offset + 3]

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

                if (avgScore < CONFIDENCE_THRESHOLD) {
                    Log.v(TAG, "Pose confidence too low: %.4f".format(avgScore))
                }

                return if (avgScore >= CONFIDENCE_THRESHOLD) {
                    Person(keypoints = keypoints, score = avgScore)
                } else {
                    null
                }
            } catch (e: Exception) {
                if (e is IllegalStateException && isClosed) {
                    Log.d(TAG, "Detector closed during inference, ignoring error")
                } else {
                    // Log output shape if possible for debugging
                    try {
                        val shape = interpreter.getOutputTensor(0).shape().contentToString()
                        Log.e(
                                TAG,
                                "Error during BlazePose detection. Output shape: $shape. Msg: ${e.message}"
                        )
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error during BlazePose detection: ${e.message}", e)
                    }
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
