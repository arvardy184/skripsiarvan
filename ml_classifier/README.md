# Exercise Phase Classifier — Custom TFLite Model

## Overview
This module contains the full ML pipeline for training a custom exercise phase classifier:

1. **Data Collection** — Record keypoint data from the Android app
2. **Training** — Train a lightweight MLP classifier in Python  
3. **Export** — Convert to TFLite format for on-device inference
4. **Deploy** — Load the model in Android alongside existing detectors

## Model Architecture
- **Input**: 34 features (17 keypoints × 2 coordinates [x, y])
- **Output**: 5 classes (STANDING, GOING_DOWN, AT_BOTTOM, GOING_UP, UNKNOWN)
- **Architecture**: MLP (34 → 64 → 32 → 5) with ReLU + Dropout
- **Size**: ~10KB TFLite model

## Quick Start

### Option 1: Google Colab (Recommended)
Upload `train_classifier.py` to Colab and run all cells.

### Option 2: Local
```bash
pip install -r requirements.txt
python train_classifier.py
```

The trained model will be saved as `exercise_classifier.tflite`.
Place it in `app/src/main/assets/` for Android deployment.
