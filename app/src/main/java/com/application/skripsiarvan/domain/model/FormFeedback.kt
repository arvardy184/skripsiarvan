package com.application.skripsiarvan.domain.model

/** Individual form check result. Each check evaluates one aspect of exercise form. */
data class FormCheck(
  val name: String, // e.g., "Depth", "Tempo", "Symmetry"
  val severity: FeedbackSeverity,
  val message: String, // Human-readable feedback
  val value: Double = 0.0, // Measured value (angle, duration, etc.)
  val target: Double = 0.0, // Target/ideal value
  val score: Int = 100 // 0-100 score for this check
)

data class FormFeedback(
  val overallScore: Int = 100, // 0-100 overall form score
  val overallSeverity: FeedbackSeverity = FeedbackSeverity.GOOD,
  val checks: List<FormCheck> = emptyList(), // Individual check results
  val primaryMessage: String = "", // Most important feedback message
  val secondaryMessage: String = "", // Additional context
  val jointHighlights: Map<Int, FeedbackSeverity> =
    emptyMap() // BodyPart index -> severity (for skeleton coloring)
) {
  companion object {
    val IDLE =
      FormFeedback(
        overallScore = 0,
        overallSeverity = FeedbackSeverity.GOOD,
        primaryMessage = "Get into position",
        secondaryMessage = ""
      )
  }
}
