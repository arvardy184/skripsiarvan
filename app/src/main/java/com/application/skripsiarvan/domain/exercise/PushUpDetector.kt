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

        // ── UP thresholds ────────────────────────────────────────────────────────────────
        // Diturunkan 155° → 140°. Dari log benchmark sebelumnya, rep hanya commit saat
        // elbow smoothed mencapai 155–162° — padahal dari kamera depan dengan
        // foreshortening + EMA lag, push-up natural sering max hanya di ~140–150°.
        // Akibatnya 10–25 push-up real hanya terhitung 5 rep. 140° sudah cukup untuk
        // membedakan "lengan lurus" dari "sedang turun/naik".
        private const val ANGLE_UP_ENTER = 140.0
        private const val ANGLE_UP_EXIT = 125.0 // Hysteresis 15° dari ENTER

        // ── DOWN thresholds ──────────────────────────────────────────────────────────────
        // Dinaikkan 110° → 115° agar lebih mudah dicapai (quarter push-up juga counts).
        private const val ANGLE_DOWN_ENTER = 115.0
        private const val ANGLE_DOWN_EXIT = 125.0 // Hysteresis 10° dari ENTER

        private const val SMOOTHING_WINDOW_SIZE = 3

        // Temporal constraints (ms)
        private const val MIN_DOWN_HOLD_MS = 150L

        // Jumlah frame berturut-turut sebelum abort state transition. Mencegah oscillation
        // DOWN ↔ GOING_UP / UP ↔ GOING_DOWN akibat satu frame noisy yang menyeberang
        // threshold. Dari log terlihat pola "DOWN→GOING_UP→DOWN→GOING_UP→..." yang
        // menelan beberapa push-up real menjadi satu siklus state.
        private const val ABORT_CONFIRMATION_FRAMES = 3

        private const val VERBOSE_FRAME_LOG = true
    }

    private var repetitionCount = 0
    private var currentState = PushUpState.UP

    // Smoothing buffer
    private val angleBuffer = ArrayDeque<Double>(SMOOTHING_WINDOW_SIZE)
    private var lastSmoothedAngle: Double? = null

    // State timing
    private var stateStartTime = 0L

    // Hysteresis counters untuk menolak transisi dari 1 frame noisy
    private var consecutiveAboveUpCount = 0
    private var consecutiveBelowDownCount = 0

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
                    consecutiveAboveUpCount = 0
                    transitionTo(PushUpState.DOWN, currentTime)
                    exerciseState = ExerciseState.IN_MOTION
                } else if (smoothedAngle > ANGLE_UP_ENTER) {
                    // Kandidat batal turun — butuh N frame sebelum commit kembali ke UP.
                    consecutiveAboveUpCount++
                    if (consecutiveAboveUpCount >= ABORT_CONFIRMATION_FRAMES) {
                        consecutiveAboveUpCount = 0
                        transitionTo(PushUpState.UP, currentTime)
                        exerciseState = ExerciseState.IDLE
                    } else {
                        exerciseState = ExerciseState.IN_MOTION
                    }
                } else {
                    consecutiveAboveUpCount = 0
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
                    consecutiveBelowDownCount = 0
                    transitionTo(PushUpState.UP, currentTime)
                    repetitionCount++
                    Log.d(TAG, "✅ REP COMPLETED! Total: $repetitionCount (Elbow: ${"%.1f".format(smoothedAngle)}°)")
                    exerciseState = ExerciseState.COMPLETED
                } else if (smoothedAngle < ANGLE_DOWN_ENTER) {
                    // Kandidat turun lagi — butuh N frame sebelum commit kembali ke DOWN.
                    // Tanpa hysteresis ini, satu dip noisy menyebabkan DOWN↔GOING_UP
                    // bouncing yang menelan beberapa push-up real.
                    consecutiveBelowDownCount++
                    if (consecutiveBelowDownCount >= ABORT_CONFIRMATION_FRAMES) {
                        consecutiveBelowDownCount = 0
                        transitionTo(PushUpState.DOWN, currentTime)
                        exerciseState = ExerciseState.IN_MOTION
                    } else {
                        exerciseState = ExerciseState.IN_MOTION
                    }
                } else {
                    consecutiveBelowDownCount = 0
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
        }

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
        consecutiveAboveUpCount = 0
        consecutiveBelowDownCount = 0
    }
}
