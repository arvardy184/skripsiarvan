# AccelPose: Android Pose Estimation Performance Benchmark

A research tool for measuring and comparing the performance of pose estimation models using different hardware accelerators on Android devices.

## 🎯 Project Overview

AccelPose is designed for Computer Science thesis research to benchmark:
- **Inference Latency**: Precise measurement of model inference time
- **FPS (Frames Per Second)**: End-to-end processing performance
- **Hardware Acceleration**: CPU (XNNPACK), GPU, and NNAPI delegate comparison

### Supported Models
1. **MoveNet Lightning (INT8)** - Fast, efficient pose detection
2. **MediaPipe BlazePose Lite (FP16)** - High-accuracy pose estimation

## 🏗️ Architecture

The app follows **Clean Architecture** principles with **MVVM** pattern:

```
app/src/main/java/com/application/skripsiarvan/
│
├── domain/                       # Business Logic Layer
│   ├── model/
│   │   ├── Keypoint.kt          # Body keypoint data class
│   │   ├── Person.kt            # Person with 17 keypoints (COCO format)
│   │   └── ModelType.kt         # Model & Delegate enums
│   └── detector/
│       └── PoseDetector.kt      # Strategy pattern interface
│
├── data/                         # Data Layer
│   ├── ml/
│   │   └── TFLiteHelper.kt      # TFLite interpreter & delegate manager
│   └── detector/
│       ├── MoveNetDetector.kt   # MoveNet implementation
│       └── BlazePoseDetector.kt # BlazePose implementation
│
└── presentation/                 # UI Layer
    ├── viewmodel/
    │   └── PoseViewModel.kt     # State management
    ├── camera/
    │   ├── CameraScreen.kt      # Main UI with CameraX
    │   └── PoseImageAnalyzer.kt # Real-time inference analyzer
    └── components/
        └── PoseVisualization.kt # Skeleton drawing on Canvas
```

## 🚀 Key Features

### 1. **Real-time Performance Tracking**
- Inference time measured using `System.nanoTime()` (milliseconds precision)
- FPS calculation based on frame processing rate
- Live metrics display on UI

### 2. **Hardware Acceleration Switching**
```kotlin
// CPU with XNNPACK
DelegateType.CPU -> options.setUseXNNPACK(true)

// GPU Delegate (with compatibility check)
DelegateType.GPU -> {
    val compatibilityList = CompatibilityList()
    if (compatibilityList.isDelegateSupportedOnThisDevice) {
        options.addDelegate(GpuDelegate(delegateOptions))
    }
}

// NNAPI (NPU/DSP acceleration)
DelegateType.NNAPI -> options.addDelegate(NnApiDelegate())
```

### 3. **Model Switching at Runtime**
Toggle between MoveNet and BlazePose without restarting the app.

### 4. **Pose Visualization**
Real-time skeleton overlay with:
- 17 COCO keypoints
- Bone connections
- Confidence-based filtering

## 📦 Dependencies

```gradle
// TensorFlow Lite
implementation("org.tensorflow:tensorflow-lite:2.15.0")
implementation("org.tensorflow:tensorflow-lite-gpu:2.15.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

// CameraX
implementation("androidx.camera:camera-core:1.3.0")
implementation("androidx.camera:camera-camera2:1.3.0")
implementation("androidx.camera:camera-lifecycle:1.3.0")
implementation("androidx.camera:camera-view:1.3.0")

// Jetpack Compose
implementation("androidx.compose.material3:material3:1.x.x")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
```

