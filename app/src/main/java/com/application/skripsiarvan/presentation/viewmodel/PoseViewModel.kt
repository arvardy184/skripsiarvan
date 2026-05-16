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
import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.ModelType
import com.application.skripsiarvan.domain.model.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State untuk layar pose detection Diperluas dengan metrik sumber daya dan exercise detection
 * sesuai skripsi
 */
data class PoseUiState(
        // Model & Delegate selection
        val selectedModel: ModelType = ModelType.MOVENET_LIGHTNING,
        val selectedDelegate: DelegateType = DelegateType.CPU_XNNPACK,
        val effectiveDelegate: DelegateType = DelegateType.CPU_XNNPACK,

        // Pose detection results
        val detectedPerson: Person? = null,
        val cameraFrameAspectRatio: Float = 0f,

        // Performance metrics (Variabel Terikat)
        val inferenceTime: Long = 0L, // Latensi inferensi (ms)
        val fps: Float = 0f, // Throughput (FPS)
        val cpuUsage: Float = 0f, // Utilisasi CPU (%)
        val memoryUsage: Float = 0f, // Penggunaan memori (MB)
        val powerConsumption: Float = 0f, // Konsumsi daya (mW)

        // Exercise detection
        val selectedExercise: ExerciseType = ExerciseType.NONE,
        val repetitionCount: Int = 0,
        val currentAngle: Double = 0.0,
        val exerciseState: ExerciseState = ExerciseState.IDLE,

        // Form feedback
        val formFeedback: FormFeedback? = null,
        val averageFormScore: Int? = null,

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
        val benchmarkSummary: BenchmarkSummary? = null,

        // Metadata eksperimen untuk CSV final
        val replicationId: Int = 1,
        val groundTruthReps: Int = 0,
        val experimentVersion: String = ""
)

/** Device information for display */
data class DeviceInfo(
        val model: String = "${Build.MANUFACTURER} ${Build.MODEL}",
        val androidVersion: String =
                "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        val chipset: String =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Build.SOC_MODEL
                } else {
                    "Unknown"
                }
)

