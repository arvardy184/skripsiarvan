package com.application.skripsiarvan.domain.exercise

import android.util.Log
import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.FeedbackSeverity
import com.application.skripsiarvan.domain.model.FormCheck
import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Person
import java.util.ArrayDeque
import kotlin.math.abs

/**
 * Real-time form analyzer for Push-up exercise.
 *
 * Evaluates 4 key aspects of push-up form:
 * 1. **Depth Quality**: How close the elbow angle gets to target (~90°)
 * 2. **Tempo**: Duration of eccentric (down) and concentric (up) phases
 * 3. **Symmetry**: Difference between left and right elbow angles
 * 4. **Body Line**: Hip alignment with shoulder-ankle line (sagging/piking)
 *
 * Scoring:
 * - Depth: 40%, Symmetry: 20%, Body Line: 30%, Tempo: 10%
 *
 * References:
 * - Standard push-up: elbow angle ≤ 90° at bottom (NSCA guidelines)
 * - Body should maintain straight line from shoulders to ankles
 */
class PushUpFormAnalyzer : FormAnalyzer {

  companion object {
    private const val TAG = "PushUpFormAnalyzer"

    // Depth thresholds (elbow angle)
    private const val DEPTH_EXCELLENT = 85.0 // Below 90°
    private const val DEPTH_GOOD = 95.0 // At or near 90°
    private const val DEPTH_FAIR = 115.0 // Partial push-up

    // Symmetry thresholds (left-right elbow angle difference)
    private const val SYMMETRY_GOOD = 10.0
    private const val SYMMETRY_WARNING = 20.0

    // Body line thresholds (hip deviation from shoulder-ankle line in degrees)
    private const val BODYLINE_GOOD = 15.0 // Slight deviation OK
    private const val BODYLINE_WARNING = 25.0 // Noticeable sagging/piking

    // Tempo thresholds (ms)
    private const val MIN_ECCENTRIC_MS = 800L // Minimum 0.8s going down
    private const val MIN_CONCENTRIC_MS = 600L // Minimum 0.6s going up

    // Weights
    private const val WEIGHT_DEPTH = 0.40
    private const val WEIGHT_SYMMETRY = 0.20
    private const val WEIGHT_BODYLINE = 0.30
    private const val WEIGHT_TEMPO = 0.10
  }

  // Tracking state
  private var currentPhase = PushUpPhase.UP
  private var phaseStartTime = 0L
  private var lastEccentricDuration = 0L
  private var lastConcentricDuration = 0L
  private var deepestAngle = 180.0
  private var lastLeftAngle: Double? = null
  private var lastRightAngle: Double? = null
  private var lastBodyLineDeviation: Double? = null

  // Rep history
  private val repScores = mutableListOf<Int>()

  // Smoothing
  private val leftAngleBuffer = ArrayDeque<Double>(5)
  private val rightAngleBuffer = ArrayDeque<Double>(5)

  private enum class PushUpPhase {
    UP,
    GOING_DOWN,
    AT_BOTTOM,
    GOING_UP
  }

