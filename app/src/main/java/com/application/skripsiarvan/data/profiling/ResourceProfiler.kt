package com.application.skripsiarvan.data.profiling

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Debug
import android.os.Process
import android.util.Log
import com.application.skripsiarvan.domain.model.CpuInfo
import com.application.skripsiarvan.domain.model.MemoryInfo
import java.io.RandomAccessFile

/**
 * Helper class untuk mengukur penggunaan sumber daya sistem
 * Sesuai dengan Bagian 4.5.2 Strategi Profiling Sumber Daya di skripsi
 * 
 * Mengukur:
 * - CPU Usage (%)
 * - Memory Usage (MB) 
 * - Power Consumption (mW)
 */
class ResourceProfiler(private val context: Context) {

    private var lastCpuTime: Long = 0
    private var lastAppCpuTime: Long = 0
    private var lastCpuInfo: CpuInfo? = null

    companion object {
        private const val TAG = "ResourceProfiler"
    }

    /**
     * Mengukur penggunaan CPU aplikasi dalam persen
     * Membaca dari /proc/stat dan /proc/[pid]/stat
     * 
     * Formula: CPU Usage (%) = (Total Time - Idle Time) / Total Time × 100%
     */
    fun getCpuUsage(): Float {
        try {
            val pid = Process.myPid()
            
            // Baca total CPU time dari /proc/stat
            val statReader = RandomAccessFile("/proc/stat", "r")
            val cpuLine = statReader.readLine()
            statReader.close()

            // Parse CPU times: user, nice, system, idle, iowait, irq, softirq
            val cpuParts = cpuLine.split("\\s+".toRegex())
            val userTime = cpuParts[1].toLong()
            val niceTime = cpuParts[2].toLong()
            val systemTime = cpuParts[3].toLong()
            val idleTime = cpuParts[4].toLong()
            val ioWait = cpuParts[5].toLong()
            val irq = cpuParts[6].toLong()
            val softIrq = cpuParts[7].toLong()

            val totalCpuTime = userTime + niceTime + systemTime + idleTime + ioWait + irq + softIrq

            // Baca app CPU time dari /proc/[pid]/stat
            val appStatReader = RandomAccessFile("/proc/$pid/stat", "r")
            val appStatLine = appStatReader.readLine()
            appStatReader.close()

            val appParts = appStatLine.split("\\s+".toRegex())
            val appUserTime = appParts[13].toLong()  // utime
            val appSystemTime = appParts[14].toLong() // stime
            val appCpuTime = appUserTime + appSystemTime

            // Hitung delta
            val cpuTimeDelta = totalCpuTime - lastCpuTime
            val appCpuTimeDelta = appCpuTime - lastAppCpuTime

            // Update last values
            lastCpuTime = totalCpuTime
            lastAppCpuTime = appCpuTime

            // Hitung persentase
            return if (cpuTimeDelta > 0) {
                (appCpuTimeDelta.toFloat() / cpuTimeDelta.toFloat()) * 100f * Runtime.getRuntime().availableProcessors()
            } else {
                0f
            }.coerceIn(0f, 100f)

        } catch (e: Exception) {
            Log.e(TAG, "Error reading CPU usage", e)
            return 0f
        }
    }

    /**
     * Mengukur penggunaan memori aplikasi
     * Menggunakan Debug.MemoryInfo API
     */
    fun getMemoryUsage(): MemoryInfo {
        try {
            val memoryInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memoryInfo)

            // Get PSS (Proportional Set Size) - memory yang benar-benar digunakan app
            val totalPssMb = memoryInfo.totalPss / 1024f  // Convert KB to MB

            // Get native heap
            val nativeHeapMb = Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)

            // Get Java heap
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val javaHeapMb = usedMemory / (1024f * 1024f)

            // Get total available memory
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalMemoryMb = memInfo.totalMem / (1024f * 1024f)

            return MemoryInfo(
                usedMemoryMb = totalPssMb,
                totalMemoryMb = totalMemoryMb,
                nativeHeapMb = nativeHeapMb,
                javaHeapMb = javaHeapMb
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error reading memory usage", e)
            return MemoryInfo(0f, 0f, 0f, 0f)
        }
    }

    /**
     * Mengukur konsumsi daya baterai
     * Menggunakan BatteryManager API
     * 
     * @return Estimasi konsumsi daya dalam mW (milliwatt)
     */
    fun getPowerConsumption(): Float {
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            // Get battery current in microamperes (uA)
            val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

            // Get battery voltage
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

            // Calculate power: P = V * I
            // currentNow is in microamperes (uA), voltage is in millivolts (mV)
            // Power = (voltage/1000 V) * (current/1000000 A) = voltage * current / 1000000000 W
            // Convert to mW: multiply by 1000
            val powerMw = kotlin.math.abs((voltage.toFloat() * currentNow.toFloat()) / 1_000_000f)

            return powerMw

        } catch (e: Exception) {
            Log.e(TAG, "Error reading power consumption", e)
            return 0f
        }
    }

    /**
     * Mendapatkan semua metrik sumber daya sekaligus
     */
    fun getAllMetrics(): Triple<Float, MemoryInfo, Float> {
        val cpuUsage = getCpuUsage()
        val memoryInfo = getMemoryUsage()
        val powerConsumption = getPowerConsumption()
        return Triple(cpuUsage, memoryInfo, powerConsumption)
    }
}
