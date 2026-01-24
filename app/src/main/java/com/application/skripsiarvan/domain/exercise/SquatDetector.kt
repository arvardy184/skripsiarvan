package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.Person

/**
 * Detektor untuk gerakan Squat
 * 
 * State Machine sesuai Flowchart 4.4:
 * STANDING (sudut > 160°) → SQUATTING (sudut < 90°) → STANDING = 1 rep
 * 
 * Tempo standar dari skripsi:
 * - Turun: 3 detik
 * - Tahan: 1 detik  
 * - Naik: 3 detik
 * - Istirahat: 2 detik
 */
class SquatDetector : ExerciseDetector {

    companion object {
        // Threshold sudut lutut untuk deteksi
        private const val ANGLE_THRESHOLD_UP = 160.0    // Posisi berdiri (lutut lurus)
        private const val ANGLE_THRESHOLD_DOWN = 100.0  // Posisi squat (lutut ditekuk)
        
        // Hysteresis untuk menghindari false positives
        private const val HYSTERESIS = 10.0
    }

    private var repetitionCount = 0
    private var currentState = SquatState.STANDING
    private var lastAngle: Double? = null

    private enum class SquatState {
        STANDING,   // Posisi berdiri (sudut > 160°)
        GOING_DOWN, // Sedang turun
        SQUATTING,  // Posisi squat (sudut < 100°)
        GOING_UP    // Sedang naik
    }

    override fun analyzeFrame(person: Person?): ExerciseState {
        if (person == null) return ExerciseState.IDLE

        val kneeAngle = AngleCalculator.getAverageKneeAngle(person)
        if (kneeAngle == null) return ExerciseState.IDLE

        lastAngle = kneeAngle
        val exerciseState: ExerciseState

        when (currentState) {
            SquatState.STANDING -> {
                if (kneeAngle < ANGLE_THRESHOLD_DOWN + HYSTERESIS) {
                    // Mulai squat - sudut lutut mulai berkurang
                    currentState = SquatState.GOING_DOWN
                    exerciseState = ExerciseState.STARTING
                } else {
                    exerciseState = ExerciseState.IDLE
                }
            }
            
            SquatState.GOING_DOWN -> {
                if (kneeAngle <= ANGLE_THRESHOLD_DOWN) {
                    // Mencapai posisi squat
                    currentState = SquatState.SQUATTING
                    exerciseState = ExerciseState.IN_MOTION
                } else if (kneeAngle > ANGLE_THRESHOLD_UP - HYSTERESIS) {
                    // Kembali berdiri sebelum mencapai squat penuh
                    currentState = SquatState.STANDING
                    exerciseState = ExerciseState.IDLE
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            
            SquatState.SQUATTING -> {
                if (kneeAngle > ANGLE_THRESHOLD_DOWN + HYSTERESIS) {
                    // Mulai berdiri
                    currentState = SquatState.GOING_UP
                    exerciseState = ExerciseState.IN_MOTION
                } else {
                    exerciseState = ExerciseState.IN_MOTION
                }
            }
            
            SquatState.GOING_UP -> {
                if (kneeAngle >= ANGLE_THRESHOLD_UP) {
                    // Kembali ke posisi berdiri - 1 repetisi selesai!
                    currentState = SquatState.STANDING
                    repetitionCount++
                    exerciseState = ExerciseState.COMPLETED
                } else if (kneeAngle < ANGLE_THRESHOLD_DOWN) {
                    // Turun lagi sebelum berdiri penuh
                    currentState = SquatState.SQUATTING
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
        currentState = SquatState.STANDING
        lastAngle = null
    }
}
