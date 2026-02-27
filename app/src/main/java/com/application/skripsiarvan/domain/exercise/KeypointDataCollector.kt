package com.application.skripsiarvan.domain.exercise

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.application.skripsiarvan.domain.model.Person
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Collects labeled keypoint data from live pose detection for ML training.
 *
 * Usage flow:
 * 1. User selects a phase label (e.g., "STANDING")
 * 2. User starts recording
 * 3. User performs the exercise pose for that phase
 * 4. System records every frame's keypoints with the selected label
 * 5. User stops recording and switches to next phase
 * 6. Repeat for all phases
 * 7. Export to CSV for use in train_classifier.py
 *
 * CSV format: label, kp0_x, kp0_y, kp1_x, kp1_y, ..., kp16_x, kp16_y
 */
class KeypointDataCollector(private val context: Context) {

    companion object {
        private const val TAG = "KeypointDataCollector"
        private const val NUM_KEYPOINTS = 17
        private const val FILE_PREFIX = "accelpose_training_data_"
    }

    data class LabeledSample(val label: String, val features: FloatArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as LabeledSample
            return label == other.label && features.contentEquals(other.features)
        }
        override fun hashCode(): Int = 31 * label.hashCode() + features.contentHashCode()
    }

    private val dataQueue = ConcurrentLinkedQueue<LabeledSample>()
    private var isCollecting = false
    private var currentLabel = "UNKNOWN"

    /** Start collecting data with the given phase label. */
    fun startCollecting(label: String) {
        currentLabel = label
        isCollecting = true
        Log.d(TAG, "Γû╢ Started collecting for label: $label")
    }

    /** Stop collecting data. */
    fun stopCollecting() {
        isCollecting = false
        Log.d(TAG, "ΓÅ╣ Stopped collecting. Total samples: ${dataQueue.size}")
    }

    /** Record a frame's keypoints if collecting is active. */
    fun recordFrame(person: Person?) {
        if (!isCollecting || person == null) return
        if (person.keypoints.size < NUM_KEYPOINTS) return

        // Check minimum confidence
        val avgConf = person.keypoints.take(NUM_KEYPOINTS).map { it.score }.average()
        if (avgConf < 0.3f) return

        val features = FloatArray(NUM_KEYPOINTS * 2)
        for (i in 0 until NUM_KEYPOINTS) {
            features[i * 2] = person.keypoints[i].x
            features[i * 2 + 1] = person.keypoints[i].y
        }

        dataQueue.add(LabeledSample(currentLabel, features))
    }

    /** Get the total number of collected samples. */
    fun getSampleCount(): Int = dataQueue.size

    /** Get sample distribution by label. */
    fun getLabelDistribution(): Map<String, Int> {
        return dataQueue.groupBy { it.label }.mapValues { it.value.size }
    }

    /** Export collected data to CSV file. */
    fun exportToCsv(): String? {
        if (dataQueue.isEmpty()) {
            Log.w(TAG, "No data to export")
            return null
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${FILE_PREFIX}${timestamp}.csv"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportWithMediaStore(fileName)
            } else {
                exportToDownloads(fileName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Export error: ${e.message}", e)
            null
        }
    }

    private fun exportWithMediaStore(fileName: String): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

        val contentValues =
                ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

        val resolver = context.contentResolver
        val uri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        ?: return null

        resolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { writer -> writeCsvContent(writer) }
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        Log.d(TAG, "Γ£à Exported ${dataQueue.size} samples to $fileName")
        return uri.toString()
    }

    private fun exportToDownloads(fileName: String): String? {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, fileName)

        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos).use { writer -> writeCsvContent(writer) }
        }

        Log.d(TAG, "Γ£à Exported ${dataQueue.size} samples to ${file.absolutePath}")
        return file.absolutePath
    }

    private fun writeCsvContent(writer: OutputStreamWriter) {
        // Header
        val header = buildString {
            append("label")
            for (i in 0 until NUM_KEYPOINTS) {
                append(",kp${i}_x,kp${i}_y")
            }
        }
        writer.write(header)
        writer.write("\n")

        // Data rows
        dataQueue.forEach { sample ->
            val row = buildString {
                append(sample.label)
                sample.features.forEach { f -> append(",${String.format("%.6f", f)}") }
            }
            writer.write(row)
            writer.write("\n")
        }
    }

    /** Clear all collected data. */
    fun clearData() {
        dataQueue.clear()
        Log.d(TAG, "Data cleared")
    }

    fun isCurrentlyCollecting(): Boolean = isCollecting
    fun getCurrentLabel(): String = currentLabel
}
