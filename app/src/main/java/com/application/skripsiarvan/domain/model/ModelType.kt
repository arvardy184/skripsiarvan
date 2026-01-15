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
 * Hardware acceleration delegates - 3 Level Progression
 * Level 1: Baseline (no optimization) -> Level 2: Software optimization -> Level 3: Hardware acceleration
 */
enum class DelegateType(val displayName: String, val description: String) {
    CPU_BASELINE(
        displayName = "Level 1: CPU (Baseline/Legacy)",
        description = "TFLite default kernel tanpa optimasi XNNPACK"
    ),
    CPU_XNNPACK(
        displayName = "Level 2: CPU (XNNPACK)",
        description = "Optimasi SIMD via library XNNPACK"
    ),
    GPU(
        displayName = "Level 3: GPU Delegate",
        description = "Akselerasi hardware via OpenGL/OpenCL"
    )
}
