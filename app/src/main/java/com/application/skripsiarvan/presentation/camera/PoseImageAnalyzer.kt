package com.application.skripsiarvan.presentation.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.application.skripsiarvan.data.profiling.ResourceProfiler
import com.application.skripsiarvan.domain.detector.PoseDetector
import com.application.skripsiarvan.domain.model.Keypoint
import com.application.skripsiarvan.domain.model.Person
import kotlin.math.min

/**
 * CameraX ImageAnalyzer untuk real-time pose detection dengan profiling
 *
 * Sesuai dengan Bagian 4.5.1 Strategi Pengukuran Latensi:
 * - Menggunakan System.nanoTime() untuk presisi tinggi
 * - Protokol warm-up: 5 frame awal tanpa pencatatan
 *
 * Sesuai dengan Bagian 4.5.2 Strategi Profiling Sumber Daya:
 * - CPU Usage dari /proc/stat
 * - Memory Usage dari Debug.MemoryInfo
 * - Power Consumption dari BatteryManager API
 *
 * Key improvement: Uses LETTERBOX padding instead of stretching to maintain aspect ratio. This
 * prevents keypoint coordinate distortion when the camera image (4:3 or 16:9) doesn't match the
 * model's square input (256x256 or 192x192).
 */
class PoseImageAnalyzer(
        private val poseDetector: PoseDetector,
        private val resourceProfiler: ResourceProfiler,
        private val onResults:
                (
                        person: Person?,
                        processingTimeMs: Long,
                        modelInferenceTimeMs: Long,
                        fps: Float,
                        cpuUsage: Float,
                        memoryUsage: Float,
                        powerConsumption: Float,
                        cameraFrameAspectRatio: Float,
                        isWarmUpFrame: Boolean,
                        convertMs: Double,
                        preprocessMs: Double,
                        postprocessMs: Double,
                        avgKeypointConfidence: Float,
                        validKeypointCount: Int) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "PoseImageAnalyzer"
        // Jumlah frame warm-up sesuai Bagian 4.5.1 skripsi
        private const val WARM_UP_FRAMES = 5
        // Profiling resource cukup jarang agar pembacaan /proc, Debug.MemoryInfo, dan
        // BatteryManager tidak mengganggu frame analyzer di perangkat mid-range.
        private const val PROFILING_INTERVAL_MS = 500
    }

    // FPS calculation
    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()
    private var currentFps = 0f
    private var lastFrameTimestamp = 0L  // untuk instantaneous FPS di detik pertama

    // Warm-up tracking
    private var totalFrameCount = 0
    private val isWarmUpComplete: Boolean
        get() = totalFrameCount >= WARM_UP_FRAMES

    // Resource profiling timing
    private var lastProfilingTimestamp = 0L
    private var lastCpuUsage = 0f
    private var lastMemoryUsage = 0f
    private var lastPowerConsumption = 0f

    override fun analyze(imageProxy: ImageProxy) {
        try {
            totalFrameCount++
            val isWarmUpFrame = !isWarmUpComplete

            // t0: mulai seluruh pipeline
            val t0 = System.nanoTime()

            // Segment 1: Convert YUV → Bitmap
            val bitmap = imageProxy.toBitmapCustom()
            val rotatedBitmap = bitmap.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val cameraFrameAspectRatio = rotatedBitmap.width.toFloat() / rotatedBitmap.height
            val t1 = System.nanoTime()

            // Segment 2: Preprocess — letterbox (atau skip jika detector handle sendiri)
            val handlesPreprocessingInternally = poseDetector.handlesPreprocessingInternally()
            val letterboxResult =
                    if (handlesPreprocessingInternally) {
                        null
                    } else {
                        val inputSize = poseDetector.getInputSize()
                        createLetterboxedBitmap(rotatedBitmap, inputSize.first, inputSize.second)
                    }
            val detectorBitmap = letterboxResult?.bitmap ?: rotatedBitmap
            val t2 = System.nanoTime()

            if (poseDetector.isClosed()) {
                letterboxResult?.bitmap?.recycle()
                rotatedBitmap.recycle()
                bitmap.recycle()
                imageProxy.close()
                return
            }

            // Segment 3: Inference — TFLite detectPose()
            val person = poseDetector.detectPose(detectorBitmap)
            val t3 = System.nanoTime()

            // Segment 4: Postprocess — koreksi koordinat letterbox
            val correctedPerson =
                    if (letterboxResult != null) {
                        person?.let { correctLetterboxCoordinates(it, letterboxResult) }
                    } else {
                        person
                    }
            val t4 = System.nanoTime()

            val convertMs = (t1 - t0) / 1_000_000.0
            val preprocessMs = (t2 - t1) / 1_000_000.0
            val modelInferenceTimeMs = (t3 - t2) / 1_000_000L
            val postprocessMs = (t4 - t3) / 1_000_000.0
            val processingTimeMs = (t4 - t0) / 1_000_000L

            // Keypoint quality
            val keypoints = correctedPerson?.keypoints ?: emptyList()
            val validKeypoints = keypoints.filter { it.score >= 0.2f }
            val avgKeypointConfidence = if (keypoints.isNotEmpty()) keypoints.map { it.score }.average().toFloat() else 0f
            val validKeypointCount = validKeypoints.size

            // FPS: rolling 1-detik; fallback instantaneous untuk detik pertama
            frameCount++
            val currentTime = System.currentTimeMillis()
            val frameDeltaMs = if (lastFrameTimestamp > 0) currentTime - lastFrameTimestamp else 0L
            lastFrameTimestamp = currentTime
            val elapsedTime = currentTime - lastFpsTimestamp

            if (elapsedTime >= 1000) {
                currentFps = (frameCount * 1000f) / elapsedTime
                frameCount = 0
                lastFpsTimestamp = currentTime
            } else if (currentFps == 0f && frameDeltaMs > 0) {
                currentFps = 1000f / frameDeltaMs
            }

            // Resource profiling dengan interval (Bagian 4.5.2)
            if (currentTime - lastProfilingTimestamp >= PROFILING_INTERVAL_MS) {
                lastCpuUsage = resourceProfiler.getCpuUsage()
                val memoryInfo = resourceProfiler.getMemoryUsage()
                lastMemoryUsage = memoryInfo.usedMemoryMb
                lastPowerConsumption = resourceProfiler.getPowerConsumption()
                lastProfilingTimestamp = currentTime
            }

            // Send results back to UI
            onResults(
                    correctedPerson,
                    if (isWarmUpFrame) 0L else processingTimeMs,
                    if (isWarmUpFrame) 0L else modelInferenceTimeMs,
                    if (isWarmUpFrame) 0f else currentFps,
                    lastCpuUsage,
                    lastMemoryUsage,
                    lastPowerConsumption,
                    cameraFrameAspectRatio,
                    isWarmUpFrame,
                    if (isWarmUpFrame) 0.0 else convertMs,
                    if (isWarmUpFrame) 0.0 else preprocessMs,
                    if (isWarmUpFrame) 0.0 else postprocessMs,
                    avgKeypointConfidence,
                    validKeypointCount
            )

            // Geometric sanity check setiap 60 frame (hanya saat ada orang)
            if (correctedPerson != null && totalFrameCount % 60 == 0) {
                logSanityCheck(correctedPerson)
            }

            // Throttled logging (setiap 30 frame)
            if (totalFrameCount % 30 == 0) {
                Log.d(
                        TAG,
                        "FPS=%.1f model=%dms total=%dms convert=%.1fms pre=%.1fms post=%.1fms WarmUp=%b".format(
                                currentFps,
                                modelInferenceTimeMs,
                                processingTimeMs,
                                convertMs,
                                preprocessMs,
                                postprocessMs,
                                isWarmUpFrame
                        )
                )
            }

            // Clean up
            letterboxResult?.bitmap?.recycle()
            rotatedBitmap.recycle()
            bitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error during analyze", e)
        } finally {
            imageProxy.close()
        }
    }

    /**
     * Create a letterboxed (padded) bitmap that preserves aspect ratio.
     *
     * Instead of stretching a 480x640 image to 256x256 (which distorts coordinates), we uniformly
     * scale to fit within 256x256 and pad the remaining area with black.
     *
     * Example: 480x640 (3:4 portrait) → scale to 192x256, pad 32px on each side horizontally
     *
     * Returns the letterboxed bitmap plus scaling metadata needed to reverse the transformation on
     * output coordinates.
     */
    private fun createLetterboxedBitmap(
            source: Bitmap,
            targetW: Int,
            targetH: Int
    ): LetterboxResult {
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()

        // Uniform scale factor to fit source within target
        val scale = min(targetW / srcW, targetH / srcH)
        val scaledW = (srcW * scale).toInt()
        val scaledH = (srcH * scale).toInt()

        // Padding to center the scaled image
        val padX = (targetW - scaledW) / 2f
        val padY = (targetH - scaledH) / 2f

        val letterboxed = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(letterboxed)
        canvas.drawColor(Color.BLACK)

        // Single-pass scale+translate via Matrix — avoids allocating an intermediate bitmap
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(padX, padY)
        canvas.drawBitmap(source, matrix, null)

        return LetterboxResult(
                bitmap = letterboxed,
                scale = scale,
                padX = padX,
                padY = padY,
                targetW = targetW.toFloat(),
                targetH = targetH.toFloat(),
                srcW = srcW,
                srcH = srcH
        )
    }

    /**
     * Reverse the letterbox transformation on predicted keypoint coordinates.
     *
     * Model outputs coordinates in the [0, 1] space of the 256x256 letterboxed image. We need to:
     * 1. Remove the padding offset
     * 2. Re-scale to original image proportions
     * 3. Normalize back to [0, 1] relative to the original image dimensions
     */
    private fun correctLetterboxCoordinates(person: Person, letterbox: LetterboxResult): Person {
        val correctedKeypoints =
                person.keypoints.map { kp ->
                    // Convert normalized [0,1] to pixel coords in letterboxed image
                    val pixelX = kp.x * letterbox.targetW
                    val pixelY = kp.y * letterbox.targetH

                    // Remove padding and un-scale
                    val scaledW = letterbox.srcW * letterbox.scale
                    val scaledH = letterbox.srcH * letterbox.scale

                    // Coordinates relative to the scaled (non-padded) region
                    val relX = (pixelX - letterbox.padX) / scaledW
                    val relY = (pixelY - letterbox.padY) / scaledH

                    Keypoint(
                            x = relX.coerceIn(0f, 1f),
                            y = relY.coerceIn(0f, 1f),
                            score = kp.score,
                            label = kp.label
                    )
                }

        return Person(keypoints = correctedKeypoints, score = person.score)
    }

    /**
     * Validasi geometri pose — memastikan keypoint mapping sudah benar.
     *
     * Otomatis memilih set aturan berdasarkan ORIENTASI TUBUH:
     *
     * VERTICAL (berdiri — squat, idle):
     *   nose.y < shoulder.y < hip.y < knee.y < ankle.y
     *
     * HORIZONTAL (push-up / plank — badan memanjang horizontal):
     *   Aturan y-ordering tidak berlaku karena bahu, pinggul, lutut hampir satu garis y.
     *   Sebagai gantinya: cek x-ordering kepala↔kaki (tergantung hadap kiri/kanan).
     *
     * Deteksi orientasi: bandingkan selisih |shoulder.y - ankle.y| (vertikal extent)
     * dengan |shoulder.x - ankle.x| (horizontal extent). Badan horizontal kalau
     * horizontal extent > vertical extent.
     *
     * Hanya di-log setiap 60 frame agar tidak spam.
     */
    private fun logSanityCheck(person: Person) {
        val kp = person.keypoints
        if (kp.size < 17) return

        val minScore = 0.5f
        val violations = mutableListOf<String>()

        fun check(conditionName: String, aVal: Float, bVal: Float, aLabel: String, bLabel: String) {
            if (aVal >= bVal) violations.add("$conditionName: $aLabel(${"%4.2f".format(aVal)}) should be < $bLabel(${"%4.2f".format(bVal)})")
        }

        val lShoulder = kp[5]
        val lHip = kp[11]
        val lAnkle = kp[15]

        // Butuh minimal shoulder + hip untuk menentukan orientasi
        if (lShoulder.score < minScore || lHip.score < minScore) {
            return
        }

        val torsoDy = kotlin.math.abs(lShoulder.y - lHip.y)
        val torsoDx = kotlin.math.abs(lShoulder.x - lHip.x)
        val isHorizontal = torsoDx > torsoDy

        if (isHorizontal) {
            // Mode push-up / plank: cek bahwa bahu, pinggul, pergelangan kaki kira-kira
            // segaris secara y (body line lurus). Toleransi cukup longgar karena ada
            // perspektif kamera.
            if (lAnkle.score >= minScore) {
                val bodyYRange = maxOf(lShoulder.y, lHip.y, lAnkle.y) -
                                 minOf(lShoulder.y, lHip.y, lAnkle.y)
                if (bodyYRange > 0.35f) {
                    violations.add("pushup body not aligned: y-range=${"%.2f".format(bodyYRange)}")
                }
            }
            if (violations.isEmpty()) {
                Log.d(TAG, "✅ Sanity OK [pushup]: frame=$totalFrameCount, score=${"%.2f".format(person.score)}")
            } else {
                Log.w(TAG, "⚠️ Sanity FAIL [pushup] (frame=$totalFrameCount): ${violations.joinToString(" | ")}")
            }
            return
        }

        // Mode berdiri: nose<shoulder<hip<knee<ankle (y=0 adalah atas layar)
        val nose = kp[0]
        val lKnee = kp[13]
        if (nose.score >= minScore && lShoulder.score >= minScore)
            check("nose<shoulder", nose.y, lShoulder.y, "nose.y", "L.sh.y")
        if (lShoulder.score >= minScore && lHip.score >= minScore)
            check("shoulder<hip", lShoulder.y, lHip.y, "L.sh.y", "L.hip.y")
        if (lHip.score >= minScore && lKnee.score >= minScore)
            check("hip<knee", lHip.y, lKnee.y, "L.hip.y", "L.kn.y")
        if (lKnee.score >= minScore && lAnkle.score >= minScore)
            check("knee<ankle", lKnee.y, lAnkle.y, "L.kn.y", "L.ank.y")

        if (violations.isEmpty()) {
            Log.d(TAG, "✅ Sanity OK [standing]: frame=$totalFrameCount, score=${"%.2f".format(person.score)}")
        } else {
            Log.w(TAG, "⚠️ Sanity FAIL [standing] (frame=$totalFrameCount): ${violations.joinToString(" | ")}")
        }
    }

    /** Reset frame counter untuk warm-up baru */
    fun resetWarmUp() {
        totalFrameCount = 0
        frameCount = 0
        lastFpsTimestamp = System.currentTimeMillis()
        currentFps = 0f
    }

    /**
     * Metadata from letterbox operation, needed to reverse the transformation on output keypoint
     * coordinates.
     */
    private data class LetterboxResult(
            val bitmap: Bitmap,
            val scale: Float, // Uniform scale factor applied to source
            val padX: Float, // Horizontal padding (pixels in target space)
            val padY: Float, // Vertical padding (pixels in target space)
            val targetW: Float, // Target width (e.g. 256)
            val targetH: Float, // Target height (e.g. 256)
            val srcW: Float, // Original source width
            val srcH: Float // Original source height
    )
}

/**
 * Extension: Convert ImageProxy (YUV_420_888) to Bitmap.
 *
 * Menggunakan CameraX 1.3+ built-in toBitmap() yang:
 *   - Pakai libyuv untuk konversi YUV→RGB langsung (tanpa JPEG encode/decode).
 *   - Lossless: tidak ada degradasi kualitas gambar yang diterima model.
 *   - Hemat ~5–15ms per frame dibanding jalur YUV→NV21→JPEG→Bitmap sebelumnya.
 *   - Handle semua variasi stride/pixelStride secara internal.
 */
private fun ImageProxy.toBitmapCustom(): Bitmap = toBitmap()

/** Extension: Rotate bitmap by degrees */
private fun Bitmap.rotate(degrees: Float): Bitmap {
    if (degrees == 0f) return this
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
