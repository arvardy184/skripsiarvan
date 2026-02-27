package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Person

interface FormAnalyzer {
    fun analyzeForm(person: Person?): FormFeedback?
    fun getAverageScore(): Int?
    fun reset()
}