  override fun analyzeForm(person: Person?): FormFeedback {
    if (person == null) return FormFeedback.IDLE

    val leftElbow = AngleCalculator.getLeftElbowAngle(person)
    val rightElbow = AngleCalculator.getRightElbowAngle(person)
    val avgElbowAngle = AngleCalculator.getAverageElbowAngle(person) ?: return FormFeedback.IDLE

    // Smooth
    leftElbow?.let { smoothAngle(leftAngleBuffer, it) }
    rightElbow?.let { smoothAngle(rightAngleBuffer, it) }
    val smoothedLeft = if (leftAngleBuffer.isNotEmpty()) leftAngleBuffer.average() else null
    val smoothedRight = if (rightAngleBuffer.isNotEmpty()) rightAngleBuffer.average() else null

    lastLeftAngle = smoothedLeft
    lastRightAngle = smoothedRight

    // Body line analysis
    val bodyLineDev = calculateBodyLineDeviation(person)
    lastBodyLineDeviation = bodyLineDev

    // Phase tracking
    val currentTime = System.currentTimeMillis()
    updatePhase(avgElbowAngle, currentTime)

    // Track deepest angle
    if (avgElbowAngle < deepestAngle &&
      (currentPhase == PushUpPhase.GOING_DOWN || currentPhase == PushUpPhase.AT_BOTTOM)
    ) {
      deepestAngle = avgElbowAngle
    }

    // Build checks
    val checks = mutableListOf<FormCheck>()
    val jointHighlights = mutableMapOf<Int, FeedbackSeverity>()

    // 1. Depth
    val depthCheck = evaluateDepth(avgElbowAngle)
    checks.add(depthCheck)
    val elbowSeverity = depthCheck.severity
    jointHighlights[BodyPart.LEFT_ELBOW] = elbowSeverity
    jointHighlights[BodyPart.RIGHT_ELBOW] = elbowSeverity

    // 2. Symmetry
    if (smoothedLeft != null && smoothedRight != null) {
      val symmetryCheck = evaluateSymmetry(smoothedLeft, smoothedRight)
      checks.add(symmetryCheck)
      if (symmetryCheck.severity != FeedbackSeverity.GOOD) {
        val weakerSide =
          if (smoothedLeft > smoothedRight) BodyPart.LEFT_ELBOW
          else BodyPart.RIGHT_ELBOW
        jointHighlights[weakerSide] = symmetryCheck.severity
      }
    }

    // 3. Body Line
    if (bodyLineDev != null) {
      val bodyLineCheck = evaluateBodyLine(bodyLineDev)
      checks.add(bodyLineCheck)
      if (bodyLineCheck.severity != FeedbackSeverity.GOOD) {
        jointHighlights[BodyPart.LEFT_HIP] = bodyLineCheck.severity
        jointHighlights[BodyPart.RIGHT_HIP] = bodyLineCheck.severity
      }
    }

    // 4. Tempo
    if (lastEccentricDuration > 0 || lastConcentricDuration > 0) {
      val tempoCheck = evaluateTempo()
      checks.add(tempoCheck)
    }

    // Overall score
    val overallScore = calculateOverallScore(checks)
    val overallSeverity =
      when {
        overallScore >= 80 -> FeedbackSeverity.GOOD
        overallScore >= 55 -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }

    val primaryMessage = generatePrimaryMessage(checks, currentPhase, avgElbowAngle)
    val secondaryMessage = generateSecondaryMessage(overallScore)

    return FormFeedback(
      overallScore = overallScore,
      overallSeverity = overallSeverity,
      checks = checks,
      primaryMessage = primaryMessage,
      secondaryMessage = secondaryMessage,
      jointHighlights = jointHighlights
    )
  }

  override fun getAverageScore(): Int? {
    return if (repScores.isNotEmpty()) repScores.average().toInt() else null
  }

  override fun reset() {
    currentPhase = PushUpPhase.UP
    phaseStartTime = 0L
    lastEccentricDuration = 0L
    lastConcentricDuration = 0L
    deepestAngle = 180.0
    lastLeftAngle = null
    lastRightAngle = null
    lastBodyLineDeviation = null
    repScores.clear()
    leftAngleBuffer.clear()
    rightAngleBuffer.clear()
  }

  // ─── Phase Tracking ──────────────────────────────────────────────

  private fun updatePhase(elbowAngle: Double, time: Long) {
    when (currentPhase) {
      PushUpPhase.UP -> {
        if (elbowAngle < 140.0) {
          currentPhase = PushUpPhase.GOING_DOWN
          phaseStartTime = time
          deepestAngle = elbowAngle
        }
      }
      PushUpPhase.GOING_DOWN -> {
        if (elbowAngle <= 100.0) {
          lastEccentricDuration = time - phaseStartTime
          currentPhase = PushUpPhase.AT_BOTTOM
          phaseStartTime = time
        } else if (elbowAngle > 155.0) {
          currentPhase = PushUpPhase.UP
        }
      }
      PushUpPhase.AT_BOTTOM -> {
        if (elbowAngle > 110.0) {
          currentPhase = PushUpPhase.GOING_UP
          phaseStartTime = time
        }
      }
      PushUpPhase.GOING_UP -> {
        if (elbowAngle >= 155.0) {
          lastConcentricDuration = time - phaseStartTime
          currentPhase = PushUpPhase.UP
          recordRepScore()
          deepestAngle = 180.0
        } else if (elbowAngle < 90.0) {
          currentPhase = PushUpPhase.AT_BOTTOM
        }
      }
    }
  }

  private fun recordRepScore() {
    val depthScore = calculateDepthScore(deepestAngle)
    val symmetryScore =
      if (lastLeftAngle != null && lastRightAngle != null) {
        calculateSymmetryScore(abs(lastLeftAngle!! - lastRightAngle!!))
      } else 100
    val bodyLineScore = lastBodyLineDeviation?.let { calculateBodyLineScore(it) } ?: 100
    val tempoScore = calculateTempoScore()

    val overall =
      (depthScore * WEIGHT_DEPTH +
        symmetryScore * WEIGHT_SYMMETRY +
        bodyLineScore * WEIGHT_BODYLINE +
        tempoScore * WEIGHT_TEMPO)
        .toInt()
        .coerceIn(0, 100)

    repScores.add(overall)
    Log.d(
      TAG,
      "Rep ${repScores.size} scored: $overall (D:$depthScore S:$symmetryScore B:$bodyLineScore T:$tempoScore)"
    )
  }

