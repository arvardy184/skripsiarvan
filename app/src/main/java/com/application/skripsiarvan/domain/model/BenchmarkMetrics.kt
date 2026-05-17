package com.application.skripsiarvan.domain.model

import android.os.Build

/**
 * Data class untuk menyimpan semua metrik benchmark per-frame.
 * Sesuai dengan variabel terikat pada skripsi.
 */
data class BenchmarkMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val modelType: String,
    val selectedDelegateType: String,
    val effectiveDelegateType: String,
    val inferenceTimeMs: Long,
    val processingTimeMs: Long = 0L,
    val fps: Float,
    val cpuUsagePercent: Float,
    val memoryUsageMb: Float,
    val powerConsumptionMw: Float,
    val exerciseType: String = "NONE",
    val repetitionCount: Int = 0,
    val frameNumber: Int = 0,
    val poseDetected: Boolean = false,
    val sessionLabel: String = "",
    val deviceName: String = Build.MODEL,
    val convertMs: Double = 0.0,
    val preprocessMs: Double = 0.0,
    val postprocessMs: Double = 0.0,
    val avgKeypointConfidence: Float = 0f,
    val validKeypointCount: Int = 0
) {
    fun toCsvLine(): String {
        return "$timestamp,$modelType,$selectedDelegateType,$effectiveDelegateType," +
               "$inferenceTimeMs,$processingTimeMs,$fps," +
               "$cpuUsagePercent,$memoryUsageMb,$powerConsumptionMw," +
               "$exerciseType,$repetitionCount,$frameNumber,${if (poseDetected) 1 else 0},$sessionLabel," +
               "$deviceName," +
               "${"%.3f".format(convertMs)},${"%.3f".format(preprocessMs)},${"%.3f".format(postprocessMs)}," +
               "${"%.4f".format(avgKeypointConfidence)},$validKeypointCount"
    }

    companion object {
        fun getCsvHeader(): String {
            return "timestamp,model_type,selected_delegate_type,effective_delegate_type," +
                   "inference_time_ms,processing_time_ms,fps," +
                   "cpu_usage_percent,memory_usage_mb,power_consumption_mw," +
                   "exercise_type,repetition_count,frame_number,pose_detected,session_label," +
                   "device_name," +
                   "convert_ms,preprocess_ms,postprocess_ms," +
                   "avg_keypoint_confidence,valid_keypoint_count"
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
