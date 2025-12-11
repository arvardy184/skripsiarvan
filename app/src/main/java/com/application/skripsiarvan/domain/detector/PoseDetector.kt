package com.application.skripsiarvan.domain.detector

import android.graphics.Bitmap
import com.application.skripsiarvan.domain.model.Person

/**
 * Strategy pattern interface for pose detection
 * Allows easy swapping between different models (MoveNet, BlazePose, etc.)
 */
interface PoseDetector {

    /**
     * Detect pose from bitmap image
     * @param bitmap Input image
     * @return Detected person with keypoints, or null if no person detected
     */
    fun detectPose(bitmap: Bitmap): Person?

    /**
     * Get the required input image size for this model
     * @return Pair of (width, height)
     */
    fun getInputSize(): Pair<Int, Int>

    /**
     * Clean up resources
     */
    fun close()
}