/** ViewModel managing pose detection state, profiling, and exercise detection */
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

    /** Initialize detector with current model and delegate selection */
    fun initializeDetector() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Clean up previous resources
                currentDetector?.close()
                tfliteHelper?.close()
                tfliteHelper = null

                val currentState = _uiState.value

                // Reset warm-up
                _uiState.value =
                        currentState.copy(isWarmUpComplete = false, warmUpFramesRemaining = 5)

                tfliteHelper =
                        TFLiteHelper(
                                context = getApplication(),
                                modelFileName = currentState.selectedModel.fileName,
                                delegateType = currentState.selectedDelegate
                        )

                val initResult = tfliteHelper!!.initializeInterpreter()
                val interpreter = initResult.interpreter

                if (interpreter == null) {
                    _uiState.value =
                            currentState.copy(
                                    isInitialized = false,
                                    errorMessage = "Failed to initialize TFLite interpreter"
                            )
                    return@launch
                }

                currentDetector =
                        when (currentState.selectedModel) {
                            ModelType.MOVENET_LIGHTNING -> MoveNetDetector(interpreter)
                            ModelType.BLAZEPOSE_LITE -> BlazePoseDetector(interpreter)
                        }

                // Initialize exercise detector if selected
                updateExerciseDetector(currentState.selectedExercise)

                _uiState.value =
                        currentState.copy(
                                isInitialized = true,
                                isWarmUpComplete = false,
                                warmUpFramesRemaining = 5,
                                effectiveDelegate = initResult.effectiveDelegateType,
                                errorMessage =
                                        if (initResult.usedFallback &&
                                                        currentState.selectedDelegate ==
                                                                DelegateType.GPU
                                        ) {
                                            "GPU not compatible, falling back to CPU XNNPACK"
                                        } else null
                        )
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(
                                isInitialized = false,
                                errorMessage = "Initialization error: ${e.message}"
                        )
            }
        }
    }

    /** Update exercise detector based on type */
    private fun updateExerciseDetector(type: ExerciseType) {
        exerciseDetector =
                when (type) {
                    ExerciseType.NONE -> null
                    ExerciseType.SQUAT -> SquatDetector()
                    ExerciseType.PUSH_UP -> PushUpDetector()

                }
    
    }

    /** Update selected model and reinitialize detector */
    fun selectModel(modelType: ModelType) {
        if (_uiState.value.selectedModel != modelType) {
            _uiState.value = _uiState.value.copy(selectedModel = modelType, isInitialized = false)
            initializeDetector()
        }
    }

    /** Update selected delegate and reinitialize detector */
    fun selectDelegate(delegateType: DelegateType) {
        if (_uiState.value.selectedDelegate != delegateType) {
            _uiState.value =
                    _uiState.value.copy(
                            selectedDelegate = delegateType,
                            effectiveDelegate = delegateType,
                            isInitialized = false
                    )
            initializeDetector()
        }
    }

    /** Update selected exercise type */
    fun selectExercise(exerciseType: ExerciseType) {
        if (_uiState.value.selectedExercise != exerciseType) {
            updateExerciseDetector(exerciseType)
            exerciseDetector?.reset()
            _uiState.value =
                    _uiState.value.copy(
                            selectedExercise = exerciseType,
                            repetitionCount = 0,
                            currentAngle = 0.0,
                            exerciseState = ExerciseState.IDLE
                    )
        }
    }

    /**
     * Update detection results from camera analyzer Now includes resource metrics and exercise
     * detection
     */
    fun updateResults(
            person: Person?,
            processingTime: Long,
            modelInferenceTime: Long,
            fps: Float,
            cpuUsage: Float,
            memoryUsage: Float,
            powerConsumption: Float,
            cameraFrameAspectRatio: Float,
            isWarmUpFrame: Boolean,
            convertMs: Double = 0.0,
            preprocessMs: Double = 0.0,
            postprocessMs: Double = 0.0,
            avgKeypointConfidence: Float = 0f,
            validKeypointCount: Int = 0
    ) {
        val currentState = _uiState.value

        // Update warm-up state
        val newWarmUpRemaining =
                if (isWarmUpFrame) {
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
        _uiState.value =
                currentState.copy(
                        detectedPerson = person,
                        cameraFrameAspectRatio = cameraFrameAspectRatio,
                        inferenceTime = modelInferenceTime,
                        fps = fps,
                        cpuUsage = cpuUsage,
                        memoryUsage = memoryUsage,
                        powerConsumption = powerConsumption,
                        isWarmUpComplete = warmUpComplete,
                        warmUpFramesRemaining = newWarmUpRemaining,
                        exerciseState = exerciseState,
                        repetitionCount = repCount,
                        currentAngle = currentAngle,
                        formFeedback = null,
                        averageFormScore = null,
                        loggedFrameCount = benchmarkLogger.getLoggedFrameCount(),
                        loggingDurationSeconds = benchmarkLogger.getLoggingDurationSeconds()
                )

        // Log metrics if logging is active and warm-up is complete
        if (benchmarkLogger.isCurrentlyLogging() && !isWarmUpFrame) {
            val metrics =
                    BenchmarkMetrics(
                            modelType = currentState.selectedModel.displayName,
                            selectedDelegateType = currentState.selectedDelegate.displayName,
                            effectiveDelegateType = currentState.effectiveDelegate.displayName,
                            inferenceTimeMs = modelInferenceTime,
                            processingTimeMs = processingTime,
                            fps = fps,
                            cpuUsagePercent = cpuUsage,
                            memoryUsageMb = memoryUsage,
                            powerConsumptionMw = powerConsumption,
                            exerciseType = currentState.selectedExercise.name,
                            repetitionCount = repCount,
                            poseDetected = person != null,
                            replicationId = currentState.replicationId,
                            groundTruthReps = currentState.groundTruthReps,
                            experimentVersion = currentState.experimentVersion,
                            isWarmup = isWarmUpFrame,
                            convertMs = convertMs,
                            preprocessMs = preprocessMs,
                            postprocessMs = postprocessMs,
                            avgKeypointConfidence = avgKeypointConfidence,
                            validKeypointCount = validKeypointCount
                    )
            benchmarkLogger.logMetrics(metrics)
        }
    }

    /**
     * Start benchmark logging.
     * sessionLabel opsional untuk identifikasi kombinasi (misal: "MoveNet_XNNPACK_Squat").
     * Kalau kosong, akan di-auto-generate dari state saat ini.
     */
    fun startLogging(sessionLabel: String = "") {
        val state = _uiState.value
        val label = sessionLabel.ifBlank {
            val modelShort = when (state.selectedModel) {
                ModelType.MOVENET_LIGHTNING -> "MoveNet"
                ModelType.BLAZEPOSE_LITE -> "BlazePose"
            }
            val delegateShort = when (state.selectedDelegate) {
                DelegateType.CPU_BASELINE -> "Baseline"
                DelegateType.CPU_XNNPACK -> "XNNPACK"
                DelegateType.GPU -> "GPU"
            }
            val effectiveDelegateShort = when (state.effectiveDelegate) {
                DelegateType.CPU_BASELINE -> "Baseline"
                DelegateType.CPU_XNNPACK -> "XNNPACK"
                DelegateType.GPU -> "GPU"
            }
            val exerciseShort = state.selectedExercise.name
            if (state.selectedDelegate == state.effectiveDelegate) {
                "${modelShort}_${delegateShort}_${exerciseShort}"
            } else {
                "${modelShort}_${delegateShort}_AS_${effectiveDelegateShort}_${exerciseShort}"
            }
        }
        benchmarkLogger.startLogging(label)
        _uiState.value = _uiState.value.copy(isLogging = true, loggedFrameCount = 0)
    }

    /** Stop benchmark logging */
    fun stopLogging() {
        benchmarkLogger.stopLogging()
        _uiState.value =
                _uiState.value.copy(
                        isLogging = false,
                        benchmarkSummary = benchmarkLogger.getSummaryStatistics()
                )
    }

    /** Export logged data to CSV (frame-level) */
    fun exportToCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = benchmarkLogger.exportToCsv()
            _uiState.value =
                    _uiState.value.copy(
                            lastExportPath = path,
                            errorMessage = if (path == null) "Export failed" else null
                    )
        }
    }

    /** Export session summary CSV (satu baris, siap ANOVA) */
    fun exportSessionSummaryCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = benchmarkLogger.exportSessionSummaryCsv()
            _uiState.value =
                    _uiState.value.copy(
                            lastExportPath = path,
                            errorMessage = if (path == null) "Summary export failed" else null
                    )
        }
    }

    /** Update metadata eksperimen untuk CSV final */
    fun setExperimentMetadata(
        replicationId: Int,
        groundTruthReps: Int,
        experimentVersion: String
    ) {
        _uiState.value = _uiState.value.copy(
            replicationId = replicationId,
            groundTruthReps = groundTruthReps,
            experimentVersion = experimentVersion
        )
    }

    /** Reset exercise counter */
    fun resetExercise() {
        exerciseDetector?.reset()
        _uiState.value =
                _uiState.value.copy(
                        repetitionCount = 0,
                        currentAngle = 0.0,
                        exerciseState = ExerciseState.IDLE,
                        formFeedback = null,
                        averageFormScore = null
                )
    }

    /** Clear logged data */
    fun clearLog() {
        benchmarkLogger.clearLog()
        _uiState.value = _uiState.value.copy(loggedFrameCount = 0, benchmarkSummary = null)
    }

    /** Get resource profiler instance for use in ImageAnalyzer */
    fun getResourceProfiler(): ResourceProfiler = resourceProfiler

    /** Get current detector instance */
    fun getDetector(): PoseDetector? = currentDetector

    override fun onCleared() {
        super.onCleared()
        currentDetector?.close()
        tfliteHelper?.close()
    }
}
