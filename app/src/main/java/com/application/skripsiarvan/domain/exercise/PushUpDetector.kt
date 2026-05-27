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
import com.application.skripsiarvan.domain.model.BodyPart
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

        // BlazePose ~1 fps saat push-up: window 3 berarti rata-rata nilai dari rentang 5-10 detik,
        // satu frame angle rendah tenggelam oleh 2 nilai lama → transisi tidak terpicu.
        // Window=1 tidak ada smoothing di sini; BlazePoseDetector sudah apply EMA α=0.4.
        private const val SMOOTHING_WINDOW_SIZE = 1

        // Temporal constraints (ms)
        private const val MIN_DOWN_HOLD_MS = 150L

        // Jumlah frame berturut-turut sebelum abort state transition. Mencegah oscillation
        // DOWN ↔ GOING_UP / UP ↔ GOING_DOWN akibat satu frame noisy yang menyeberang
        // threshold. Dari log terlihat pola "DOWN→GOING_UP→DOWN→GOING_UP→..." yang
        // menelan beberapa push-up real menjadi satu siklus state.
        // Dengan ~1 fps di BlazePose, 3 frame = 3 detik menunggu. Turunkan ke 1.
        private const val ABORT_CONFIRMATION_FRAMES = 1
        private const val MIN_PUSHUP_KEYPOINT_SCORE = 0.2f
        private const val MIN_BODY_HORIZONTAL_EXTENT = 0.12f
        private const val INVALID_POSE_RESET_FRAMES = 5

        private const val VERBOSE_FRAME_LOG = false
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
    private var consecutiveInvalidPoseCount = 0

    // Anti-overcount: cooldown + full-cycle guard
    private var lastRepTimeMs = 0L
    private val REP_COOLDOWN_MS = 500L
    private var hasReachedBottom = false

    private enum class PushUpState {
        UP, // > 160° (Plank)
        GOING_DOWN, // 145° -> 90° (Eccentric phase)
        DOWN, // < 90° (Bottom hold)
        GOING_UP // 105° -> 160° (Concentric phase)
    }

    private var diagFrameCount = 0

    override fun analyzeFrame(person: Person?): ExerciseState {
        diagFrameCount++
        if (person == null) {
            if (diagFrameCount % 15 == 0)
                Log.d("PushUpDebug", "SKIP person=null state=$currentState")
            return ExerciseState.IDLE
        }
        if (!isPushUpPoseOrientation(person)) {
            val ls = person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
            val lh = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
            if (diagFrameCount % 15 == 0)
                Log.d("PushUpDebug", "SKIP orientation_fail sh=(${ls?.x?.let{"%.2f".format(it)}},${ls?.y?.let{"%.2f".format(it)}}) sco=${ls?.score?.let{"%.2f".format(it)}} hip=(${lh?.x?.let{"%.2f".format(it)}},${lh?.y?.let{"%.2f".format(it)}}) sco=${lh?.score?.let{"%.2f".format(it)}}")
            handleInvalidPose()
            return ExerciseState.IDLE
        }
        consecutiveInvalidPoseCount = 0

        val rawAngle = AngleCalculator.getAverageElbowAngle(person) ?: return ExerciseState.IDLE

        Log.d("PushUpDebug", "angle=$rawAngle state=$currentState wristL=${person.keypoints.getOrNull(9)?.score} wristR=${person.keypoints.getOrNull(10)?.score}")

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
                hasReachedBottom = true
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
                    val cooldownOk = (currentTime - lastRepTimeMs) >= REP_COOLDOWN_MS
                    if (hasReachedBottom && cooldownOk) {
                        repetitionCount++
                        lastRepTimeMs = currentTime
                        Log.d(TAG, "✅ REP COMPLETED! Total: $repetitionCount (Elbow: ${"%.1f".format(smoothedAngle)}°)")
                    } else {
                        Log.w(TAG, "⚠️ Rep ditolak: hasReachedBottom=$hasReachedBottom cooldownOk=$cooldownOk")
                    }
                    hasReachedBottom = false
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
        consecutiveInvalidPoseCount = 0
        lastRepTimeMs = 0L
        hasReachedBottom = false
    }

    private fun isPushUpPoseOrientation(person: Person): Boolean {
        val leftShoulder = person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
        val rightShoulder = person.keypoints.getOrNull(BodyPart.RIGHT_SHOULDER)
        val leftHip = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
        val rightHip = person.keypoints.getOrNull(BodyPart.RIGHT_HIP)

        val shoulder = listOfNotNull(leftShoulder, rightShoulder).maxByOrNull { it.score }
        val hip = listOfNotNull(leftHip, rightHip).maxByOrNull { it.score }
        if (shoulder == null || hip == null) return false
        if (shoulder.score < MIN_PUSHUP_KEYPOINT_SCORE ||
                hip.score < MIN_PUSHUP_KEYPOINT_SCORE) return false

        val torsoDx = kotlin.math.abs(shoulder.x - hip.x)
        val torsoDy = kotlin.math.abs(shoulder.y - hip.y)

        // Kondisi 1 (side-view): shoulder dan hip terpisah secara horizontal — badan mendatar.
        val isSideHorizontal = torsoDx > torsoDy && torsoDx >= MIN_BODY_HORIZONTAL_EXTENT

        // Kondisi 2 (front-view): bahu dan pinggul hampir satu ketinggian y → badan horizontal.
        // Diperketat 0.20 → 0.12 karena squat dari kamera depan juga bisa torsoDy kecil
        // saat posisi bawah. Threshold lebih ketat memaksa hanya posisi benar-benar flat.
        val isFlatByY = torsoDy < 0.12f

        if (!isSideHorizontal && !isFlatByY) return false

        // Guard tambahan: tolak pose squat yang lolos filter di atas.
        // Squat ditandai lutut tertekuk — knee.y > hip.y secara signifikan (lutut lebih rendah).
        // Pada push-up, lutut hampir selevel atau lebih tinggi dari pinggul.
        val leftKnee = person.keypoints.getOrNull(BodyPart.LEFT_KNEE)
        val rightKnee = person.keypoints.getOrNull(BodyPart.RIGHT_KNEE)
        val knee = listOfNotNull(leftKnee, rightKnee).maxByOrNull { it.score }
        if (knee != null && knee.score >= MIN_PUSHUP_KEYPOINT_SCORE) {
            // knee.y > hip.y + 0.15 berarti lutut jauh di bawah pinggul → posisi squat
            if (knee.y > hip.y + 0.15f) return false
        }

        return true
    }

    private fun handleInvalidPose() {
        consecutiveInvalidPoseCount++
        if (consecutiveInvalidPoseCount >= INVALID_POSE_RESET_FRAMES) {
            currentState = PushUpState.UP
            angleBuffer.clear()
            lastSmoothedAngle = null
            consecutiveAboveUpCount = 0
            consecutiveBelowDownCount = 0
        }
    }
}
