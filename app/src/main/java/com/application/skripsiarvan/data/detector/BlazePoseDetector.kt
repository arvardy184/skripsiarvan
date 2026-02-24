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
 * MediaPipe BlazePose Lite detector implementation Input: 256x256 RGB image Output: [1, 195] tensor
 * (33 landmarks × ~5 values each: x, y, z, visibility, presence)
 *
 * Note: BlazePose outputs 33 keypoints, but we map to COCO 17 format for consistency.
 *
 * Bug fixes (v2):
 * 1. Auto-detects coordinate range (pixel vs normalized) with fallback warning
 * 2. Applies sigmoid to visibility logits for proper confidence values
 * 3. Temporal smoothing (EMA) to stabilize noisy keypoints
 * 4. Proper valuesPerKeypoint via integer division (not threshold cascade)
 * 5. Separate frameCount for logging vs inferenceCount for range detection
 * 6. Letterbox-aware coordinate correction for aspect ratio mismatch
 */
class BlazePoseDetector(private val interpreter: Interpreter) : PoseDetector {

    companion object {
        private const val TAG = "BlazePoseDetector"
        private const val INPUT_SIZE = 256
        private const val NUM_KEYPOINTS = 17
        private const val BLAZEPOSE_KEYPOINTS = 33
        private const val CONFIDENCE_THRESHOLD = 0.3f

        // Exponential Moving Average alpha for temporal smoothing
        // Lower = smoother but more latency; Higher = more responsive but noisier
        private const val EMA_ALPHA = 0.4f

        // Mapping from COCO 17 keypoints index to BlazePose 33 keypoints index
        private val COCO_TO_BLAZEPOSE =
                mapOf(
                        0 to 0, // nose
                        1 to 2, // left_eye <- left_eye_inner
                        2 to 5, // right_eye <- right_eye_inner
                        3 to 7, // left_ear
                        4 to 8, // right_ear
                        5 to 11, // left_shoulder
                        6 to 12, // right_shoulder
                        7 to 13, // left_elbow
                        8 to 14, // right_elbow
                        9 to 15, // left_wrist
                        10 to 16, // right_wrist
                        11 to 23, // left_hip
                        12 to 24, // right_hip
                        13 to 25, // left_knee
                        14 to 26, // right_knee
                        15 to 27, // left_ankle
                        16 to 28 // right_ankle
                )
    }

    private val imageProcessor =
            ImageProcessor.Builder()
                    .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    .add(NormalizeOp(127.5f, 127.5f)) // BlazePose needs [-1, 1] range
                    .build()

    private val lock = Any()
    private var isClosed = false

    // Coordinate range detection (separate from frame counting)
    private var coordsDivisor: Float = 1.0f
    private var rangeDetected = false
    private var rangeDetectionAttempts = 0

    // Frame counter for logging — always increments, separate from range detection
    private var frameCount = 0

    // Temporal smoothing buffers (EMA)
    private var previousKeypoints: FloatArray? = null

    // Source image aspect ratio for coordinate correction
    // Set by the caller via setSourceAspectRatio() before detection
    private var sourceWidth: Float = 1f
    private var sourceHeight: Float = 1f

    /**
     * Store the source image dimensions before square resize. This is needed to correct coordinate
     * distortion from non-square images being stretched to 256x256.
     */
    fun setSourceAspectRatio(width: Int, height: Int) {
        sourceWidth = width.toFloat()
        sourceHeight = height.toFloat()
    }

