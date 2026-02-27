package com.application.skripsiarvan.domain.exercise

import android.content.Context
import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.Person

/**
 * ML-based exercise classifier using TFLite. Placeholder implementation — falls back to IDLE
 * until a classification model is integrated.
 */
class MLExerciseClassifier(private val context: Context) : ExerciseDetector {

    private var repetitionCount = 0
    private var lastAngle: Double? = null

    override fun analyzeFrame(person: Person?): ExerciseState {
        if (person == null) return ExerciseState.IDLE
        // TODO: integrate TFLite pose classification model
        return ExerciseState.IDLE
    }

    override fun getRepetitionCount(): Int = repetitionCount

    override fun getCurrentAngle(): Double? = lastAngle

    override fun reset() {
        repetitionCount = 0
        lastAngle = null
    }

    fun close() {
        // Release TFLite interpreter resources when model is integrated
    }
}
