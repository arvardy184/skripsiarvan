package com.application.skripsiarvan.presentation.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.application.skripsiarvan.data.detector.BlazePoseDetector
import com.application.skripsiarvan.data.detector.MoveNetDetector
import com.application.skripsiarvan.data.ml.TFLiteHelper
import com.application.skripsiarvan.domain.detector.PoseDetector
import com.application.skripsiarvan.domain.model.DelegateType
import com.application.skripsiarvan.domain.model.ModelType
import com.application.skripsiarvan.domain.model.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the pose detection screen
 */
data class PoseUiState(
    val selectedModel: ModelType = ModelType.MOVENET_LIGHTNING,
    val selectedDelegate: DelegateType = DelegateType.CPU_BASELINE, // Start with baseline
    val detectedPerson: Person? = null,
    val inferenceTime: Long = 0L, // in milliseconds
    val fps: Float = 0f,
    val isInitialized: Boolean = false,
    val errorMessage: String? = null,
    val deviceInfo: DeviceInfo = DeviceInfo()
)

/**
 * Device information for display
 */
data class DeviceInfo(
    val model: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
)

/**
 * ViewModel managing pose detection state and model lifecycle
 */
class PoseViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PoseUiState())
    val uiState: StateFlow<PoseUiState> = _uiState.asStateFlow()

    private var tfliteHelper: TFLiteHelper? = null
    private var currentDetector: PoseDetector? = null

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

                _uiState.value = currentState.copy(
                    isInitialized = true,
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
     * Update detection results from camera analyzer
     */
    fun updateResults(person: Person?, inferenceTime: Long, fps: Float) {
        _uiState.value = _uiState.value.copy(
            detectedPerson = person,
            inferenceTime = inferenceTime,
            fps = fps
        )
    }

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
