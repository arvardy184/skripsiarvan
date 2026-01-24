package com.application.skripsiarvan.domain.model

/**
 * Jenis latihan yang didukung untuk deteksi repetisi
 * Sesuai dengan desain eksperimen 3×2×2 di skripsi
 */
enum class ExerciseType(val displayName: String) {
    NONE("No Exercise"),
    SQUAT("Squat"),
    PUSH_UP("Push-up");

    companion object {
        fun fromDisplayName(name: String): ExerciseType {
            return entries.find { it.displayName == name } ?: NONE
        }
    }
}

/**
 * State untuk deteksi gerakan
 */
enum class ExerciseState {
    IDLE,       // Tidak ada gerakan terdeteksi
    STARTING,   // Mulai gerakan (fase turun)
    IN_MOTION,  // Sedang dalam gerakan
    COMPLETED   // Satu repetisi selesai
}
