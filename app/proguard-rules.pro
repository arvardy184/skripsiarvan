# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# ── TensorFlow Lite ──────────────────────────────────────────────────────────
# R8/ProGuard di release build menghapus kelas TFLite karena tidak ada referensi
# langsung dari Java/Kotlin yang bisa dideteksi statik. Semua kelas TFLite diakses
# via reflection atau JNI, sehingga HARUS di-keep secara eksplisit.
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-keep class org.tensorflow.lite.nnapi.** { *; }
-dontwarn org.tensorflow.lite.**
-dontwarn org.tensorflow.lite.gpu.**
-dontwarn org.tensorflow.lite.support.**
