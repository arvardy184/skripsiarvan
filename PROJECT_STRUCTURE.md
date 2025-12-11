# AccelPose - Complete Project Structure

## 📁 Directory Tree

```
skripsiarvan/
│
├── app/
│   ├── build.gradle.kts                    # Module-level Gradle config with dependencies
│   │
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml         # App manifest with camera permissions
│       │   │
│       │   ├── assets/                     # TensorFlow Lite models directory
│       │   │   ├── README_MODELS.txt       # Model download instructions
│       │   │   ├── movenet_lightning_int8.tflite      (download required)
│       │   │   └── blazepose_lite_fp16.tflite         (download required)
│       │   │
│       │   └── java/com/application/skripsiarvan/
│       │       │
│       │       ├── MainActivity.kt         # Entry point with permission handling
│       │       │
│       │       ├── domain/                 # Business Logic Layer
│       │       │   ├── model/
│       │       │   │   ├── Keypoint.kt           # Single keypoint data class
│       │       │   │   ├── Person.kt             # Person with 17 keypoints
│       │       │   │   └── ModelType.kt          # Enums for Model & Delegate types
│       │       │   │
│       │       │   └── detector/
│       │       │       └── PoseDetector.kt       # Strategy pattern interface
│       │       │
│       │       ├── data/                   # Data Layer
│       │       │   ├── ml/
│       │       │   │   └── TFLiteHelper.kt       # TFLite interpreter & delegate manager
│       │       │   │
│       │       │   └── detector/
│       │       │       ├── MoveNetDetector.kt    # MoveNet implementation
│       │       │       └── BlazePoseDetector.kt  # BlazePose implementation
│       │       │
│       │       ├── presentation/           # UI Layer
│       │       │   ├── viewmodel/
│       │       │   │   └── PoseViewModel.kt      # State management & lifecycle
│       │       │   │
│       │       │   ├── camera/
│       │       │   │   ├── CameraScreen.kt       # Main UI with controls & metrics
│       │       │   │   └── PoseImageAnalyzer.kt  # CameraX analyzer with perf tracking
│       │       │   │
│       │       │   └── components/
│       │       │       └── PoseVisualization.kt  # Canvas drawing for skeleton
│       │       │
│       │       └── ui/theme/
│       │           ├── Color.kt
│       │           ├── Theme.kt
│       │           └── Type.kt
│       │
│       ├── androidTest/                    # Instrumentation tests
│       └── test/                           # Unit tests
│
├── build.gradle.kts                        # Project-level Gradle config
├── settings.gradle.kts                     # Gradle settings
├── gradle.properties                       # Gradle properties
├── .gitignore                             # Git ignore rules (includes *.tflite)
│
├── README.md                              # Main project documentation
└── PROJECT_STRUCTURE.md                   # This file

```

## 📝 File Descriptions

### Core Application Files

#### `MainActivity.kt` (98 lines)
- **Purpose**: Entry point of the application
- **Key Features**:
  - Camera permission handling using ActivityResultContracts
  - Permission request UI with LaunchedEffect
  - Hosts CameraScreen when permission granted
- **Dependencies**: CameraScreen, Theme

---

### Domain Layer (Business Logic)

#### `domain/model/Keypoint.kt`
```kotlin
data class Keypoint(
    val x: Float,      // Normalized 0-1
    val y: Float,      // Normalized 0-1
    val score: Float,  // Confidence 0-1
    val label: String
)
```

#### `domain/model/Person.kt`
```kotlin
data class Person(
    val keypoints: List<Keypoint>,  // 17 COCO keypoints
    val score: Float
)

object BodyPart {
    // Constants for 17 COCO keypoints
    const val NOSE = 0
    const val LEFT_SHOULDER = 5
    // ... etc
}
```

#### `domain/model/ModelType.kt`
```kotlin
enum class ModelType(val displayName: String, val fileName: String)
enum class DelegateType(val displayName: String)
```

