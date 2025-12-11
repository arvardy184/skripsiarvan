package com.application.skripsiarvan.data.ml

import android.content.Context
import android.util.Log
import com.application.skripsiarvan.domain.model.DelegateType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
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
    private var nnApiDelegate: NnApiDelegate? = null

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
                DelegateType.CPU -> {
                    // Default CPU with XNNPACK optimization
                    options.setNumThreads(NUM_THREADS)
                    options.setUseXNNPACK(true)
                    Log.d(TAG, "Using CPU delegate with XNNPACK")
                }

                DelegateType.GPU -> {
                    // Check GPU compatibility
                    val compatibilityList = CompatibilityList()
                    isGpuCompatible = compatibilityList.isDelegateSupportedOnThisDevice

                    if (isGpuCompatible) {
                        val delegateOptions = compatibilityList.bestOptionsForThisDevice
                        gpuDelegate = GpuDelegate(delegateOptions)
                        options.addDelegate(gpuDelegate)
                        Log.d(TAG, "Using GPU delegate")
                    } else {
                        // Fallback to CPU
                        Log.w(TAG, "GPU not compatible, falling back to CPU")
                        options.setNumThreads(NUM_THREADS)
                        options.setUseXNNPACK(true)
                    }
                }

                DelegateType.NNAPI -> {
                    // NNAPI delegate for NPU/DSP acceleration
                    nnApiDelegate = NnApiDelegate()
                    options.addDelegate(nnApiDelegate)
                    Log.d(TAG, "Using NNAPI delegate")
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

        nnApiDelegate?.close()
        nnApiDelegate = null

        Log.d(TAG, "Interpreter and delegates closed")
    }

    fun getInterpreter(): Interpreter? = interpreter
}
