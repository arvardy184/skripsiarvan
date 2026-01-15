package com.application.skripsiarvan.data.ml

import android.content.Context
import android.util.Log
import com.application.skripsiarvan.domain.model.DelegateType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Helper class for TensorFlow Lite model loading and delegate management
 */
class TFLiteHelper(
    private val context: Context,
    private val modelFileName: String,
    private val delegateType: DelegateType
) {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    companion object {
        private const val TAG = "TFLiteHelper"
        private const val NUM_THREADS = 4
    }

    /**
     * Initialize interpreter with selected delegate
     * @return Pair of (Interpreter, isGpuCompatible flag)
     */
    fun initializeInterpreter(): Pair<Interpreter?, Boolean> {
        try {
            val modelBuffer = loadModelFile(modelFileName)
            val options = Interpreter.Options()

            var isGpuCompatible = false

            when (delegateType) {
                DelegateType.CPU_BASELINE -> {
                    // Level 1: TFLite default kernel WITHOUT XNNPACK optimization
                    // This is the baseline for performance comparison
                    options.setNumThreads(NUM_THREADS)
                    options.setUseXNNPACK(false)  // Explicitly disable XNNPACK
                    Log.d(TAG, "Level 1: Using CPU Baseline (no XNNPACK optimization)")
                }

                DelegateType.CPU_XNNPACK -> {
                    // Level 2: CPU with XNNPACK optimization (SIMD)
                    // Shows improvement from software optimization
                    options.setNumThreads(NUM_THREADS)
                    options.setUseXNNPACK(true)
                    Log.d(TAG, "Level 2: Using CPU with XNNPACK (SIMD optimization)")
                }

                DelegateType.GPU -> {
                    // Level 3: Hardware acceleration via GPU
                    // Check GPU compatibility first
                    val compatibilityList = CompatibilityList()
                    isGpuCompatible = compatibilityList.isDelegateSupportedOnThisDevice

                    if (isGpuCompatible) {
                        gpuDelegate = GpuDelegate()
                        options.addDelegate(gpuDelegate)
                        Log.d(TAG, "Level 3: Using GPU Delegate (OpenGL/OpenCL)")
                    } else {
                        // Fallback to CPU XNNPACK if GPU not available
                        Log.w(TAG, "GPU not compatible, falling back to CPU XNNPACK")
                        options.setNumThreads(NUM_THREADS)
                        options.setUseXNNPACK(true)
                    }
                }
            }

            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "Interpreter initialized successfully")

            return Pair(interpreter, isGpuCompatible)

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing interpreter", e)
            return Pair(null, false)
        }
    }

    /**
     * Load TFLite model from assets folder
     */
    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Clean up resources and delegates
     */
    fun close() {
        interpreter?.close()
        interpreter = null

        gpuDelegate?.close()
        gpuDelegate = null

        Log.d(TAG, "Interpreter and delegates closed")
    }

    fun getInterpreter(): Interpreter? = interpreter
}