#### `domain/detector/PoseDetector.kt`
```kotlin
interface PoseDetector {
    fun detectPose(bitmap: Bitmap): Person?
    fun getInputSize(): Pair<Int, Int>
    fun close()
}
```

---

### Data Layer (Implementation)

#### `data/ml/TFLiteHelper.kt` (115 lines)
- **Purpose**: Manages TFLite interpreter and hardware delegates
- **Key Features**:
  - Loads models from assets folder
  - Initializes interpreter with selected delegate
  - GPU compatibility check using `CompatibilityList`
  - Automatic fallback to CPU if GPU unavailable
  - Proper resource cleanup

**Delegate Initialization**:
```kotlin
DelegateType.CPU   -> options.setUseXNNPACK(true)
DelegateType.GPU   -> options.addDelegate(GpuDelegate(delegateOptions))
DelegateType.NNAPI -> options.addDelegate(NnApiDelegate())
```

#### `data/detector/MoveNetDetector.kt` (92 lines)
- **Purpose**: MoveNet Lightning model implementation
- **Input**: 192x192 RGB image
- **Output**: [1, 1, 17, 3] tensor (y, x, score)
- **Features**:
  - Image preprocessing with normalization
  - Confidence threshold filtering (0.3)
  - Returns Person with 17 keypoints

#### `data/detector/BlazePoseDetector.kt` (105 lines)
- **Purpose**: MediaPipe BlazePose model implementation
- **Input**: 256x256 RGB image
- **Output**: [1, 33, 5] tensor (33 landmarks mapped to 17 COCO)
- **Features**:
  - BlazePose to COCO keypoint mapping
  - Visibility score as confidence
  - Higher confidence threshold (0.5)

---

### Presentation Layer (UI)

#### `presentation/viewmodel/PoseViewModel.kt` (148 lines)
- **Purpose**: Manages app state and model lifecycle
- **State Management**:
  ```kotlin
  data class PoseUiState(
      val selectedModel: ModelType,
      val selectedDelegate: DelegateType,
      val detectedPerson: Person?,
      val inferenceTime: Long,
      val fps: Float,
      val isInitialized: Boolean,
      val errorMessage: String?
  )
  ```
- **Key Functions**:
  - `initializeDetector()`: Creates new detector with selected config
  - `selectModel()`: Switches model and reinitializes
  - `selectDelegate()`: Switches delegate and reinitializes
  - `updateResults()`: Updates metrics from camera analyzer

#### `presentation/camera/CameraScreen.kt` (257 lines)
- **Purpose**: Main UI with camera preview and controls
- **Components**:
  - **CameraX Preview**: AndroidView with PreviewView
  - **Image Analysis**: Real-time pose detection
  - **Control Panel**: Model/delegate dropdowns + metrics
  - **Pose Overlay**: Visualization on top of camera

- **Composables**:
  - `CameraScreen()`: Main composable
  - `ControlPanel()`: Bottom card with controls
  - `DropdownSelector<T>()`: Generic dropdown for model/delegate
  - `MetricsDisplay()`: Inference time & FPS display
  - `DeviceInfoDisplay()`: Device model & Android version

#### `presentation/camera/PoseImageAnalyzer.kt` (102 lines)
- **Purpose**: CameraX ImageAnalysis.Analyzer for real-time inference
- **Process Flow**:
  1. Convert `ImageProxy` to `Bitmap`
  2. Rotate based on device orientation
  3. Resize to model input size
  4. Run inference (with timing)
  5. Calculate FPS
  6. Send results to ViewModel

- **Performance Tracking**:
  ```kotlin
  val startTime = System.nanoTime()
  val person = poseDetector.detectPose(bitmap)
  val inferenceTime = (System.nanoTime() - startTime) / 1_000_000L
  ```

