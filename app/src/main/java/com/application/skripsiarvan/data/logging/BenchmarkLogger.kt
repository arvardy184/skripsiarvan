package com.application.skripsiarvan.data.logging

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.application.skripsiarvan.domain.model.BenchmarkMetrics
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.sqrt

/**
 * Class untuk logging dan export data benchmark.
 * Sesuai dengan Tabel 4.1 Kebutuhan Fungsional — Pencatatan Data Otomatis.
 *
 * Thread-safe: diakses dari camera thread (logMetrics) dan UI thread (start/stop/export).
 * - isLogging   → AtomicBoolean (volatile read/write tanpa lock)
 * - frameCounter → AtomicInteger (increment atomic)
 * - startTime   → AtomicLong
 * - endTime     → AtomicLong (dicatat saat stopLogging agar durasi tersedia di summary)
 */
class BenchmarkLogger(private val context: Context) {

    companion object {
        private const val TAG = "BenchmarkLogger"
        private const val FILE_PREFIX = "accelpose_benchmark_"
        private const val FILE_EXTENSION = ".csv"
    }

    private val metricsQueue = ConcurrentLinkedQueue<BenchmarkMetrics>()

    private val isLogging = AtomicBoolean(false)
    private val frameCounter = AtomicInteger(0)
    private val startTime = AtomicLong(0L)
    private val endTime = AtomicLong(0L)

    @Volatile private var currentSessionLabel: String = ""

    /**
     * Mulai logging dengan label sesi opsional.
     * Label digunakan untuk mengidentifikasi kombinasi benchmark di CSV
     * (misal: "MoveNet_XNNPACK_Squat").
     */
    fun startLogging(sessionLabel: String = "") {
        metricsQueue.clear()
        frameCounter.set(0)
        startTime.set(System.currentTimeMillis())
        endTime.set(0L)
        currentSessionLabel = sessionLabel
        isLogging.set(true)
        Log.d(TAG, "Benchmark logging started [session=$sessionLabel]")
    }

    fun stopLogging() {
        isLogging.set(false)
        endTime.set(System.currentTimeMillis())
        Log.d(TAG, "Benchmark logging stopped [session=$currentSessionLabel] Total frames: ${frameCounter.get()}")
    }

    fun getCurrentSessionLabel(): String = currentSessionLabel

    fun logMetrics(metrics: BenchmarkMetrics) {
        if (!isLogging.get()) return
        val frame = frameCounter.incrementAndGet()
        metricsQueue.add(metrics.copy(frameNumber = frame, sessionLabel = currentSessionLabel))
    }

    fun isCurrentlyLogging(): Boolean = isLogging.get()

    fun getLoggedFrameCount(): Int = metricsQueue.size

    fun getLoggingDurationSeconds(): Long {
        val start = startTime.get()
        if (start == 0L) return 0L
        val end = if (isLogging.get()) System.currentTimeMillis() else endTime.get()
        return (end - start) / 1000
    }

    fun exportToCsv(): String? {
        if (metricsQueue.isEmpty()) {
            Log.w(TAG, "No data to export")
            return null
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$FILE_PREFIX$timestamp$FILE_EXTENSION"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportWithMediaStore(fileName)
            } else {
                exportToDownloadsFolder(fileName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting CSV", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportWithMediaStore(fileName: String): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        resolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(BenchmarkMetrics.getCsvHeader())
                writer.write("\n")
                metricsQueue.forEach { metrics ->
                    writer.write(metrics.toCsvLine())
                    writer.write("\n")
                }
            }
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        Log.d(TAG, "Exported ${metricsQueue.size} records to $fileName")
        return uri.toString()
    }

    private fun exportToDownloadsFolder(fileName: String): String? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos).use { writer ->
                writer.write(BenchmarkMetrics.getCsvHeader())
                writer.write("\n")
                metricsQueue.forEach { metrics ->
                    writer.write(metrics.toCsvLine())
                    writer.write("\n")
                }
            }
        }

        Log.d(TAG, "Exported ${metricsQueue.size} records to ${file.absolutePath}")
        return file.absolutePath
    }

    fun clearLog() {
        metricsQueue.clear()
        frameCounter.set(0)
        Log.d(TAG, "Log cleared")
    }

    /**
     * Hitung statistik ringkasan dari semua frame yang ter-log.
     *
     * Metrik yang dihitung (sesuai kebutuhan analisis statistik skripsi):
     * - Rata-rata, minimum, maksimum, dan STANDAR DEVIASI latensi inferensi
     * - Rata-rata FPS, CPU, memori, daya
     * - Detection rate: % frame di mana pose berhasil terdeteksi
     * - Durasi sesi logging
     */
    fun getSummaryStatistics(): BenchmarkSummary? {
        if (metricsQueue.isEmpty()) return null

        val metrics = metricsQueue.toList()
        val latencies = metrics.map { it.inferenceTimeMs.toDouble() }

        val avgLatency = latencies.average()
        val stddevLatency = if (latencies.size > 1) {
            val variance = latencies.map { (it - avgLatency) * (it - avgLatency) }.average()
            sqrt(variance)
        } else 0.0

        val detectedFrames = metrics.count { it.poseDetected }
        val detectionRate = if (metrics.isNotEmpty()) detectedFrames.toFloat() / metrics.size else 0f

        val start = startTime.get()
        val end = if (endTime.get() > 0L) endTime.get() else System.currentTimeMillis()
        val durationMs = if (start > 0L) end - start else 0L

        return BenchmarkSummary(
            totalFrames = metrics.size,
            avgInferenceTimeMs = avgLatency,
            stddevInferenceTimeMs = stddevLatency,
            minInferenceTimeMs = latencies.minOrNull()?.toLong() ?: 0L,
            maxInferenceTimeMs = latencies.maxOrNull()?.toLong() ?: 0L,
            avgFps = metrics.map { it.fps.toDouble() }.average().toFloat(),
            avgCpuUsage = metrics.map { it.cpuUsagePercent.toDouble() }.average().toFloat(),
            avgMemoryUsageMb = metrics.map { it.memoryUsageMb.toDouble() }.average().toFloat(),
            avgPowerConsumptionMw = metrics.map { it.powerConsumptionMw.toDouble() }.average().toFloat(),
            detectionRate = detectionRate,
            durationMs = durationMs
        )
    }
}

/**
 * Statistik ringkasan satu sesi benchmark.
 * Stddev latensi wajib ada untuk uji statistik inferensial (ANOVA/t-test) di skripsi.
 */
data class BenchmarkSummary(
    val totalFrames: Int,
    val avgInferenceTimeMs: Double,
    val stddevInferenceTimeMs: Double,   // Wajib untuk uji statistik skripsi
    val minInferenceTimeMs: Long,
    val maxInferenceTimeMs: Long,
    val avgFps: Float,
    val avgCpuUsage: Float,
    val avgMemoryUsageMb: Float,
    val avgPowerConsumptionMw: Float,
    val detectionRate: Float,            // 0.0–1.0: % frame pose terdeteksi
    val durationMs: Long                 // Durasi sesi logging (ms)
)
