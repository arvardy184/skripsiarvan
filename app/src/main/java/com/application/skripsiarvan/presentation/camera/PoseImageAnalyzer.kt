package com.application.skripsiarvan.presentation.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.application.skripsiarvan.domain.detector.PoseDetector
import com.application.skripsiarvan.domain.model.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

/**
 * CameraX ImageAnalyzer for real-time pose detection
 * Handles image conversion, rotation, and performance tracking
 */
class PoseImageAnalyzer(
    private val poseDetector: PoseDetector,
    private val onResults: (Person?, Long, Float) -> Unit // (person, inferenceTime, fps)
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()
    private var currentFps = 0f

    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            val startTime = System.nanoTime()

            // Convert ImageProxy to Bitmap
            val bitmap = imageProxy.toBitmap()
            val rotatedBitmap = bitmap.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // Resize to model input size
            val inputSize = poseDetector.getInputSize()
            val resizedBitmap = Bitmap.createScaledBitmap(
                rotatedBitmap,
                inputSize.first,
                inputSize.second,
                true
            )

            // Run inference
            val person = poseDetector.detectPose(resizedBitmap)

            // Calculate inference latency (in milliseconds)
            val inferenceTime = (System.nanoTime() - startTime) / 1_000_000L

            // Calculate FPS
            frameCount++
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastFpsTimestamp

            if (elapsedTime >= 1000) { // Update FPS every second
                currentFps = (frameCount * 1000f) / elapsedTime
                frameCount = 0
                lastFpsTimestamp = currentTime
            }

            // Send results back to UI
            onResults(person, inferenceTime, currentFps)

            // Clean up
            resizedBitmap.recycle()
            rotatedBitmap.recycle()
            bitmap.recycle()
            imageProxy.close()
        }
    }
}

/**
 * Extension: Convert ImageProxy to Bitmap
 */
private fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = android.graphics.YuvImage(
        nv21,
        android.graphics.ImageFormat.NV21,
        width,
        height,
        null
    )

    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(
        android.graphics.Rect(0, 0, width, height),
        100,
        out
    )

    val imageBytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

/**
 * Extension: Rotate bitmap by degrees
 */
private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
