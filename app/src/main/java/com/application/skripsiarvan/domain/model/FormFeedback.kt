package com.application.skripsiarvan.domain.model

data class FormCheck(
    val name: String,
    val message: String,
    val severity: FeedbackSeverity
)

data class FormFeedback(
    val checks: List<FormCheck>,
    val overallSeverity: FeedbackSeverity,
    val overallScore: Int,
    val primaryMessage: String,
    val secondaryMessage: String,
    val jointHighlights: Map<Int, FeedbackSeverity> = emptyMap()
)
