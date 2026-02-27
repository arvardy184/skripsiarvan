package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.FeedbackSeverity
import com.application.skripsiarvan.domain.model.FormCheck
import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Person
import kotlin.math.abs

class SquatFormAnalyzer : FormAnalyzer {

    private val scoreHistory = mutableListOf<Int>()

    override fun analyzeForm(person: Person?): FormFeedback? {
        if (person == null) return null

        val checks = mutableListOf<FormCheck>()
        val highlights = mutableMapOf<Int, FeedbackSeverity>()

        // Check 1: Knee angle (squat depth)
        val kneeAngle = AngleCalculator.getAverageKneeAngle(person)
        checkKneeAngle(kneeAngle, highlights)?.let { checks.add(it) }

        // Check 2: Back/torso alignment
        checkBackAlignment(person, highlights)?.let { checks.add(it) }

        // Check 3: Knee tracking over toes
        checkKneeTracking(person, highlights)?.let { checks.add(it) }

        val score = computeScore(checks)
        scoreHistory.add(score)

        val overallSeverity = when {
            checks.any { it.severity == FeedbackSeverity.ERROR } -> FeedbackSeverity.ERROR
            checks.any { it.severity == FeedbackSeverity.WARNING } -> FeedbackSeverity.WARNING
            else -> FeedbackSeverity.GOOD
        }

        val primaryMessage = when (overallSeverity) {
            FeedbackSeverity.GOOD -> "Good squat form!"
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

    private fun checkKneeAngle(
        angle: Double?,
        highlights: MutableMap<Int, FeedbackSeverity>
    ): FormCheck? {
        if (angle == null) return null
        return when {
            angle in 80.0..110.0 -> {
                highlights[BodyPart.LEFT_KNEE] = FeedbackSeverity.GOOD
                highlights[BodyPart.RIGHT_KNEE] = FeedbackSeverity.GOOD
                FormCheck("Squat Depth", "Good depth (${angle.toInt()}°)", FeedbackSeverity.GOOD)
            }
            angle in 60.0..130.0 -> {
                highlights[BodyPart.LEFT_KNEE] = FeedbackSeverity.WARNING
                highlights[BodyPart.RIGHT_KNEE] = FeedbackSeverity.WARNING
                FormCheck(
                    "Squat Depth",
                    "Adjust squat depth (${angle.toInt()}°)",
                    FeedbackSeverity.WARNING
                )
            }
            else -> {
                highlights[BodyPart.LEFT_KNEE] = FeedbackSeverity.ERROR
                highlights[BodyPart.RIGHT_KNEE] = FeedbackSeverity.ERROR
                FormCheck(
                    "Squat Depth",
                    if (angle > 130.0) "Squat deeper" else "Too deep (${angle.toInt()}°)",
                    FeedbackSeverity.ERROR
                )
            }
        }
    }

    private fun checkBackAlignment(
        person: Person,
        highlights: MutableMap<Int, FeedbackSeverity>
    ): FormCheck? {
        val leftShoulder = person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
        val leftHip = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
        val leftKnee = person.keypoints.getOrNull(BodyPart.LEFT_KNEE)

        if (leftShoulder == null || leftHip == null || leftKnee == null) return null
        if (leftShoulder.score < 0.3f || leftHip.score < 0.3f || leftKnee.score < 0.3f)
            return null

        val torsoAngle = AngleCalculator.calculateAngle(leftShoulder, leftHip, leftKnee)
        return when {
            torsoAngle >= 80.0 -> {
                highlights[BodyPart.LEFT_HIP] = FeedbackSeverity.GOOD
                FormCheck("Back Angle", "Back upright", FeedbackSeverity.GOOD)
            }
            torsoAngle >= 65.0 -> {
                highlights[BodyPart.LEFT_HIP] = FeedbackSeverity.WARNING
                FormCheck("Back Angle", "Lean back slightly", FeedbackSeverity.WARNING)
            }
            else -> {
                highlights[BodyPart.LEFT_HIP] = FeedbackSeverity.ERROR
                FormCheck("Back Angle", "Back too far forward", FeedbackSeverity.ERROR)
            }
        }
    }

    private fun checkKneeTracking(
        person: Person,
        highlights: MutableMap<Int, FeedbackSeverity>
    ): FormCheck? {
        val leftKnee = person.keypoints.getOrNull(BodyPart.LEFT_KNEE)
        val leftAnkle = person.keypoints.getOrNull(BodyPart.LEFT_ANKLE)
        val rightKnee = person.keypoints.getOrNull(BodyPart.RIGHT_KNEE)
        val rightAnkle = person.keypoints.getOrNull(BodyPart.RIGHT_ANKLE)

        if (leftKnee == null || leftAnkle == null || rightKnee == null || rightAnkle == null)
            return null
        if (
            leftKnee.score < 0.3f ||
                leftAnkle.score < 0.3f ||
                rightKnee.score < 0.3f ||
                rightAnkle.score < 0.3f
        )
            return null

        val avgDeviation =
            (abs(leftKnee.x - leftAnkle.x) + abs(rightKnee.x - rightAnkle.x)) / 2

        return when {
            avgDeviation < 0.07f ->
                FormCheck("Knee Tracking", "Knees tracking well", FeedbackSeverity.GOOD)
            avgDeviation < 0.12f ->
                FormCheck("Knee Tracking", "Watch knee alignment", FeedbackSeverity.WARNING)
            else -> FormCheck("Knee Tracking", "Keep knees over toes", FeedbackSeverity.ERROR)
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