  // ─── Individual Checks ───────────────────────────────────────────

  private fun evaluateDepth(elbowAngle: Double): FormCheck {
    val score = calculateDepthScore(elbowAngle)
    val severity =
      when {
        elbowAngle <= DEPTH_GOOD -> FeedbackSeverity.GOOD
        elbowAngle <= DEPTH_FAIR -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }
    val message =
      when (severity) {
        FeedbackSeverity.GOOD -> "Great depth! ${String.format("%.0f", elbowAngle)}°"
        FeedbackSeverity.WARNING ->
          "Go lower! ${String.format("%.0f", elbowAngle)}° (target: <${DEPTH_GOOD.toInt()}°)"
        FeedbackSeverity.ERROR -> "Insufficient depth: ${String.format("%.0f", elbowAngle)}°"
      }
    return FormCheck("Depth", severity, message, elbowAngle, DEPTH_GOOD, score)
  }

  private fun evaluateSymmetry(leftAngle: Double, rightAngle: Double): FormCheck {
    val diff = abs(leftAngle - rightAngle)
    val score = calculateSymmetryScore(diff)
    val severity =
      when {
        diff <= SYMMETRY_GOOD -> FeedbackSeverity.GOOD
        diff <= SYMMETRY_WARNING -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }
    val weakerSide = if (leftAngle > rightAngle) "Left" else "Right"
    val message =
      when (severity) {
        FeedbackSeverity.GOOD -> "Even arms (${String.format("%.0f", diff)}° diff)"
        FeedbackSeverity.WARNING -> "$weakerSide arm lagging (${String.format("%.0f", diff)}°)"
        FeedbackSeverity.ERROR -> "Uneven! $weakerSide arm weak (${String.format("%.0f", diff)}°)"
      }
    return FormCheck("Symmetry", severity, message, diff, 0.0, score)
  }

  private fun evaluateBodyLine(deviation: Double): FormCheck {
    val score = calculateBodyLineScore(deviation)
    val severity =
      when {
        deviation <= BODYLINE_GOOD -> FeedbackSeverity.GOOD
        deviation <= BODYLINE_WARNING -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }
    val message =
      when (severity) {
        FeedbackSeverity.GOOD -> "Straight body line"
        FeedbackSeverity.WARNING -> "Hips sagging! Engage core"
        FeedbackSeverity.ERROR -> "Fix body position! Hips misaligned"
      }
    return FormCheck("Body Line", severity, message, deviation, 0.0, score)
  }

  private fun evaluateTempo(): FormCheck {
    val eccentricOk = lastEccentricDuration >= MIN_ECCENTRIC_MS
    val concentricOk = lastConcentricDuration >= MIN_CONCENTRIC_MS
    val score = calculateTempoScore()
    val severity =
      when {
        eccentricOk && concentricOk -> FeedbackSeverity.GOOD
        !eccentricOk && !concentricOk -> FeedbackSeverity.ERROR
        else -> FeedbackSeverity.WARNING
      }
    val message =
      when {
        severity == FeedbackSeverity.GOOD -> "Good tempo"
        !eccentricOk -> "Slow down! Lowering too fast"
        else -> "Control the push! Rising too fast"
      }
    return FormCheck(
      "Tempo",
      severity,
      message,
      (lastEccentricDuration + lastConcentricDuration).toDouble(),
      (MIN_ECCENTRIC_MS + MIN_CONCENTRIC_MS).toDouble(),
      score
    )
  }

  // ─── Score Calculations ──────────────────────────────────────────

  private fun calculateDepthScore(angle: Double): Int {
    return when {
      angle <= DEPTH_EXCELLENT -> 100
      angle <= DEPTH_GOOD -> 90
      angle <= DEPTH_FAIR ->
        (90 - ((angle - DEPTH_GOOD) / (DEPTH_FAIR - DEPTH_GOOD) * 40)).toInt()
      else -> (50 - ((angle - DEPTH_FAIR) / 30.0 * 50)).toInt().coerceAtLeast(0)
    }
  }

  private fun calculateSymmetryScore(diff: Double): Int {
    return when {
      diff <= SYMMETRY_GOOD -> 100
      diff <= SYMMETRY_WARNING ->
        (100 - ((diff - SYMMETRY_GOOD) / (SYMMETRY_WARNING - SYMMETRY_GOOD) * 30)).toInt()
      else -> (70 - ((diff - SYMMETRY_WARNING) / 15.0 * 70)).toInt().coerceAtLeast(0)
    }
  }

