package com.application.skripsiarvan.data.logging

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.application.skripsiarvan.domain.model.BenchmarkMetrics
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Class untuk logging dan export data benchmark
 * Sesuai dengan Tabel 4.1 Kebutuhan Fungsional - Pencatatan Data Otomatis
 * 
 * "Sistem harus dapat menyimpan data metrik performa (Latensi, FPS, CPU Load) 
 * ke dalam format log (CSV) untuk keperluan analisis statistik lebih lanjut."
 */
class BenchmarkLogger(private val context: Context) {

    companion object {
        private const val TAG = "BenchmarkLogger"
        private const val FILE_PREFIX = "accelpose_benchmark_"
        private const val FILE_EXTENSION = ".csv"
    }

    // Thread-safe queue untuk menyimpan metrik
    private val metricsQueue = ConcurrentLinkedQueue<BenchmarkMetrics>()
    
    private var isLogging = false
    private var startTime: Long = 0
    private var frameCounter = 0

    /**
     * Memulai sesi logging baru
     */
    fun startLogging() {
        metricsQueue.clear()
        frameCounter = 0
        startTime = System.currentTimeMillis()
        isLogging = true
        Log.d(TAG, "Benchmark logging started")
    }

    /**
     * Menghentikan sesi logging
     */
    fun stopLogging() {
        isLogging = false
        Log.d(TAG, "Benchmark logging stopped. Total frames: $frameCounter")
    }

    /**
     * Menambahkan metrik ke queue jika logging aktif
     */
    fun logMetrics(metrics: BenchmarkMetrics) {
        if (!isLogging) return
        
        frameCounter++
        val metricsWithFrame = metrics.copy(frameNumber = frameCounter)
        metricsQueue.add(metricsWithFrame)
    }

    /**
     * Mengecek apakah sedang logging
     */
    fun isCurrentlyLogging(): Boolean = isLogging

    /**
     * Mendapatkan jumlah frame yang ter-log
     */
    fun getLoggedFrameCount(): Int = metricsQueue.size

    /**
     * Mendapatkan durasi logging dalam detik
     */
    fun getLoggingDurationSeconds(): Long {
        return if (isLogging) {
            (System.currentTimeMillis() - startTime) / 1000
        } else {
            0
        }
    }

    /**
     * Export semua data ke file CSV
     * Menyimpan ke folder Downloads
     * 
     * @return Path file yang disimpan, null jika gagal
     */
    fun exportToCsv(): String? {
        if (metricsQueue.isEmpty()) {
            Log.w(TAG, "No data to export")
            return null
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$FILE_PREFIX$timestamp$FILE_EXTENSION"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ menggunakan MediaStore
                exportWithMediaStore(fileName)
            } else {
                // Android 9 dan sebelumnya
                exportToDownloadsFolder(fileName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting CSV", e)
            null
        }
    }

    /**
     * Export menggunakan MediaStore API untuk Android 10+
     */
    private fun exportWithMediaStore(fileName: String): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        resolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                // Write header
                writer.write(BenchmarkMetrics.getCsvHeader())
                writer.write("\n")

                // Write all metrics
                metricsQueue.forEach { metrics ->
                    writer.write(metrics.toCsvLine())
                    writer.write("\n")
                }
            }
        }

        // Mark as complete
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        Log.d(TAG, "Exported ${metricsQueue.size} records to $fileName")
        return uri.toString()
    }

    /**
     * Export ke folder Downloads untuk Android 9 dan sebelumnya
     */
    private fun exportToDownloadsFolder(fileName: String): String? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos).use { writer ->
                // Write header
                writer.write(BenchmarkMetrics.getCsvHeader())
                writer.write("\n")

                // Write all metrics
                metricsQueue.forEach { metrics ->
                    writer.write(metrics.toCsvLine())
                    writer.write("\n")
                }
            }
        }

        Log.d(TAG, "Exported ${metricsQueue.size} records to ${file.absolutePath}")
        return file.absolutePath
    }

    /**
     * Menghapus semua data yang sudah ter-log
     */
    fun clearLog() {
        metricsQueue.clear()
        frameCounter = 0
        Log.d(TAG, "Log cleared")
    }

    /**
     * Mendapatkan statistik ringkasan dari data yang ter-log
     */
    fun getSummaryStatistics(): BenchmarkSummary? {
        if (metricsQueue.isEmpty()) return null

        val metrics = metricsQueue.toList()
        
        return BenchmarkSummary(
            totalFrames = metrics.size,
            avgInferenceTimeMs = metrics.map { it.inferenceTimeMs }.average(),
            avgFps = metrics.map { it.fps.toDouble() }.average().toFloat(),
            avgCpuUsage = metrics.map { it.cpuUsagePercent.toDouble() }.average().toFloat(),
            avgMemoryUsageMb = metrics.map { it.memoryUsageMb.toDouble() }.average().toFloat(),
            avgPowerConsumptionMw = metrics.map { it.powerConsumptionMw.toDouble() }.average().toFloat(),
            minInferenceTimeMs = metrics.minOf { it.inferenceTimeMs },
            maxInferenceTimeMs = metrics.maxOf { it.inferenceTimeMs }
        )
    }
}

/**
 * Data class untuk statistik ringkasan benchmark
 */
data class BenchmarkSummary(
    val totalFrames: Int,
    val avgInferenceTimeMs: Double,
    val avgFps: Float,
    val avgCpuUsage: Float,
    val avgMemoryUsageMb: Float,
    val avgPowerConsumptionMw: Float,
    val minInferenceTimeMs: Long,
    val maxInferenceTimeMs: Long
)
