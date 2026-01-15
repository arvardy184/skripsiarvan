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
3. Default configuration: **MoveNet Lightning + Level 1: CPU Baseline**

---

### Step 6: Test Performance (3-Level Progression)

#### Quick Test Sequence - Follow this order for best research results:

**With MoveNet Lightning:**

1. **Level 1: CPU Baseline** (No Optimization)
   - Model: MoveNet Lightning
   - Acceleration: Level 1: CPU (Baseline/Legacy)
   - Expected: ~50-80ms inference, 12-20 FPS
   - Purpose: Baseline measurement

2. **Level 2: CPU XNNPACK** (Software Optimization)
   - Model: MoveNet Lightning
   - Acceleration: Level 2: CPU (XNNPACK)
   - Expected: ~20-40ms inference, 25-35 FPS
   - Purpose: Show SIMD optimization impact (~2x faster)

3. **Level 3: GPU Delegate** (Hardware Acceleration)
   - Model: MoveNet Lightning
   - Acceleration: Level 3: GPU Delegate
   - Expected: ~10-20ms inference, 40-60 FPS
   - Purpose: Show hardware acceleration impact (~4-5x faster)

**If BlazePose is installed, repeat with:**

4. **BlazePose + Level 1**: ~80-120ms, 8-12 FPS
5. **BlazePose + Level 2**: ~40-70ms, 14-20 FPS
6. **BlazePose + Level 3**: ~20-40ms, 25-40 FPS

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
- Try higher acceleration level (Level 1 → 2 → 3)
- Switch to MoveNet Lightning (faster than BlazePose)
- Close background apps

**GPU not working?**
- App will show "GPU not compatible, falling back to CPU XNNPACK"
- This is normal on some devices
- Will automatically use Level 2 (CPU XNNPACK) instead
- You can still compare Level 1 vs Level 2 for research

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

### Recommended Testing Matrix (3-Level Progression)

| Device | Model | Acceleration Level | Trials | Purpose |
|--------|-------|-------------------|--------|---------|
| Your Phone | MoveNet | Level 1: CPU Baseline | 3x | Baseline (no opt) |
| Your Phone | MoveNet | Level 2: CPU XNNPACK | 3x | Software opt |
| Your Phone | MoveNet | Level 3: GPU Delegate | 3x | Hardware accel |
| Your Phone | BlazePose | Level 1: CPU Baseline | 3x | Baseline |
| Your Phone | BlazePose | Level 2: CPU XNNPACK | 3x | Software opt |
| Your Phone | BlazePose | Level 3: GPU Delegate | 3x | Hardware accel |

**Analysis Focus:**
- **Level 1 vs 2**: Measure XNNPACK/SIMD optimization impact
- **Level 2 vs 3**: Measure GPU acceleration impact
- **Level 1 vs 3**: Total optimization potential

### Data to Record:
- Device name & specs (CPU, GPU, RAM)
- Android version
- Model type (MoveNet or BlazePose)
- Acceleration level (Level 1, 2, or 3)
- Average inference time (ms)
- Average FPS
- Min/Max values
- Performance gain vs baseline
- Test conditions (lighting, pose type)

### Export Method (Manual):
1. Take screenshots of metrics
2. Record screen video
3. Manually log values in spreadsheet

*Future enhancement: Add CSV export*

---

## 🚀 Next Steps

### Immediate:
1. ✅ Run with MoveNet + Level 1 (Baseline)
2. ✅ Test Level 2 (XNNPACK) - measure software optimization impact
3. ✅ Test Level 3 (GPU) - measure hardware acceleration impact
4. ✅ Compare all 3 levels

### Short-term:
1. Add BlazePose model
2. Repeat 3-level test with BlazePose
3. Test on multiple devices
4. Collect benchmark data for all levels

### For Thesis:
1. Document 3-level testing methodology
2. Run statistical analysis (ANOVA across levels)
3. Create comparison charts (Level 1 vs 2 vs 3)
4. Calculate optimization factors (XNNPACK speedup, GPU speedup)
5. Write performance section with clear progression analysis

---

## 📱 Recommended Test Devices

### Ideal for Research:
- **Flagship**: Samsung Galaxy S21+, Pixel 6+, OnePlus 9+
  - Strong GPU (Level 3 performance)
  - Shows maximum optimization potential
  - Clear 3-level progression

- **Mid-range**: Samsung A52, Xiaomi Redmi Note 10
  - Real-world use case
  - GPU may be weaker (good for comparison)
  - Budget hardware insights

- **Low-end**: Entry-level devices
  - May only have CPU (compare Level 1 vs 2)
  - Accessibility testing
  - Shows importance of software optimization

### Chipset Variety (for comprehensive research):
- Qualcomm Snapdragon (Adreno GPU)
- Samsung Exynos (Mali GPU)
- MediaTek Dimensity (Mali GPU)

**Research Tip**: Test same model on different chipsets to see how XNNPACK and GPU perform across different hardware.

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
