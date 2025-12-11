==========================================
AccelPose - TensorFlow Lite Model Setup
==========================================

This folder should contain the following TensorFlow Lite models:

1. movenet_lightning_int8.tflite
   - MoveNet Lightning (INT8 quantized)
   - Download from: https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/int8/4
   - Input size: 192x192
   - Output: 17 keypoints (COCO format)

2. blazepose_lite_fp16.tflite
   - MediaPipe BlazePose Lite (FP16)
   - Download from: https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task
   - Note: You may need to extract the .tflite from the .task file or use the full model
   - Alternative: https://tfhub.dev/google/lite-model/movenet/singlepose/thunder/tflite/float16/4
   - Input size: 256x256
   - Output: 33 landmarks (mapped to COCO 17)

==========================================
How to Add Models:
==========================================

1. Download the models from the links above
2. Place them in this directory: app/src/main/assets/
3. Ensure the filenames match exactly:
   - movenet_lightning_int8.tflite
   - blazepose_lite_fp16.tflite

4. Rebuild the project

==========================================
Alternative Model Sources:
==========================================

TensorFlow Hub: https://tfhub.dev/
MediaPipe Models: https://developers.google.com/mediapipe/solutions/vision/pose_landmarker

For research purposes, you can also train your own models
and convert them to TFLite format.
