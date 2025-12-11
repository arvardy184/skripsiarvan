# AccelPose - Quick Start Guide

## ⚡ Get Started in 5 Minutes

### Step 1: Download TensorFlow Lite Models

You need to add two model files to the `app/src/main/assets/` folder:

#### Option A: Download MoveNet Lightning (Recommended for Quick Start)

```bash
# Navigate to assets folder
cd app/src/main/assets/

# Download MoveNet Lightning INT8
wget https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/int8/4?lite-format=tflite -O movenet_lightning_int8.tflite
```

Or download manually:
- Go to: https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/int8/4
- Click "Download" and save as `movenet_lightning_int8.tflite`
- Place in `app/src/main/assets/`

#### Option B: BlazePose (Optional - for comparison)

For BlazePose, you'll need to:
1. Visit: https://developers.google.com/mediapipe/solutions/vision/pose_landmarker
2. Download the lite model
3. Extract/convert to `blazepose_lite_fp16.tflite`
4. Place in `app/src/main/assets/`

**Note**: You can start with just MoveNet and add BlazePose later.

---

### Step 2: Sync Gradle Dependencies

```bash
# In Android Studio
File > Sync Project with Gradle Files

# Or via command line
./gradlew build
```

This will download:
- TensorFlow Lite (2.15.0)
- TensorFlow Lite GPU
- CameraX libraries
- Jetpack Compose dependencies

---

### Step 3: Connect Android Device

#### Physical Device (Recommended)
```bash
# Enable Developer Options on your phone:
# Settings > About Phone > Tap "Build Number" 7 times

# Enable USB Debugging:
# Settings > Developer Options > USB Debugging

# Connect via USB and verify
adb devices
```

#### Emulator (Limited GPU support)
- Create AVD with API 24+
- Note: GPU delegate may not work on emulator

---

### Step 4: Run the App

```bash
# Install and run debug build
./gradlew installDebug

# Or in Android Studio
Run > Run 'app' (Shift+F10)
```

---

### Step 5: Grant Camera Permission

When the app launches:
1. Tap "Allow" when prompted for camera access
2. Camera preview should appear immediately
3. Default configuration: **MoveNet Lightning + CPU**

---

### Step 6: Test Performance

#### Quick Test Sequence:

1. **CPU Baseline**
   - Model: MoveNet Lightning
   - Accelerator: CPU (XNNPACK)
   - Expected: ~30-50ms inference, 20-30 FPS

2. **GPU Acceleration** (if supported)
   - Model: MoveNet Lightning
   - Accelerator: GPU
   - Expected: ~10-20ms inference, 40-60 FPS

3. **NNAPI** (device-dependent)
   - Model: MoveNet Lightning
   - Accelerator: NNAPI
   - Expected: Varies (15-30ms typical)

#### If BlazePose is installed:

4. **BlazePose + GPU**
   - Model: MediaPipe BlazePose Lite
   - Accelerator: GPU
   - Expected: ~20-40ms inference, 25-40 FPS

---

## 🎯 Pose Detection Tips

### For Best Results:
- **Lighting**: Good, even lighting
- **Distance**: Stand 1.5-3 meters from camera
- **Background**: Plain background works best
- **Pose**: Full body visible in frame

### Troubleshooting:

**No pose detected?**
- Ensure full body is visible
- Check lighting conditions
- Try moving closer/farther from camera

**Low FPS?**
- Switch to MoveNet Lightning (faster)
- Try GPU delegate
- Close background apps

**GPU not working?**
- App will show "GPU not compatible, using CPU fallback"
- This is normal on some devices
- CPU will be used automatically

---

## 📊 Understanding Metrics

### Inference Time
- **What it measures**: Model execution time only
- **Good value**: < 50ms for real-time
- **How it's measured**: `System.nanoTime()` before/after inference

### FPS (Frames Per Second)
- **What it measures**: End-to-end processing speed
- **Good value**: > 20 FPS for smooth experience
- **Includes**: Image capture + preprocessing + inference + visualization

### Relationship:
```
FPS ≈ 1000 / (Inference Time + Overhead)

Example:
- Inference: 25ms
- Overhead: ~15ms (capture, resize, draw)
- Total: 40ms per frame
- FPS: 1000/40 = 25 FPS
```

---

## 🔍 Quick Debugging

### Build Errors

**"Cannot resolve symbol TensorFlow"**
```bash
# Sync Gradle
./gradlew build --refresh-dependencies
```

**"Model file not found"**
```bash
# Check if models exist
ls -la app/src/main/assets/
# Should show .tflite files
```

### Runtime Errors

**"java.io.IOException: model.tflite not found"**
- Ensure model files are in `app/src/main/assets/`
- Rebuild: `./gradlew clean assembleDebug`

**"Camera permission denied"**
- Manually grant: `adb shell pm grant com.application.skripsiarvan android.permission.CAMERA`

**App crashes on launch**
```bash
# Check logs
adb logcat | grep skripsiarvan
```

---

## 🎓 Research Data Collection

### Recommended Testing Matrix

| Device | Model | Delegate | Trials | Notes |
|--------|-------|----------|--------|-------|
| Your Phone | MoveNet | CPU | 3x | Baseline |
| Your Phone | MoveNet | GPU | 3x | Best perf |
| Your Phone | MoveNet | NNAPI | 3x | NPU test |
| Your Phone | BlazePose | GPU | 3x | Accuracy |

### Data to Record:
- Device name & specs (CPU, GPU, RAM)
- Android version
- Model type
- Delegate type
- Average inference time (ms)
- Average FPS
- Min/Max values
- Test conditions (lighting, pose type)

### Export Method (Manual):
1. Take screenshots of metrics
2. Record screen video
3. Manually log values in spreadsheet

*Future enhancement: Add CSV export*

---

## 🚀 Next Steps

### Immediate:
1. ✅ Run with MoveNet + CPU
2. ✅ Test GPU delegate
3. ✅ Compare performance

### Short-term:
1. Add BlazePose model
2. Test on multiple devices
3. Collect benchmark data

### For Thesis:
1. Document methodology
2. Run statistical analysis
3. Create comparison charts
4. Write performance section

---

## 📱 Recommended Test Devices

### Ideal for Research:
- **Flagship**: Samsung Galaxy S21+, Pixel 6+
  - Strong GPU, NNAPI support
  - Consistent performance

- **Mid-range**: Samsung A52, Xiaomi Redmi Note 10
  - Real-world use case
  - Budget hardware insights

- **Low-end**: Entry-level devices
  - CPU-only performance
  - Accessibility testing

### Chipset Variety:
- Qualcomm Snapdragon (Adreno GPU)
- Samsung Exynos (Mali GPU)
- MediaTek Dimensity (Mali GPU)

---

## 📞 Support

### Common Issues:
- Check `app/src/main/assets/README_MODELS.txt`
- Review `README.md` for full documentation
- Check `PROJECT_STRUCTURE.md` for architecture details

### Logs:
```bash
# Filter for app logs
adb logcat | grep -E "TFLiteHelper|PoseDetector|PoseViewModel"
```

---

**Estimated Time**: 10-15 minutes (excluding model download)

**Build Time**: ~2-5 minutes (first build)

**Total Project Size**: ~50MB (with models)

---

Happy Benchmarking! 🎯📊🚀
