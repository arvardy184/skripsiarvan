package com.application.skripsiarvan.presentation.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.skripsiarvan.data.detector.BlazePoseDetector
import com.application.skripsiarvan.data.detector.MoveNetDetector
import com.application.skripsiarvan.data.logging.BenchmarkLogger
import com.application.skripsiarvan.data.logging.BenchmarkSummary
import com.application.skripsiarvan.data.ml.TFLiteHelper
import com.application.skripsiarvan.data.profiling.ResourceProfiler
import com.application.skripsiarvan.domain.detector.PoseDetector
import com.application.skripsiarvan.domain.exercise.ExerciseDetector
import com.application.skripsiarvan.domain.exercise.PushUpDetector
import com.application.skripsiarvan.domain.exercise.SquatDetector
import com.application.skripsiarvan.domain.model.BenchmarkMetrics
import com.application.skripsiarvan.domain.model.DelegateType
import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.ExerciseType
import com.application.skripsiarvan.domain.model.ModelType
import com.application.skripsiarvan.domain.model.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State untuk layar pose detection
 * Diperluas dengan metrik sumber daya dan exercise detection sesuai skripsi
 */
data class PoseUiState(
    // Model & Delegate selection
    val selectedModel: ModelType = ModelType.MOVENET_LIGHTNING,
    val selectedDelegate: DelegateType = DelegateType.CPU_BASELINE,
    
    // Pose detection results
    val detectedPerson: Person? = null,
    
    // Performance metrics (Variabel Terikat)
    val inferenceTime: Long = 0L,           // Latensi inferensi (ms)
    val fps: Float = 0f,                     // Throughput (FPS)
    val cpuUsage: Float = 0f,                // Utilisasi CPU (%)
    val memoryUsage: Float = 0f,             // Penggunaan memori (MB)
    val powerConsumption: Float = 0f,        // Konsumsi daya (mW)
    
    // Exercise detection
    val selectedExercise: ExerciseType = ExerciseType.NONE,
    val repetitionCount: Int = 0,
    val currentAngle: Double = 0.0,
    val exerciseState: ExerciseState = ExerciseState.IDLE,
    
    // Logging state
    val isLogging: Boolean = false,
    val loggedFrameCount: Int = 0,
    val loggingDurationSeconds: Long = 0,
    
    // Warm-up state (Bagian 4.5.1 skripsi)
    val isWarmUpComplete: Boolean = false,
    val warmUpFramesRemaining: Int = 5,
    
    // System state
    val isInitialized: Boolean = false,
    val errorMessage: String? = null,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    
    // Export state
    val lastExportPath: String? = null,
    val benchmarkSummary: BenchmarkSummary? = null
)

/**
 * Device information for display
 */
data class DeviceInfo(
    val model: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    val chipset: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Build.SOC_MODEL
    } else {
        "Unknown"
    }
)

/**
 * ViewModel managing pose detection state, profiling, and exercise detection
 */
class PoseViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PoseUiState())
    val uiState: StateFlow<PoseUiState> = _uiState.asStateFlow()

    private var tfliteHelper: TFLiteHelper? = null
    private var currentDetector: PoseDetector? = null
    
    // New components
    private val resourceProfiler = ResourceProfiler(application)
    private val benchmarkLogger = BenchmarkLogger(application)
    private var exerciseDetector: ExerciseDetector? = null

    init {
        initializeDetector()
    }

    /**
     * Initialize detector with current model and delegate selection
     */
    fun initializeDetector() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Clean up previous resources
                currentDetector?.close()
                tfliteHelper?.close()

                val currentState = _uiState.value

                // Reset warm-up
                _uiState.value = currentState.copy(
                    isWarmUpComplete = false,
                    warmUpFramesRemaining = 5
                )

                // Create TFLite helper with selected delegate
                tfliteHelper = TFLiteHelper(
                    context = getApplication(),
                    modelFileName = currentState.selectedModel.fileName,
                    delegateType = currentState.selectedDelegate
                )

                val (interpreter, isGpuCompatible) = tfliteHelper!!.initializeInterpreter()

                if (interpreter == null) {
                    _uiState.value = currentState.copy(
                        isInitialized = false,
                        errorMessage = "Failed to initialize TFLite interpreter"
                    )
                    return@launch
                }

                // Create appropriate detector based on model type
                currentDetector = when (currentState.selectedModel) {
                    ModelType.MOVENET_LIGHTNING -> MoveNetDetector(interpreter)
                    ModelType.BLAZEPOSE_LITE -> BlazePoseDetector(interpreter)
                }

                // Initialize exercise detector if selected
                updateExerciseDetector(currentState.selectedExercise)

                _uiState.value = currentState.copy(
                    isInitialized = true,
                    isWarmUpComplete = false,
                    warmUpFramesRemaining = 5,
                    errorMessage = if (!isGpuCompatible && currentState.selectedDelegate == DelegateType.GPU) {
                        "GPU not compatible, falling back to CPU XNNPACK"
                    } else null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isInitialized = false,
                    errorMessage = "Initialization error: ${e.message}"
                )
            }
        }
    }

    /**
     * Update exercise detector based on type
     */
    private fun updateExerciseDetector(type: ExerciseType) {
        exerciseDetector = when (type) {
            ExerciseType.NONE -> null
            ExerciseType.SQUAT -> SquatDetector()
            ExerciseType.PUSH_UP -> PushUpDetector()
        }
    }

    /**
     * Update selected model and reinitialize detector
     */
    fun selectModel(modelType: ModelType) {
        if (_uiState.value.selectedModel != modelType) {
            _uiState.value = _uiState.value.copy(
                selectedModel = modelType,
                isInitialized = false
            )
            initializeDetector()
        }
    }

    /**
     * Update selected delegate and reinitialize detector
     */
    fun selectDelegate(delegateType: DelegateType) {
        if (_uiState.value.selectedDelegate != delegateType) {
            _uiState.value = _uiState.value.copy(
                selectedDelegate = delegateType,
                isInitialized = false
            )
            initializeDetector()
        }
    }

    /**
     * Update selected exercise type
     */
    fun selectExercise(exerciseType: ExerciseType) {
        if (_uiState.value.selectedExercise != exerciseType) {
            updateExerciseDetector(exerciseType)
            exerciseDetector?.reset()
            _uiState.value = _uiState.value.copy(
                selectedExercise = exerciseType,
                repetitionCount = 0,
                currentAngle = 0.0,
                exerciseState = ExerciseState.IDLE
            )
        }
    }

    /**
     * Update detection results from camera analyzer
     * Now includes resource metrics and exercise detection
     */
    fun updateResults(
        person: Person?,
        inferenceTime: Long,
        fps: Float,
        cpuUsage: Float,
        memoryUsage: Float,
        powerConsumption: Float,
        isWarmUpFrame: Boolean
    ) {
        val currentState = _uiState.value

        // Update warm-up state
        val newWarmUpRemaining = if (isWarmUpFrame) {
            (currentState.warmUpFramesRemaining - 1).coerceAtLeast(0)
        } else {
            0
        }
        val warmUpComplete = newWarmUpRemaining == 0

        // Exercise detection
        var exerciseState = ExerciseState.IDLE
        var repCount = currentState.repetitionCount
        var currentAngle = currentState.currentAngle

        exerciseDetector?.let { detector ->
            exerciseState = detector.analyzeFrame(person)
            repCount = detector.getRepetitionCount()
            currentAngle = detector.getCurrentAngle() ?: 0.0
        }

        // Update UI state
        _uiState.value = currentState.copy(
            detectedPerson = person,
            inferenceTime = inferenceTime,
            fps = fps,
            cpuUsage = cpuUsage,
            memoryUsage = memoryUsage,
            powerConsumption = powerConsumption,
            isWarmUpComplete = warmUpComplete,
            warmUpFramesRemaining = newWarmUpRemaining,
            exerciseState = exerciseState,
            repetitionCount = repCount,
            currentAngle = currentAngle,
            loggedFrameCount = benchmarkLogger.getLoggedFrameCount(),
            loggingDurationSeconds = benchmarkLogger.getLoggingDurationSeconds()
        )

        // Log metrics if logging is active and warm-up is complete
        if (benchmarkLogger.isCurrentlyLogging() && warmUpComplete) {
            val metrics = BenchmarkMetrics(
                modelType = currentState.selectedModel.displayName,
                delegateType = currentState.selectedDelegate.displayName,
                inferenceTimeMs = inferenceTime,
                fps = fps,
                cpuUsagePercent = cpuUsage,
                memoryUsageMb = memoryUsage,
                powerConsumptionMw = powerConsumption,
                exerciseType = currentState.selectedExercise.name,
                repetitionCount = repCount
            )
            benchmarkLogger.logMetrics(metrics)
        }
    }

    /**
     * Start benchmark logging
     */
    fun startLogging() {
        benchmarkLogger.startLogging()
        _uiState.value = _uiState.value.copy(
            isLogging = true,
            loggedFrameCount = 0
        )
    }

    /**
     * Stop benchmark logging
     */
    fun stopLogging() {
        benchmarkLogger.stopLogging()
        _uiState.value = _uiState.value.copy(
            isLogging = false,
            benchmarkSummary = benchmarkLogger.getSummaryStatistics()
        )
    }

    /**
     * Export logged data to CSV
     */
    fun exportToCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = benchmarkLogger.exportToCsv()
            _uiState.value = _uiState.value.copy(
                lastExportPath = path,
                errorMessage = if (path == null) "Export failed" else null
            )
        }
    }

    /**
     * Reset exercise counter
     */
    fun resetExercise() {
        exerciseDetector?.reset()
        _uiState.value = _uiState.value.copy(
            repetitionCount = 0,
            currentAngle = 0.0,
            exerciseState = ExerciseState.IDLE
        )
    }

    /**
     * Clear logged data
     */
    fun clearLog() {
        benchmarkLogger.clearLog()
        _uiState.value = _uiState.value.copy(
            loggedFrameCount = 0,
            benchmarkSummary = null
        )
    }

    /**
     * Get resource profiler instance for use in ImageAnalyzer
     */
    fun getResourceProfiler(): ResourceProfiler = resourceProfiler

    /**
     * Get current detector instance
     */
    fun getDetector(): PoseDetector? = currentDetector

    override fun onCleared() {
        super.onCleared()
        currentDetector?.close()
        tfliteHelper?.close()
    }
}
