package com.application.skripsiarvan.domain.model

import kotlin.math.sqrt

/**
 * Data class untuk menyimpan semua metrik benchmark per-frame.
 * Sesuai dengan variabel terikat pada skripsi.
 */
data class BenchmarkMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val modelType: String,
    val delegateType: String,
    val inferenceTimeMs: Long,       // hanya TFLite detectPose() — metrik utama skripsi
    val processingTimeMs: Long = 0L, // end-to-end termasuk preprocessing (YUV→Bitmap, letterbox)
    val fps: Float,
    val cpuUsagePercent: Float,
    val memoryUsageMb: Float,
    val powerConsumptionMw: Float,
    val exerciseType: String = "NONE",
    val repetitionCount: Int = 0,
    val frameNumber: Int = 0,
    // Apakah pose berhasil terdeteksi di frame ini — untuk hitung detection rate
    val poseDetected: Boolean = false
) {
    fun toCsvLine(): String {
        return "$timestamp,$modelType,$delegateType,$inferenceTimeMs,$processingTimeMs,$fps," +
               "$cpuUsagePercent,$memoryUsageMb,$powerConsumptionMw," +
               "$exerciseType,$repetitionCount,$frameNumber,${if (poseDetected) 1 else 0}"
    }

    companion object {
        fun getCsvHeader(): String {
            return "timestamp,model_type,delegate_type,inference_time_ms,processing_time_ms,fps," +
                   "cpu_usage_percent,memory_usage_mb,power_consumption_mw," +
                   "exercise_type,repetition_count,frame_number,pose_detected"
        }
    }
}

/**
 * Data class untuk informasi penggunaan memori
 */
data class MemoryInfo(
    val usedMemoryMb: Float,
    val totalMemoryMb: Float,
    val nativeHeapMb: Float,
    val javaHeapMb: Float
)

/**
 * Data class untuk informasi penggunaan CPU
 */
data class CpuInfo(
    val usagePercent: Float,
    val userTime: Long,
    val systemTime: Long,
    val idleTime: Long
)