## 📥 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd skripsiarvan
```

### 2. Add TensorFlow Lite Models

Download and place the following models in `app/src/main/assets/`:

**MoveNet Lightning (INT8)**
- Download: [TF Hub - MoveNet Lightning INT8](https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/int8/4)
- Filename: `movenet_lightning_int8.tflite`

**BlazePose Lite (FP16)**
- Download: [MediaPipe Pose Landmarker](https://developers.google.com/mediapipe/solutions/vision/pose_landmarker)
- Filename: `blazepose_lite_fp16.tflite`

See `app/src/main/assets/README_MODELS.txt` for detailed instructions.

### 3. Build and Run
```bash
./gradlew assembleDebug
```

Or use Android Studio: **Run > Run 'app'**

## 🎮 Usage

1. **Grant Camera Permission** when prompted
2. **Select Model** from the dropdown (MoveNet or BlazePose)
3. **Select Hardware Accelerator** (CPU, GPU, or NNAPI)
4. **Observe Metrics**:
   - **Inference Time**: Time for model inference only (ms)
   - **FPS**: Frames processed per second
5. **Switch configurations** to compare performance

## 📊 Performance Benchmarking

### Metrics Explained

**Inference Latency**
```kotlin
val startTime = System.nanoTime()
val person = poseDetector.detectPose(bitmap)
val inferenceTime = (System.nanoTime() - startTime) / 1_000_000L // ms
```

**FPS (Frames Per Second)**
```kotlin
frameCount++
val elapsedTime = currentTime - lastFpsTimestamp
if (elapsedTime >= 1000) {
    currentFps = (frameCount * 1000f) / elapsedTime
}
```

### Comparison Matrix

| Model | Delegate | Expected Inference Time | Expected FPS |
|-------|----------|------------------------|--------------|
| MoveNet Lightning | CPU | 20-50 ms | 20-30 FPS |
| MoveNet Lightning | GPU | 10-20 ms | 40-60 FPS |
| MoveNet Lightning | NNAPI | 15-30 ms | 30-50 FPS |
| BlazePose Lite | CPU | 40-80 ms | 10-20 FPS |
| BlazePose Lite | GPU | 20-40 ms | 25-40 FPS |
| BlazePose Lite | NNAPI | 30-60 ms | 15-30 FPS |

*Note: Actual performance varies by device*

## 🔧 Technical Implementation Details

### TFLiteHelper: Delegate Management
- Handles model loading from assets
- Manages delegate lifecycle (initialization & cleanup)
- Performs GPU compatibility checks
- Implements fallback to CPU if GPU unavailable

### PoseImageAnalyzer: Real-time Processing
- Converts `ImageProxy` to `Bitmap`
- Handles image rotation based on device orientation
- Resizes to model input dimensions (192x192 or 256x256)
- Runs inference and tracks performance

### PoseDetector Interface: Strategy Pattern
Allows easy addition of new models:
```kotlin
interface PoseDetector {
    fun detectPose(bitmap: Bitmap): Person?
    fun getInputSize(): Pair<Int, Int>
    fun close()
}
```

## 📱 Device Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Required Features**: Camera
- **Recommended**: GPU support for accelerated inference

## 🐛 Known Limitations

1. **BlazePose Model**: The current implementation uses a simplified output parsing. You may need to adjust the model file or output tensor mapping based on the specific BlazePose variant.

2. **GPU Delegate**: Not all devices support GPU acceleration. The app automatically falls back to CPU with a warning message.

3. **NNAPI**: Performance varies significantly across devices and Android versions.

## 📚 Research Use

This app is designed for academic research. When using for your thesis:

1. **Record metrics** across multiple devices
2. **Test different lighting conditions**
3. **Vary poses** (standing, sitting, exercises)
4. **Document device specifications** (CPU, GPU, RAM)
5. **Run multiple trials** for statistical significance

### Data Collection Suggestions

- Export metrics to CSV for analysis
- Use screen recording for qualitative assessment
- Test on devices with different chipsets (Snapdragon, Exynos, MediaTek)

## 🤝 Contributing

This is a research project. For improvements:
1. Add more models (e.g., YOLOv8-Pose, EfficientPose)
2. Implement metrics export (CSV/JSON)
3. Add batch testing mode
4. Improve BlazePose output parsing

## 📄 License

This project is for educational and research purposes.

## 📧 Contact

For thesis-related questions, contact your research supervisor or CS department.

---

**Built with:** Kotlin • Jetpack Compose • TensorFlow Lite • CameraX • MVVM

**Research Focus:** On-Device ML Performance • Hardware Acceleration • Real-time Inference
