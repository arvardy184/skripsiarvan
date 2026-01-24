package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.Person

/**
 * Detektor untuk gerakan Push-up
 * 
 * State Machine sesuai Flowchart 4.4:
 * UP (sudut siku > 160°) → DOWN (sudut siku < 90°) → UP = 1 rep
 * 
 * Tempo standar dari skripsi:
 * - Turun: 2 detik
 * - Tahan: 1 detik
 * - Naik: 2 detik
 * - Istirahat: 2 detik
 */
class PushUpDetector : ExerciseDetector {

    companion object {
        // Threshold sudut siku untuk deteksi
        private const val ANGLE_THRESHOLD_UP = 155.0    // Posisi atas (siku lurus)
        private const val ANGLE_THRESHOLD_DOWN = 100.0  // Posisi bawah (siku ditekuk)
        
        // Hysteresis untuk menghindari false positives
        private const val HYSTERESIS = 10.0
    }

    private var repetitionCount = 0
    private var currentState = PushUpState.UP
    private var lastAngle: Double? = null

    private enum class PushUpState {
        UP,         // Posisi atas (siku lurus, sudut > 155°)
        GOING_DOWN, // Sedang turun
        DOWN,       // Posisi bawah (siku ditekuk, sudut < 100°)
        GOING_UP    // Sedang naik
    }

    override fun analyzeFrame(person: Person?): ExerciseState {
        if (person == null) return ExerciseState.IDLE

        val elbowAngle = AngleCalculator.getAverageElbowAngle(person)
        if (elbowAngle == null) return ExerciseState.IDLE

        lastAngle = elbowAngle
        val exerciseState: ExerciseState

        when (currentState) {
            PushUpState.UP -> {
                if (elbowAngle < ANGLE_THRESHOLD_DOWN + HYSTERESIS) {
                    // Mulai turun - sudut siku mulai berkurang
                    currentState = PushUpState.GOING_DOWN
                    exerciseState = ExerciseState.STARTING
                } else {
                    exerciseState = ExerciseState.IDLE
                }
            }
            
            PushUpState.GOING_DOWN -> {
                if (elbowAngle <= ANGLE_THRESHOLD_DOWN) {
                    // Mencapai posisi bawah
                    currentState = PushUpState.DOWN
                    exerciseState = ExerciseState.IN_MOTION
                } else if (elbowAngle > ANGLE_THRESHOLD_UP - HYSTERESIS) {
                    // Kembali ke atas sebelum mencapai posisi bawah penuh
                    currentState = PushUpState.UP
                    exerciseState = ExerciseState.IDLE
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            
            PushUpState.DOWN -> {
                if (elbowAngle > ANGLE_THRESHOLD_DOWN + HYSTERESIS) {
                    // Mulai naik
                    currentState = PushUpState.GOING_UP
                    exerciseState = ExerciseState.IN_MOTION
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            
            PushUpState.GOING_UP -> {
                if (elbowAngle >= ANGLE_THRESHOLD_UP) {
                    // Kembali ke posisi atas - 1 repetisi selesai!
                    currentState = PushUpState.UP
                    repetitionCount++
                    exerciseState = ExerciseState.COMPLETED
                } else if (elbowAngle < ANGLE_THRESHOLD_DOWN) {
                    // Turun lagi sebelum naik penuh
                    currentState = PushUpState.DOWN
                    exerciseState = ExerciseState.IN_MOTION
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
        }

        return exerciseState
    }

    override fun getRepetitionCount(): Int = repetitionCount

    override fun getCurrentAngle(): Double? = lastAngle

    override fun reset() {
        repetitionCount = 0
        currentState = PushUpState.UP
        lastAngle = null
    }
}
