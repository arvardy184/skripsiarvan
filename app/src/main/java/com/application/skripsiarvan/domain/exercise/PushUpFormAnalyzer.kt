package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.FeedbackSeverity
import com.application.skripsiarvan.domain.model.FormCheck
import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Person

class PushUpFormAnalyzer : FormAnalyzer {

    private val scoreHistory = mutableListOf<Int>()

    override fun analyzeForm(person: Person?): FormFeedback? {
        if (person == null) return null

        val checks = mutableListOf<FormCheck>()
        val highlights = mutableMapOf<Int, FeedbackSeverity>()

        // Check 1: Elbow angle
        val elbowAngle = AngleCalculator.getAverageElbowAngle(person)
        checkElbowAngle(elbowAngle, highlights)?.let { checks.add(it) }

        // Check 2: Body alignment (shoulder-hip-ankle)
        checkBodyAlignment(person, highlights)?.let { checks.add(it) }

        val score = computeScore(checks)
        scoreHistory.add(score)

        val overallSeverity = when {
            checks.any { it.severity == FeedbackSeverity.ERROR } -> FeedbackSeverity.ERROR
            checks.any { it.severity == FeedbackSeverity.WARNING } -> FeedbackSeverity.WARNING
            else -> FeedbackSeverity.GOOD
        }

        val primaryMessage = when (overallSeverity) {
            FeedbackSeverity.GOOD -> "Good form!"
            FeedbackSeverity.WARNING ->
                checks.firstOrNull { it.severity == FeedbackSeverity.WARNING }?.message
                    ?: "Check your form"
            FeedbackSeverity.ERROR ->
                checks.firstOrNull { it.severity == FeedbackSeverity.ERROR }?.message
                    ?: "Fix your form"
        }

        val secondaryMessage =
            checks
                .firstOrNull { it.severity != FeedbackSeverity.GOOD && it.message != primaryMessage }
                ?.message ?: ""

        return FormFeedback(
            checks = checks,
            overallSeverity = overallSeverity,
            overallScore = score,
            primaryMessage = primaryMessage,
            secondaryMessage = secondaryMessage,
            jointHighlights = highlights
        )
    }

    private fun checkElbowAngle(
        angle: Double?,
        highlights: MutableMap<Int, FeedbackSeverity>
    ): FormCheck? {
        if (angle == null) return null
        return when {
            angle in 70.0..110.0 -> {
                highlights[BodyPart.LEFT_ELBOW] = FeedbackSeverity.GOOD
                highlights[BodyPart.RIGHT_ELBOW] = FeedbackSeverity.GOOD
                FormCheck("Elbow Angle", "Elbow angle OK (${angle.toInt()}°)", FeedbackSeverity.GOOD)
            }
            angle in 55.0..125.0 -> {
                highlights[BodyPart.LEFT_ELBOW] = FeedbackSeverity.WARNING
                highlights[BodyPart.RIGHT_ELBOW] = FeedbackSeverity.WARNING
                FormCheck(
                    "Elbow Angle",
                    "Elbow angle slightly off (${angle.toInt()}°)",
                    FeedbackSeverity.WARNING
                )
            }
            else -> {
                highlights[BodyPart.LEFT_ELBOW] = FeedbackSeverity.ERROR
                highlights[BodyPart.RIGHT_ELBOW] = FeedbackSeverity.ERROR
                FormCheck(
                    "Elbow Angle",
                    "Elbow angle too ${if (angle < 55.0) "low" else "high"} (${angle.toInt()}°)",
                    FeedbackSeverity.ERROR
                )
            }
        }
    }

    private fun checkBodyAlignment(
        person: Person,
        highlights: MutableMap<Int, FeedbackSeverity>
    ): FormCheck? {
        val leftShoulder = person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
        val leftHip = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
        val leftAnkle = person.keypoints.getOrNull(BodyPart.LEFT_ANKLE)

        if (leftShoulder == null || leftHip == null || leftAnkle == null) return null
        if (leftShoulder.score < 0.3f || leftHip.score < 0.3f || leftAnkle.score < 0.3f)
            return null

        val hipAngle = AngleCalculator.calculateAngle(leftShoulder, leftHip, leftAnkle)
        return when {
            hipAngle >= 155.0 -> {
                highlights[BodyPart.LEFT_HIP] = FeedbackSeverity.GOOD
                highlights[BodyPart.RIGHT_HIP] = FeedbackSeverity.GOOD
                FormCheck("Body Alignment", "Body straight", FeedbackSeverity.GOOD)
            }
            hipAngle >= 135.0 -> {
                highlights[BodyPart.LEFT_HIP] = FeedbackSeverity.WARNING
                highlights[BodyPart.RIGHT_HIP] = FeedbackSeverity.WARNING
                FormCheck("Body Alignment", "Keep body straighter", FeedbackSeverity.WARNING)
            }
            else -> {
                highlights[BodyPart.LEFT_HIP] = FeedbackSeverity.ERROR
                highlights[BodyPart.RIGHT_HIP] = FeedbackSeverity.ERROR
                FormCheck("Body Alignment", "Hip sagging/piking", FeedbackSeverity.ERROR)
            }
        }
    }

    private fun computeScore(checks: List<FormCheck>): Int {
        if (checks.isEmpty()) return 0
        val points =
            checks.sumOf { check ->
                when (check.severity) {
                    FeedbackSeverity.GOOD -> 100
                    FeedbackSeverity.WARNING -> 60
                    FeedbackSeverity.ERROR -> 20
                }
            }
        return points / checks.size
    }

    override fun getAverageScore(): Int? {
        if (scoreHistory.isEmpty()) return null
        return scoreHistory.average().toInt()
    }

    override fun reset() {
        scoreHistory.clear()
    }
}