#### `presentation/components/PoseVisualization.kt` (127 lines)
- **Purpose**: Draws pose skeleton on Canvas
- **Features**:
  - Draws 16 bone connections (lines)
  - Draws 17 joint keypoints (circles)
  - Confidence-based filtering (> 0.3)
  - Green lines, red circles
  - Scales to view dimensions

---

## 🔄 Data Flow

```
User Action (Select Model/Delegate)
    ↓
ViewModel.selectModel() / selectDelegate()
    ↓
ViewModel.initializeDetector()
    ↓
TFLiteHelper.initializeInterpreter()
    ↓
Create MoveNetDetector / BlazePoseDetector
    ↓
Camera Frame Available
    ↓
PoseImageAnalyzer.analyze()
    ↓
PoseDetector.detectPose()
    ↓
ViewModel.updateResults()
    ↓
UI State Update (Compose Recomposition)
    ↓
CameraScreen displays metrics + PoseVisualization
```

## 📊 Key Metrics

### Inference Time Measurement
```kotlin
// Start timing before inference
val startTime = System.nanoTime()

// Run inference
val person = poseDetector.detectPose(bitmap)

// Calculate elapsed time in milliseconds
val inferenceTime = (System.nanoTime() - startTime) / 1_000_000L
```

### FPS Calculation
```kotlin
frameCount++
val currentTime = System.currentTimeMillis()
val elapsedTime = currentTime - lastFpsTimestamp

if (elapsedTime >= 1000) { // Update every second
    currentFps = (frameCount * 1000f) / elapsedTime
    frameCount = 0
    lastFpsTimestamp = currentTime
}
```

## 🎨 UI Layout

```
┌────────────────────────────────────┐
│                                    │
│        Camera Preview              │
│         (with Pose Overlay)        │
│                                    │
│                                    │
│                                    │
├────────────────────────────────────┤
│  AccelPose - Performance Benchmark │
│                                    │
│  Model: [MoveNet Lightning ▼]     │
│  Accelerator: [CPU (XNNPACK) ▼]   │
│  ─────────────────────────────     │
│  Inference: 25 ms    FPS: 35.2    │
│  ─────────────────────────────     │
│  Device: Samsung Galaxy S21        │
│  Android 13 (API 33)              │
└────────────────────────────────────┘
```

## 🧪 Testing Strategy

### Unit Tests (Recommended)
- `TFLiteHelperTest`: Test delegate initialization
- `MoveNetDetectorTest`: Test output parsing
- `PoseViewModelTest`: Test state management

### Integration Tests (Recommended)
- Camera permission flow
- Model switching without crashes
- Delegate switching performance

### Manual Testing Checklist
- [ ] Camera permission granted/denied
- [ ] MoveNet model loads successfully
- [ ] BlazePose model loads successfully
- [ ] CPU delegate works
- [ ] GPU delegate works (or falls back to CPU)
- [ ] NNAPI delegate works
- [ ] Pose visualization renders correctly
- [ ] Metrics update in real-time
- [ ] App handles rotation gracefully

## 🔧 Build Commands

```bash
# Clean build
./gradlew clean

# Debug APK
./gradlew assembleDebug

# Release APK (unsigned)
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

## 📦 APK Size Optimization

Current size (estimated): ~50MB (with TFLite models)

**To reduce size**:
- Use INT8 quantized models
- Enable ProGuard/R8 in release builds
- Use model compression
- Split APKs by ABI

## 🚀 Future Enhancements

1. **Export Metrics**: CSV/JSON export for analysis
2. **Batch Testing**: Automated tests across configurations
3. **More Models**: YOLOv8-Pose, EfficientPose
4. **Video Input**: Test on recorded videos
5. **Graph Visualization**: Real-time performance graphs
6. **Multi-person Detection**: Support multiple people
7. **Custom Model Upload**: Load models at runtime

---

**Total Lines of Code**: ~1200+ lines (excluding tests and theme files)

**Last Updated**: December 2024
