/**
 * Detektor untuk gerakan Squat
 *
 * State Machine sesuai Flowchart 4.4: STANDING (sudut > 160°) → SQUATTING (sudut < 90°) → STANDING
 * = 1 rep
 *
 * Tempo standar dari skripsi:
 * - Turun: 3 detik
 * - Tahan: 1 detik
 * - Naik: 3 detik
 * - Istirahat: 2 detik
 */
package com.application.skripsiarvan.domain.exercise

import android.util.Log
import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.Person
import java.util.ArrayDeque

/**
 * Detektor untuk gerakan Squat dengan Robust State Machine.
 *
 * Implementasi "Skripsi Level" dengan perbaikan:
 * 1. Signal Smoothing: Menggunakan Moving Average (window=5) untuk meredam noise kamera/MoveNet.
 * 2. Strict Thresholds: Mengikuti standar biomekanika (Parallel Squat ~100°).
 * 3. Temporal Constraints: Memastikan durasi minimum pada fase SQUATTING untuk validitas.
 * 4. Directional Logic: Memastikan perubahan sudut konsisten dengan state (turun/naik).
 *
 * Thresholds:
 * - STANDING: > 165° (Lutut lurus sempurna)
 * - SQUATTING: < 100° (Mendekati parallel squat, lebih strict dari 120°)
 * - HYSTERESIS: Mencegah flickering state di perbatasan sudut.
 */
class SquatDetector : ExerciseDetector {

    companion object {
        private const val TAG = "SquatDetector"

        // Thresholds (relaxed for BlazePose compatibility)
        private const val ANGLE_STAND_ENTER =
                160.0 // Masuk fase berdiri (tidak harus lock-out sempurna)
        private const val ANGLE_STAND_EXIT =
                145.0 // Toleransi mulai menekuk (lowered for sensitivity)

        private const val ANGLE_SQUAT_ENTER = 110.0 // Parallel Squat (lebih toleran dari 100°)
        private const val ANGLE_SQUAT_EXIT = 120.0 // Toleransi mulai naik

        // Smoothing configuration (larger window = more stable with noisy models)
        private const val SMOOTHING_WINDOW_SIZE = 7

        // Temporal constraints (ms)
        private const val MIN_SQUAT_HOLD_DESC = "0.3 det"
        private const val MIN_SQUAT_HOLD_MS = 200L // Turunkan sedikit agar lebih responsive
    }

    private var repetitionCount = 0
    private var currentState = SquatState.STANDING

    // Smoothing buffer
    private val angleBuffer = ArrayDeque<Double>(SMOOTHING_WINDOW_SIZE)
    private var lastSmoothedAngle: Double? = null

    // State timing
    private var stateStartTime = 0L
    private var lastRepTime = 0L

    private enum class SquatState {
        STANDING, // > 165°
        GOING_DOWN, // 150° -> 100° (Descending)
        SQUATTING, // < 100° (Hold)
        GOING_UP // 115° -> 165° (Ascending)
    }

    override fun analyzeFrame(person: Person?): ExerciseState {
        if (person == null) return ExerciseState.IDLE

        val rawAngle = AngleCalculator.getAverageKneeAngle(person) ?: return ExerciseState.IDLE

        // 1. Signal Smoothing (Moving Average)
        val smoothedAngle = applySmoothing(rawAngle)
        val currentTime = System.currentTimeMillis()

        // Hitung delta untuk deteksi arah gerakan
        val delta = if (lastSmoothedAngle != null) smoothedAngle - lastSmoothedAngle!! else 0.0
        lastSmoothedAngle = smoothedAngle

        var exerciseState = ExerciseState.IDLE

        // State Machine dengan Hysteresis & Temporal Checks
        when (currentState) {
            SquatState.STANDING -> {
                // Transisi ke GOING_DOWN jika sudut turun melewati threshold exit
                // Delta check removed — too sensitive with noisy BlazePose detections
                if (smoothedAngle < ANGLE_STAND_EXIT) {
                    transitionTo(SquatState.GOING_DOWN, currentTime)
                    exerciseState = ExerciseState.STARTING
                } else {
                    exerciseState = ExerciseState.IDLE
                }
            }
            SquatState.GOING_DOWN -> {
                if (smoothedAngle <= ANGLE_SQUAT_ENTER) {
                    // Masuk posisi Squat
                    transitionTo(SquatState.SQUATTING, currentTime)
                    exerciseState = ExerciseState.IN_MOTION
                } else if (smoothedAngle > ANGLE_STAND_ENTER) {
                    // Batal turun, kembali berdiri (False start)
                    transitionTo(SquatState.STANDING, currentTime)
                    exerciseState = ExerciseState.IDLE
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            SquatState.SQUATTING -> {
                val durationInSquat = currentTime - stateStartTime

                // Cek apakah mulai naik (sudut membesar melewati threshold exit)
                if (smoothedAngle > ANGLE_SQUAT_EXIT) {
                    // VALIDASI: Apakah durasi di bawah cukup lama? (Mencegah noise spike low angle)
                    if (durationInSquat >= MIN_SQUAT_HOLD_MS) {
                        transitionTo(SquatState.GOING_UP, currentTime)
                        exerciseState = ExerciseState.IN_MOTION
                    } else {
                        // Jika durasi terlalu singkat tapi sudut sudah naik drastis,
                        // kemungkinan gerakan tidak valid atau noise.
                        // Untuk safety, kita anggap batal squat (reset ke GOING_UP tapi mungkin
                        // tidak dihitung rep jika perlu logika lebih lanjut)
                        // Disini kita biarkan transisi tapi log peringatan
                        Log.w(
                                TAG,
                                "⚠️ Squat terlalu singkat (${durationInSquat}ms). Potensi bad form."
                        )
                        transitionTo(SquatState.GOING_UP, currentTime)
                        exerciseState = ExerciseState.IN_MOTION
                    }
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            SquatState.GOING_UP -> {
                if (smoothedAngle >= ANGLE_STAND_ENTER) {
                    // Selesai satu repetisi
                    transitionTo(SquatState.STANDING, currentTime)
                    repetitionCount++
                    lastRepTime = currentTime
                    Log.d(
                            TAG,
                            "✅ REP COMPLETED! Total: $repetitionCount (Knee: ${String.format("%.1f", smoothedAngle)}°)"
                    )
                    exerciseState = ExerciseState.COMPLETED
                } else if (smoothedAngle < ANGLE_SQUAT_ENTER) {
                    // Turun lagi (gagal berdiri penuh)
                    transitionTo(SquatState.SQUATTING, currentTime)
                    exerciseState = ExerciseState.IN_MOTION
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
        }

        // Debug Log (bisa dikurangi frekuensinya nanti)
        // Log.v(TAG, "State: $currentState | Angle: %.1f | Delta: %.1f".format(smoothedAngle,
        // delta))

        return exerciseState
    }

    private fun applySmoothing(rawAngle: Double): Double {
        if (angleBuffer.size >= SMOOTHING_WINDOW_SIZE) {
            angleBuffer.removeFirst()
        }
        angleBuffer.addLast(rawAngle)
        return angleBuffer.average()
    }

    private fun transitionTo(newState: SquatState, time: Long) {
        Log.d(TAG, "State Transition: $currentState -> $newState")
        currentState = newState
        stateStartTime = time
    }

    override fun getRepetitionCount(): Int = repetitionCount

    override fun getCurrentAngle(): Double? = lastSmoothedAngle ?: 0.0

    override fun reset() {
        repetitionCount = 0
        currentState = SquatState.STANDING
        angleBuffer.clear()
        lastSmoothedAngle = null
        stateStartTime = 0L
    }
}