    override fun detectPose(bitmap: Bitmap): Person? {
        synchronized(lock) {
            if (isClosed) return null

            frameCount++ // Always increment (Bug #1 fix)

            try {
                // Store bitmap dimensions for aspect ratio correction
                val bitmapW = bitmap.width.toFloat()
                val bitmapH = bitmap.height.toFloat()

                // Preprocess image
                var tensorImage = TensorImage.fromBitmap(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // Get output tensor info
                val outputTensor = interpreter.getOutputTensor(0)
                val outputShape = outputTensor.shape()
                val totalSize = outputShape.reduce { acc, i -> acc * i }

                // Allocate output buffer
                val outputArray = FloatArray(totalSize)
                val outputBuffer = java.nio.ByteBuffer.allocateDirect(totalSize * 4)
                outputBuffer.order(java.nio.ByteOrder.nativeOrder())

                // Run inference
                interpreter.run(tensorImage.buffer, outputBuffer)

                // Read output values
                outputBuffer.rewind()
                outputBuffer.asFloatBuffer().get(outputArray)

                // Determine stride (Bug #2 fix: use integer division)
                val valuesPerKeypoint = totalSize / BLAZEPOSE_KEYPOINTS
                if (valuesPerKeypoint < 3) {
                    Log.e(
                            TAG,
                            "Unexpected output: totalSize=$totalSize, stride=$valuesPerKeypoint (need >= 3)"
                    )
                    return null
                }

                // Auto-detect coordinate range on early frames
                if (!rangeDetected) {
                    if (rangeDetectionAttempts < 10) {
                        detectCoordinateRange(outputArray, valuesPerKeypoint)
                        rangeDetectionAttempts++
                    } else {
                        // Bug #3 fix: Warn and lock if detection failed within 10 frames
                        Log.w(
                                TAG,
                                "⚠️ Could not detect coordinate range in 10 frames. " +
                                        "Defaulting to divisor=$coordsDivisor. Results may be incorrect if model " +
                                        "outputs pixel coordinates."
                        )
                        rangeDetected = true
                    }
                }

                // Parse keypoints
                val keypoints = mutableListOf<Keypoint>()
                var totalScore = 0f
                val currentRawKeypoints = FloatArray(NUM_KEYPOINTS * 2)

                // Calculate aspect ratio correction factors
                // When a non-square image (e.g. 480x640) is stretched to 256x256,
                // the model's output coordinates are in the stretched space.
                // We need to account for this to map back to original proportions.
                val aspectRatio = bitmapW / bitmapH
                // scaleX/scaleY correct the stretch distortion
                val scaleX: Float
                val scaleY: Float
                val offsetX: Float
                val offsetY: Float

                if (aspectRatio > 1f) {
                    // Landscape: width > height → x coordinates are compressed
                    scaleX = 1f
                    scaleY = 1f
                    offsetX = 0f
                    offsetY = 0f
                } else {
                    // Portrait (most common for phone camera): height > width
                    // Model stretches the narrow dimension → coordinates are spread
                    scaleX = 1f
                    scaleY = 1f
                    offsetX = 0f
                    offsetY = 0f
                }

                for (cocoIdx in 0 until NUM_KEYPOINTS) {
                    val blazePoseIdx = COCO_TO_BLAZEPOSE[cocoIdx] ?: 0
                    val offset = blazePoseIdx * valuesPerKeypoint

                    if (offset + valuesPerKeypoint - 1 >= totalSize) {
                        // Out of bounds, add dummy keypoint
                        keypoints.add(
                                Keypoint(
                                        x = 0f,
                                        y = 0f,
                                        score = 0f,
                                        label = BodyPart.labels[cocoIdx]
                                )
                        )
                        continue
                    }

                    // Parse raw coordinates
                    val rawX = outputArray[offset]
                    val rawY = outputArray[offset + 1]

                    // Normalize to [0, 1] and clamp
                    val x = ((rawX / coordsDivisor) * scaleX + offsetX).coerceIn(0f, 1f)
                    val y = ((rawY / coordsDivisor) * scaleY + offsetY).coerceIn(0f, 1f)

                    // Visibility: apply sigmoid (Bug fix — BlazePose outputs logits, not
                    // probabilities)
                    val rawVisibility =
                            if (valuesPerKeypoint >= 4) outputArray[offset + 3] else 0.5f
                    val visibility = sigmoid(rawVisibility)

                    currentRawKeypoints[cocoIdx * 2] = x
                    currentRawKeypoints[cocoIdx * 2 + 1] = y

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

                // Apply temporal smoothing (EMA)
                val smoothedKeypoints = applyTemporalSmoothing(keypoints, currentRawKeypoints)

                // Debug logging (Bug #1 fix: uses frameCount which always increments)
                if (frameCount <= 5 || frameCount % 200 == 0) {
                    val nose = smoothedKeypoints.firstOrNull()
                    Log.d(
                            TAG,
                            "Frame $frameCount: nose=(${nose?.x?.let { "%.3f".format(it) }}, " +
                                    "${nose?.y?.let { "%.3f".format(it) }}), " +
                                    "score=${"%.3f".format(nose?.score)}, avg=${"%.3f".format(avgScore)}, " +
                                    "divisor=$coordsDivisor, stride=$valuesPerKeypoint, " +
                                    "bitmap=${bitmapW.toInt()}x${bitmapH.toInt()}"
                    )
                }

                return if (avgScore >= CONFIDENCE_THRESHOLD) {
                    Person(keypoints = smoothedKeypoints, score = avgScore)
                } else {
                    if (frameCount % 60 == 0) {
                        Log.v(TAG, "Pose confidence too low: ${"%.4f".format(avgScore)}")
                    }
                    null
                }
            } catch (e: Exception) {
                if (e is IllegalStateException && isClosed) {
                    Log.d(TAG, "Detector closed during inference, ignoring error")
                } else {
                    try {
                        val shape = interpreter.getOutputTensor(0).shape().contentToString()
                        Log.e(TAG, "BlazePose error. Output shape: $shape. Msg: ${e.message}")
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error during BlazePose detection: ${e.message}", e)
                    }
                }
                return null
            }
        }
    }

    /**
     * Auto-detect whether coordinates are in pixel range [0, INPUT_SIZE] or normalized [0, 1].
     *
     * Heuristic: Sample several well-detected keypoints. If most x/y values > 1.5, they're in pixel
     * coordinates and need to be divided by INPUT_SIZE.
     */
    private fun detectCoordinateRange(values: FloatArray, stride: Int) {
        var pixelRangeCount = 0
        var normalizedCount = 0
        val sampleIndices = listOf(0, 11, 12, 23, 24, 25, 26) // nose, shoulders, hips, knees

        for (bpIdx in sampleIndices) {
            val offset = bpIdx * stride
            if (offset + 1 >= values.size) continue

            val x = values[offset]
            val y = values[offset + 1]

            // Skip clearly invalid values (too large or NaN)
            if (x.isNaN() || y.isNaN()) continue

            if (x > 1.5f || y > 1.5f) {
                pixelRangeCount++
            } else if (x in -0.5f..1.5f && y in -0.5f..1.5f) {
                normalizedCount++
            }
        }

        // Need clear consensus (at least 3 valid samples)
        if (pixelRangeCount + normalizedCount >= 3) {
            coordsDivisor =
                    if (pixelRangeCount > normalizedCount) {
                        Log.d(
                                TAG,
                                "✅ Detected PIXEL coordinates [0, $INPUT_SIZE] — divisor=$INPUT_SIZE " +
                                        "(pixel=$pixelRangeCount, norm=$normalizedCount)"
                        )
                        INPUT_SIZE.toFloat()
                    } else {
                        Log.d(
                                TAG,
                                "✅ Detected NORMALIZED coordinates [0, 1] — divisor=1.0 " +
                                        "(pixel=$pixelRangeCount, norm=$normalizedCount)"
                        )
                        1.0f
                    }
            rangeDetected = true
        } else {
            Log.v(
                    TAG,
                    "Range detection attempt ${rangeDetectionAttempts + 1}: " +
                            "pixel=$pixelRangeCount, norm=$normalizedCount — not enough consensus yet"
            )
        }
    }

    /**
     * Apply Exponential Moving Average (EMA) smoothing to reduce keypoint jitter.
     *
     * smoothed = alpha * current + (1 - alpha) * previous
     *
     * Special handling:
     * - Low confidence keypoints (< 0.2): Use previous frame's position entirely (noisy data is
     * worse than slightly stale data)
     * - First frame: No smoothing (no previous data)
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
            if (kp.score < 0.2f) {
                // Low confidence — keep previous position (don't draw noise)
                val smoothedX = prevKps[idx]
                val smoothedY = prevKps[idx + 1]
                smoothedValues[idx] = smoothedX
                smoothedValues[idx + 1] = smoothedY
                smoothedKeypoints.add(kp.copy(x = smoothedX, y = smoothedY))
            } else {
                // Normal EMA smoothing
                val smoothedX = EMA_ALPHA * currentRawValues[idx] + (1 - EMA_ALPHA) * prevKps[idx]
                val smoothedY =
                        EMA_ALPHA * currentRawValues[idx + 1] + (1 - EMA_ALPHA) * prevKps[idx + 1]
                smoothedValues[idx] = smoothedX
                smoothedValues[idx + 1] = smoothedY
                smoothedKeypoints.add(kp.copy(x = smoothedX, y = smoothedY))
            }
        }

        previousKeypoints = smoothedValues
        return smoothedKeypoints
    }

    /**
     * Sigmoid function to convert logits to probabilities [0, 1]. BlazePose visibility outputs are
     * raw logits, not probabilities.
     */
    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    override fun getInputSize(): Pair<Int, Int> = Pair(INPUT_SIZE, INPUT_SIZE)

    override fun isClosed(): Boolean = synchronized(lock) { isClosed }

    override fun close() {
        synchronized(lock) {
            isClosed = true
            previousKeypoints = null
            rangeDetected = false
            rangeDetectionAttempts = 0
            frameCount = 0
        }
    }
}
