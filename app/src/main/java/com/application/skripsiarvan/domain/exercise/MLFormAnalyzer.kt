package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Person

/** Placeholder ML-based form analyzer. Falls back to null until a TFLite form model is available. */
class MLFormAnalyzer : FormAnalyzer {
    override fun analyzeForm(person: Person?): FormFeedback? = null
    override fun getAverageScore(): Int? = null
    override fun reset() {}
}
