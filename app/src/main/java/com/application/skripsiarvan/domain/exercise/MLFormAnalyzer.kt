package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.FeedbackSeverity
import com.application.skripsiarvan.domain.model.FormCheck
import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Person
import java.util.ArrayDeque
import kotlin.math.abs

/**
 * Form analyzer that works with the ML-classified phases. Reuses the same biomechanical checks
 * (depth, symmetry, alignment) but receives phase information from the ML classifier instead of
 * computing its own phase transitions.
 *
 * This is essentially the same logic as SquatFormAnalyzer but designed to be exercise-agnostic — it
 * analyzes whatever the ML classifier identifies.
 */
class MLFormAnalyzer : FormAnalyzer {

  companion object {
    private const val SYMMETRY_GOOD = 10.0
    private const val SYMMETRY_WARNING = 18.0
    private const val LEAN_GOOD = 25.0
    private const val LEAN_WARNING = 40.0
  }

  private val repScores = mutableListOf<Int>()
  private val leftAngleBuffer = ArrayDeque<Double>(5)
  private val rightAngleBuffer = ArrayDeque<Double>(5)

  override fun analyzeForm(person: Person?): FormFeedback {
    if (person == null) return FormFeedback.IDLE

    val checks = mutableListOf<FormCheck>()
    val jointHighlights = mutableMapOf<Int, FeedbackSeverity>()

    // Knee angle check (works for squat-type exercises)
    val leftKnee = AngleCalculator.getLeftKneeAngle(person)
    val rightKnee = AngleCalculator.getRightKneeAngle(person)
    val avgKnee = AngleCalculator.getAverageKneeAngle(person)

    // Elbow angle check (works for push-up type exercises)
    val leftElbow = AngleCalculator.getLeftElbowAngle(person)
    val rightElbow = AngleCalculator.getRightElbowAngle(person)
    val avgElbow = AngleCalculator.getAverageElbowAngle(person)

    // Determine which exercise by which joints are more active
    val kneeActive = avgKnee != null && avgKnee < 160.0
    val elbowActive = avgElbow != null && avgElbow < 150.0

    // Symmetry check for the primary exercise
    if (kneeActive && leftKnee != null && rightKnee != null) {
      val diff = abs(leftKnee - rightKnee)
      val severity =
        when {
          diff <= SYMMETRY_GOOD -> FeedbackSeverity.GOOD
          diff <= SYMMETRY_WARNING -> FeedbackSeverity.WARNING
          else -> FeedbackSeverity.ERROR
        }
      checks.add(
        FormCheck(
          "Symmetry",
          severity,
          if (severity == FeedbackSeverity.GOOD) "Balanced (${diff.toInt()}°)"
          else "Asymmetric (${diff.toInt()}° diff)",
          diff,
          0.0,
          if (diff <= SYMMETRY_GOOD) 100
          else (100 - diff * 3).toInt().coerceAtLeast(0)
        )
      )
      if (severity != FeedbackSeverity.GOOD) {
        val weak = if (leftKnee > rightKnee) BodyPart.LEFT_KNEE else BodyPart.RIGHT_KNEE
        jointHighlights[weak] = severity
      }
    } else if (elbowActive && leftElbow != null && rightElbow != null) {
      val diff = abs(leftElbow - rightElbow)
      val severity =
        when {
          diff <= SYMMETRY_GOOD -> FeedbackSeverity.GOOD
          diff <= SYMMETRY_WARNING -> FeedbackSeverity.WARNING
          else -> FeedbackSeverity.ERROR
        }
      checks.add(
        FormCheck(
          "Symmetry",
          severity,
          if (severity == FeedbackSeverity.GOOD) "Even arms (${diff.toInt()}°)"
          else "Uneven arms (${diff.toInt()}° diff)",
          diff,
          0.0,
          if (diff <= SYMMETRY_GOOD) 100
          else (100 - diff * 3).toInt().coerceAtLeast(0)
        )
      )
      if (severity != FeedbackSeverity.GOOD) {
        val weak = if (leftElbow > rightElbow) BodyPart.LEFT_ELBOW else BodyPart.RIGHT_ELBOW
        jointHighlights[weak] = severity
      }
    }

    // Torso alignment
    val torsoLean = calculateTorsoLean(person)
    if (torsoLean != null) {
      val severity =
        when {
          torsoLean <= LEAN_GOOD -> FeedbackSeverity.GOOD
          torsoLean <= LEAN_WARNING -> FeedbackSeverity.WARNING
          else -> FeedbackSeverity.ERROR
        }
      checks.add(
        FormCheck(
          "Alignment",
          severity,
          if (severity == FeedbackSeverity.GOOD) "Good posture"
          else "Leaning (${torsoLean.toInt()}°)",
          torsoLean,
          0.0,
          if (torsoLean <= LEAN_GOOD) 100
          else (100 - torsoLean * 2).toInt().coerceAtLeast(0)
        )
      )
      if (severity != FeedbackSeverity.GOOD) {
        jointHighlights[BodyPart.LEFT_SHOULDER] = severity
        jointHighlights[BodyPart.RIGHT_SHOULDER] = severity
      }
    }

    val overallScore =
      if (checks.isNotEmpty()) checks.map { it.score }.average().toInt() else 100
    val overallSeverity =
      when {
        overallScore >= 80 -> FeedbackSeverity.GOOD
        overallScore >= 55 -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }

    val primaryMessage =
      checks
        .filter { it.severity != FeedbackSeverity.GOOD }
        .maxByOrNull { if (it.severity == FeedbackSeverity.ERROR) 2 else 1 }
        ?.message
        ?: "Good form! ML classifier active"

    return FormFeedback(
      overallScore = overallScore,
      overallSeverity = overallSeverity,
      checks = checks,
      primaryMessage = primaryMessage,
      secondaryMessage = "Score: $overallScore",
      jointHighlights = jointHighlights
    )
  }

  override fun getAverageScore(): Int? =
    if (repScores.isNotEmpty()) repScores.average().toInt() else null

  override fun reset() {
    repScores.clear()
    leftAngleBuffer.clear()
    rightAngleBuffer.clear()
  }

  private fun calculateTorsoLean(person: Person): Double? {
    val ls = person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
    val rs = person.keypoints.getOrNull(BodyPart.RIGHT_SHOULDER)
    val lh = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
    val rh = person.keypoints.getOrNull(BodyPart.RIGHT_HIP)
    if (ls == null || rs == null || lh == null || rh == null) return null
    if (ls.score < 0.3f || rs.score < 0.3f || lh.score < 0.3f || rh.score < 0.3f) return null

    val smx = (ls.x + rs.x) / 2.0
    val smy = (ls.y + rs.y) / 2.0
    val hmx = (lh.x + rh.x) / 2.0
    val hmy = (lh.y + rh.y) / 2.0
    val dx = (smx - hmx).toFloat()
    val dy = (hmy - smy).toFloat()
    if (dy == 0f) return 90.0
    return Math.toDegrees(kotlin.math.abs(kotlin.math.atan2(dx.toDouble(), dy.toDouble())))
  }
}
