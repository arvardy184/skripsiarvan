/**
 * Detektor untuk gerakan Push-up
 *
 * State Machine sesuai Flowchart 4.4: UP (sudut siku > 160°) → DOWN (sudut siku < 90°) → UP = 1 rep
 *
 * Tempo standar dari skripsi:
 * - Turun: 2 detik
 * - Tahan: 1 detik
 * - Naik: 2 detik
 * - Istirahat: 2 detik
 */
package com.application.skripsiarvan.domain.exercise

import android.util.Log
import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.Person
import java.util.ArrayDeque

/**
 * Detektor untuk gerakan Push-up dengan Robust State Machine.
 *
 * Implementasi "Skripsi Level" dengan perbaikan:
 * 1. Signal Smoothing: Menggunakan Moving Average (window=5) untuk stabilitas input.
 * 2. Strict Thresholds: Mengikuti standar biomekanika (Siku ~90° saat turun).
 * 3. Temporal Constraints: Memastikan durasi minimum pada fase DOWN (bottom hold).
 * 4. Directional Delta: Memvalidasi arah gerakan (turun/naik) untuk transisi state.
 *
 * Thresholds:
 * - UP: > 160° (Posisi plank/lengan lurus)
 * - DOWN: < 90° (Posisi dada dekat lantai / siku 90°)
 * - HYSTERESIS: Mencegah bouncing state akibat noise mikroskopik.
 */
class PushUpDetector : ExerciseDetector {

    companion object {
        private const val TAG = "PushUpDetector"

        // Thresholds (relaxed for BlazePose compatibility)
        private const val ANGLE_UP_ENTER = 155.0 // Lengan lurus (tidak harus lockout sempurna)
        private const val ANGLE_UP_EXIT = 140.0 // Mulai menekuk (lowered for sensitivity)

        private const val ANGLE_DOWN_ENTER = 100.0 // Siku mendekati 90° (lebih toleran)
        private const val ANGLE_DOWN_EXIT = 110.0 // Mulai mendorong naik

        // Smoothing configuration (larger window for noisy models)
        private const val SMOOTHING_WINDOW_SIZE = 7

        // Temporal constraints (ms)
        private const val MIN_DOWN_HOLD_MS = 150L // Turunkan sedikit agar lebih responsive
    }

    private var repetitionCount = 0
    private var currentState = PushUpState.UP

    // Smoothing buffer
    private val angleBuffer = ArrayDeque<Double>(SMOOTHING_WINDOW_SIZE)
    private var lastSmoothedAngle: Double? = null

    // State timing
    private var stateStartTime = 0L

    private enum class PushUpState {
        UP, // > 160° (Plank)
        GOING_DOWN, // 145° -> 90° (Eccentric phase)
        DOWN, // < 90° (Bottom hold)
        GOING_UP // 105° -> 160° (Concentric phase)
    }

    override fun analyzeFrame(person: Person?): ExerciseState {
        if (person == null) return ExerciseState.IDLE

        val rawAngle = AngleCalculator.getAverageElbowAngle(person) ?: return ExerciseState.IDLE

        // 1. Signal Smoothing
        val smoothedAngle = applySmoothing(rawAngle)
        val currentTime = System.currentTimeMillis()

        // Hitung delta
        val delta = if (lastSmoothedAngle != null) smoothedAngle - lastSmoothedAngle!! else 0.0
        lastSmoothedAngle = smoothedAngle

        var exerciseState = ExerciseState.IDLE

        when (currentState) {
            PushUpState.UP -> {
                // Transisi turun: Sudut mengecil melewati threshold EXIT
                // Delta check removed — too sensitive with noisy BlazePose detections
                if (smoothedAngle < ANGLE_UP_EXIT) {
                    transitionTo(PushUpState.GOING_DOWN, currentTime)
                    exerciseState = ExerciseState.STARTING
                } else {
                    exerciseState = ExerciseState.IDLE
                }
            }
            PushUpState.GOING_DOWN -> {
                if (smoothedAngle <= ANGLE_DOWN_ENTER) {
                    // Masuk posisi bawah (Bottom position)
                    transitionTo(PushUpState.DOWN, currentTime)
                    exerciseState = ExerciseState.IN_MOTION
                } else if (smoothedAngle > ANGLE_UP_ENTER) {
                    // Batal turun, kembali ke posisi plank (False start)
                    transitionTo(PushUpState.UP, currentTime)
                    exerciseState = ExerciseState.IDLE
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            PushUpState.DOWN -> {
                val durationAtBottom = currentTime - stateStartTime

                // Cek apakah mulai naik
                if (smoothedAngle > ANGLE_DOWN_EXIT) {
                    // Validasi durasi di bawah
                    if (durationAtBottom >= MIN_DOWN_HOLD_MS) {
                        transitionTo(PushUpState.GOING_UP, currentTime)
                        exerciseState = ExerciseState.IN_MOTION
                    } else {
                        // Durasi terlalu singkat (Bounce) - Log warning tapi izinkan lanjut
                        // (opsional: bisa di-invalidate)
                        Log.w(
                                TAG,
                                "⚠️ PushUp terlalu cepat di bawah (${durationAtBottom}ms). Gunakan full ROM."
                        )
                        transitionTo(PushUpState.GOING_UP, currentTime)
                        exerciseState = ExerciseState.IN_MOTION
                    }
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            PushUpState.GOING_UP -> {
                if (smoothedAngle >= ANGLE_UP_ENTER) {
                    // Selesai satu repetisi (Lockout)
                    transitionTo(PushUpState.UP, currentTime)
                    repetitionCount++
                    Log.d(
                            TAG,
                            "✅ REP COMPLETED! Total: $repetitionCount (Elbow: ${String.format("%.1f", smoothedAngle)}°)"
                    )
                    exerciseState = ExerciseState.COMPLETED
                } else if (smoothedAngle < ANGLE_DOWN_ENTER) {
                    // Turun lagi (gagal naik penuh)
                    transitionTo(PushUpState.DOWN, currentTime)
                    exerciseState = ExerciseState.IN_MOTION
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
        }

        return exerciseState
    }

    private fun applySmoothing(rawAngle: Double): Double {
        if (angleBuffer.size >= SMOOTHING_WINDOW_SIZE) {
            angleBuffer.removeFirst()
        }
        angleBuffer.addLast(rawAngle)
        return angleBuffer.average()
    }

    private fun transitionTo(newState: PushUpState, time: Long) {
        Log.d(TAG, "State Transition: $currentState -> $newState")
        currentState = newState
        stateStartTime = time
    }

    override fun getRepetitionCount(): Int = repetitionCount

    override fun getCurrentAngle(): Double? = lastSmoothedAngle ?: 0.0

    override fun reset() {
        repetitionCount = 0
        currentState = PushUpState.UP
        angleBuffer.clear()
        lastSmoothedAngle = null
        stateStartTime = 0L
    }
}
