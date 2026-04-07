==========================================
AccelPose - TensorFlow Lite Model Setup
==========================================

Keduanya menggunakan FP16 (half-precision weights) untuk konsistensi benchmark.

1. movenet_lightning_fp16.tflite
   - MoveNet Lightning SinglePose (FP16 weights)
   - Download: https://tfhub.dev/google/lite-model/movenet/singlepose/lightning/tflite/float16/4
   - Input : [1, 192, 192, 3] FLOAT32, nilai piksel RAW [0, 255] — TANPA normalisasi
   - Output: [1, 1, 17, 3] FLOAT32 — [y, x, score] per keypoint (COCO 17)
   - ⚠️ JANGAN pakai INT8 variant — input dtype berbeda (UINT8 vs FLOAT32)
   - ⚠️ JANGAN pakai MultiPose variant — output shape berbeda ([1, 6, 56])

2. blazepose_lite_fp16.tflite
   - MediaPipe BlazePose Lite (FP16 weights)
   - Download: https://storage.googleapis.com/mediapipe-assets/pose_landmark_lite.tflite
     Atau ekstrak dari: https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task
   - Input : [1, 256, 256, 3] FLOAT32, normalisasi [-1, 1]
   - Output: shape divisible by 33, stride=5 ideal → [x, y, z, visibility, presence]

==========================================
Cara menaruh model:
==========================================

1. Download kedua file di atas
2. Rename sesuai nama di atas jika perlu
3. Taruh di: app/src/main/assets/
4. Rebuild project

==========================================
Cara verifikasi model sudah benar:
==========================================

Jalankan app, filter Logcat:
  adb logcat -s BlazePoseDetector:I MoveNetDetector:I

Lihat baris "VALIDASI MODEL" — pastikan tidak ada ⛔ atau ⚠️.
