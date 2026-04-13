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
 */
class SquatDetector : ExerciseDetector {

    companion object {
        private const val TAG = "SquatDetector"

        private const val ANGLE_STAND_ENTER = 160.0
        private const val ANGLE_STAND_EXIT = 145.0

        // Squat depth threshold — diturunkan 120° → 110° untuk memberi headroom bagi lag EMA.
        // Untuk MoveNet Lightning dari kamera depan, "parallel squat" biasanya terbaca
        // ~110–115° pada sinyal smoothed (raw bisa lebih dalam, tapi MA window menahan ~5°).
        private const val ANGLE_SQUAT_ENTER = 110.0
        private const val ANGLE_SQUAT_EXIT = 120.0 // Hysteresis 10° dari ENTER

        // Window=3: lag hanya ~100ms di 10fps.
        private const val SMOOTHING_WINDOW_SIZE = 3

        private const val MIN_SQUAT_HOLD_MS = 200L

        // Frame berturut-turut smoothedAngle > ANGLE_STAND_ENTER untuk membatalkan GOING_DOWN.
        // Mencegah single-frame noise spike mereset state machine.
        private const val ABORT_CONFIRMATION_FRAMES = 3

        // Flip ke false saat benchmark resmi. BuildConfig tidak di-generate di project ini.
        private const val VERBOSE_FRAME_LOG = true
    }

    private var repetitionCount = 0
    private var currentState = SquatState.STANDING

    // Smoothing buffer
    private val angleBuffer = ArrayDeque<Double>(SMOOTHING_WINDOW_SIZE)
    private var lastSmoothedAngle: Double? = null

    // State timing
    private var stateStartTime = 0L
    private var lastRepTime = 0L

    // Hysteresis counter untuk abort GOING_DOWN → STANDING
    private var consecutiveAboveStandCount = 0

    private enum class SquatState {
        STANDING,
        GOING_DOWN,
        SQUATTING,
        GOING_UP
    }

    override fun analyzeFrame(person: Person?): ExerciseState {
        if (person == null) return ExerciseState.IDLE

        val rawAngle = AngleCalculator.getAverageKneeAngle(person) ?: return ExerciseState.IDLE

        val smoothedAngle = applySmoothing(rawAngle)
        val currentTime = System.currentTimeMillis()

        val delta = if (lastSmoothedAngle != null) smoothedAngle - lastSmoothedAngle!! else 0.0
        lastSmoothedAngle = smoothedAngle

        var exerciseState = ExerciseState.IDLE

        when (currentState) {
            SquatState.STANDING -> {
                if (smoothedAngle < ANGLE_STAND_EXIT) {
                    transitionTo(SquatState.GOING_DOWN, currentTime)
                    exerciseState = ExerciseState.STARTING
                } else {
                    exerciseState = ExerciseState.IDLE
                }
            }

            SquatState.GOING_DOWN -> {
                if (smoothedAngle <= ANGLE_SQUAT_ENTER) {
                    consecutiveAboveStandCount = 0
                    transitionTo(SquatState.SQUATTING, currentTime)
                    exerciseState = ExerciseState.IN_MOTION
                } else if (smoothedAngle > ANGLE_STAND_ENTER) {
                    // Kandidat batal turun — butuh N frame berturut-turut sebelum commit.
                    // Tanpa hysteresis ini, satu frame noisy di ~162° akan me-reset state
                    // machine ke STANDING dan squat tidak pernah progress.
                    consecutiveAboveStandCount++
                    if (consecutiveAboveStandCount >= ABORT_CONFIRMATION_FRAMES) {
                        consecutiveAboveStandCount = 0
                        transitionTo(SquatState.STANDING, currentTime)
                        exerciseState = ExerciseState.IDLE
                    } else {
                        exerciseState = ExerciseState.IN_MOTION
                    }
                } else {
                    consecutiveAboveStandCount = 0
                    exerciseState = ExerciseState.IN_MOTION
                }
            }

            SquatState.SQUATTING -> {
                val durationInSquat = currentTime - stateStartTime
                if (smoothedAngle > ANGLE_SQUAT_EXIT) {
                    if (durationInSquat >= MIN_SQUAT_HOLD_MS) {
                        transitionTo(SquatState.GOING_UP, currentTime)
                        exerciseState = ExerciseState.IN_MOTION
                    } else {
                        Log.w(TAG, "⚠️ Squat terlalu singkat (${durationInSquat}ms). Potensi bad form.")
                        transitionTo(SquatState.GOING_UP, currentTime)
                        exerciseState = ExerciseState.IN_MOTION
                    }
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }

            SquatState.GOING_UP -> {
                if (smoothedAngle >= ANGLE_STAND_ENTER) {
                    transitionTo(SquatState.STANDING, currentTime)
                    repetitionCount++
                    lastRepTime = currentTime
                    Log.d(TAG, "✅ REP COMPLETED! Total: $repetitionCount (Knee: ${"%.1f".format(smoothedAngle)}°)")
                    exerciseState = ExerciseState.COMPLETED
                } else if (smoothedAngle < ANGLE_SQUAT_ENTER) {
                    transitionTo(SquatState.SQUATTING, currentTime)
                    exerciseState = ExerciseState.IN_MOTION
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
        }

        // Per-frame debug log — kritikal untuk diagnosa squat detection.
        if (VERBOSE_FRAME_LOG) {
            Log.d(TAG, "frame raw=%.1f smoothed=%.1f delta=%.2f state=%s reps=%d".format(
                rawAngle, smoothedAngle, delta, currentState, repetitionCount))
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
        consecutiveAboveStandCount = 0
    }
}
