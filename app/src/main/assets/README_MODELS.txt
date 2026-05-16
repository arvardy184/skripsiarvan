==========================================
AccelPose - TensorFlow Lite Model Setup
==========================================

This folder should contain the following TensorFlow Lite models:

1. movenet.tflite
   - MoveNet Lightning (FP16)
   - Download from: https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/float16/4
   - Input size: 192x192
   - Output: 17 keypoints (COCO format)

2. pose_detection.tflite
   - MediaPipe BlazePose detector model
   - Download from: https://storage.googleapis.com/mediapipe-assets/pose_detection.tflite
   - Input size: 224x224
   - Output: body detection / ROI used before landmark inference

3. pose_landmark_lite.tflite
   - MediaPipe BlazePose Lite landmark model
   - Download from: https://storage.googleapis.com/mediapipe-assets/pose_landmark_lite.tflite
   - Input size: 256x256 ROI crop
   - Output: 33/39 landmarks (mapped to COCO 17)

Note:
   - blazepose_lite_fp16.tflite is a legacy landmark-only file and should not be used
     directly on the full camera frame for benchmark data.

==========================================
How to Add Models:
==========================================

1. Download the models from the links above
2. Place them in this directory: app/src/main/assets/
3. Ensure the filenames match exactly:
   - movenet.tflite
   - pose_detection.tflite
   - pose_landmark_lite.tflite

4. Rebuild the project

==========================================
Alternative Model Sources:
==========================================

TensorFlow Hub: https://tfhub.dev/
MediaPipe Models: https://developers.google.com/mediapipe/solutions/vision/pose_landmarker

For research purposes, you can also train your own models
and convert them to TFLite format.
