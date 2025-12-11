package com.application.skripsiarvan.domain.model

/**
 * Supported pose estimation models
 */
enum class ModelType(val displayName: String, val fileName: String) {
    MOVENET_LIGHTNING(
        displayName = "MoveNet Lightning (INT8)",
        fileName = "movenet_lightning_int8.tflite"
    ),
    BLAZEPOSE_LITE(
        displayName = "MediaPipe BlazePose Lite (FP16)",
        fileName = "blazepose_lite_fp16.tflite"
    )
}

/**
 * Hardware acceleration delegates
 */
enum class DelegateType(val displayName: String) {
    CPU("CPU (XNNPACK)"),
    GPU("GPU"),
    NNAPI("NNAPI")
}