  private fun calculateBodyLineScore(deviation: Double): Int {
    return when {
      deviation <= BODYLINE_GOOD -> 100
      deviation <= BODYLINE_WARNING ->
        (100 - ((deviation - BODYLINE_GOOD) / (BODYLINE_WARNING - BODYLINE_GOOD) * 30)).toInt()
      else -> (70 - ((deviation - BODYLINE_WARNING) / 20.0 * 70)).toInt().coerceAtLeast(0)
    }
  }

  private fun calculateTempoScore(): Int {
    var score = 100
    if (lastEccentricDuration in 1 until MIN_ECCENTRIC_MS) {
      score -= ((MIN_ECCENTRIC_MS - lastEccentricDuration).toFloat() / MIN_ECCENTRIC_MS * 30).toInt()
    }
    if (lastConcentricDuration in 1 until MIN_CONCENTRIC_MS) {
      score -= ((MIN_CONCENTRIC_MS - lastConcentricDuration).toFloat() / MIN_CONCENTRIC_MS * 20).toInt()
    }
    return score.coerceIn(0, 100)
  }

  private fun calculateOverallScore(checks: List<FormCheck>): Int {
    if (checks.isEmpty()) return 100
    val depthScore = checks.find { it.name == "Depth" }?.score ?: 100
    val symmetryScore = checks.find { it.name == "Symmetry" }?.score ?: 100
    val bodyLineScore = checks.find { it.name == "Body Line" }?.score ?: 100
    val tempoScore = checks.find { it.name == "Tempo" }?.score ?: 100

    return (depthScore * WEIGHT_DEPTH +
      symmetryScore * WEIGHT_SYMMETRY +
      bodyLineScore * WEIGHT_BODYLINE +
      tempoScore * WEIGHT_TEMPO)
      .toInt()
      .coerceIn(0, 100)
  }

  // ─── Message Generation ──────────────────────────────────────────

  private fun generatePrimaryMessage(
    checks: List<FormCheck>,
    phase: PushUpPhase,
    angle: Double
  ): String {
    val worstCheck =
      checks.filter { it.severity != FeedbackSeverity.GOOD }.maxByOrNull {
        if (it.severity == FeedbackSeverity.ERROR) 2 else 1
      }

    if (worstCheck != null) return worstCheck.message

    return when (phase) {
      PushUpPhase.UP -> "Ready — Arms locked out"
      PushUpPhase.GOING_DOWN -> "Lowering... controlled"
      PushUpPhase.AT_BOTTOM -> "Hold! ${String.format("%.0f", angle)}°"
      PushUpPhase.GOING_UP -> "Push! Drive through palms"
    }
  }

  private fun generateSecondaryMessage(overallScore: Int): String {
    val avgScore = getAverageScore()
    return if (avgScore != null) {
      "Score: $overallScore | Avg: $avgScore (${repScores.size} reps)"
    } else {
      "Score: $overallScore"
    }
  }

  // ─── Utility ─────────────────────────────────────────────────────

  /**
   * Calculate how much the hips deviate from the shoulder-ankle line. Measures if the body
   * maintains a plank-like straight line.
   */
  private fun calculateBodyLineDeviation(person: Person): Double? {
    val shoulder =
      person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
        ?: person.keypoints.getOrNull(BodyPart.RIGHT_SHOULDER)
    val hip =
      person.keypoints.getOrNull(BodyPart.LEFT_HIP)
        ?: person.keypoints.getOrNull(BodyPart.RIGHT_HIP)
    val ankle =
      person.keypoints.getOrNull(BodyPart.LEFT_ANKLE)
        ?: person.keypoints.getOrNull(BodyPart.RIGHT_ANKLE)

    if (shoulder == null || hip == null || ankle == null) return null
    val minConf = 0.3f
    if (shoulder.score < minConf || hip.score < minConf || ankle.score < minConf) return null

    // Calculate the angle at hip (shoulder-hip-ankle)
    // A perfectly straight body line = 180°
    // Deviation = abs(180 - angle)
    val bodyAngle = AngleCalculator.calculateAngle(shoulder, hip, ankle)
    return abs(180.0 - bodyAngle)
  }

  private fun smoothAngle(buffer: ArrayDeque<Double>, value: Double): Double {
    if (buffer.size >= 5) buffer.removeFirst()
    buffer.addLast(value)
    return buffer.average()
  }
}
