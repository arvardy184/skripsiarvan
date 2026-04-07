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
 * Real-time form analyzer for Squat exercise.
 *
 * Evaluates 4 key aspects of squat form:
 * 1. **Depth Quality**: How close the knee angle gets to target (~100°)
 * 2. **Tempo**: Duration of eccentric (down) and concentric (up) phases
 * 3. **Symmetry**: Difference between left and right knee angles
 * 4. **Alignment**: Torso lean angle (shoulder-hip vertical alignment)
 *
 * Scoring:
 * - Each check contributes to a weighted overall score
 * - Depth: 40%, Symmetry: 25%, Alignment: 25%, Tempo: 10%
 *
 * References:
 * - Parallel squat standard: knee angle ≤ 100°
 * - Acceptable asymmetry: < 10° difference (Schoenfeld, 2010)
 * - Torso lean: ideally < 30° from vertical
 */
class SquatFormAnalyzer : FormAnalyzer {

  companion object {
    private const val TAG = "SquatFormAnalyzer"

    // Depth thresholds
    private const val DEPTH_EXCELLENT = 95.0 // Deep squat
    private const val DEPTH_GOOD = 105.0 // Parallel squat
    private const val DEPTH_FAIR = 120.0 // Partial squat
    // Above 120° = insufficient depth

    // Symmetry thresholds (left-right angle difference)
    private const val SYMMETRY_GOOD = 8.0 // < 8° difference
    private const val SYMMETRY_WARNING = 15.0 // < 15° difference

    // Torso alignment thresholds (lean angle from vertical)
    private const val LEAN_GOOD = 25.0 // Slight lean OK
    private const val LEAN_WARNING = 40.0 // Excessive forward lean

    // Tempo thresholds (ms)
    private const val MIN_ECCENTRIC_MS = 1000L // Minimum 1s going down
    private const val MIN_CONCENTRIC_MS = 800L // Minimum 0.8s going up

    // Weights for overall score
    private const val WEIGHT_DEPTH = 0.40
    private const val WEIGHT_SYMMETRY = 0.25
    private const val WEIGHT_ALIGNMENT = 0.25
    private const val WEIGHT_TEMPO = 0.10
  }

  // Tracking state
  private var currentPhase = SquatPhase.STANDING
  private var phaseStartTime = 0L
  private var lastEccentricDuration = 0L
  private var lastConcentricDuration = 0L
  private var deepestAngle = 180.0 // Track deepest angle in current rep
  private var lastLeftAngle: Double? = null
  private var lastRightAngle: Double? = null
  private var lastTorsoLean: Double? = null

  // Rep history for running average
  private val repScores = mutableListOf<Int>()

  // Smoothing buffers
  private val leftAngleBuffer = ArrayDeque<Double>(5)
  private val rightAngleBuffer = ArrayDeque<Double>(5)

  private enum class SquatPhase {
    STANDING,
    GOING_DOWN,
    AT_BOTTOM,
    GOING_UP
  }

  override fun analyzeForm(person: Person?): FormFeedback {
    if (person == null) return FormFeedback.IDLE

    // Get individual knee angles (not averaged — we need both for symmetry)
    val leftKnee = AngleCalculator.getLeftKneeAngle(person)
    val rightKnee = AngleCalculator.getRightKneeAngle(person)
    val avgKneeAngle = AngleCalculator.getAverageKneeAngle(person) ?: return FormFeedback.IDLE

    // Smooth individual angles
    leftKnee?.let { smoothAngle(leftAngleBuffer, it) }
    rightKnee?.let { smoothAngle(rightAngleBuffer, it) }
    val smoothedLeft = if (leftAngleBuffer.isNotEmpty()) leftAngleBuffer.average() else null
    val smoothedRight = if (rightAngleBuffer.isNotEmpty()) rightAngleBuffer.average() else null

    lastLeftAngle = smoothedLeft
    lastRightAngle = smoothedRight

    // Calculate torso lean
    val torsoLean = calculateTorsoLean(person)
    lastTorsoLean = torsoLean

    // Track phase and timing
    val currentTime = System.currentTimeMillis()
    updatePhase(avgKneeAngle, currentTime)

    // Track deepest angle
    if (avgKneeAngle < deepestAngle &&
      (currentPhase == SquatPhase.GOING_DOWN || currentPhase == SquatPhase.AT_BOTTOM)
    ) {
      deepestAngle = avgKneeAngle
    }

    // Build checks
    val checks = mutableListOf<FormCheck>()
    val jointHighlights = mutableMapOf<Int, FeedbackSeverity>()

    // 1. Depth Check
    val depthCheck = evaluateDepth(avgKneeAngle)
    checks.add(depthCheck)
    // Highlight knees based on depth
    val kneeSeverity = depthCheck.severity
    jointHighlights[BodyPart.LEFT_KNEE] = kneeSeverity
    jointHighlights[BodyPart.RIGHT_KNEE] = kneeSeverity

    // 2. Symmetry Check
    if (smoothedLeft != null && smoothedRight != null) {
      val symmetryCheck = evaluateSymmetry(smoothedLeft, smoothedRight)
      checks.add(symmetryCheck)
      // Highlight the weaker side
      if (symmetryCheck.severity != FeedbackSeverity.GOOD) {
        val weakerSide =
          if (smoothedLeft > smoothedRight) {
            // Left knee less bent = weaker
            BodyPart.LEFT_KNEE
          } else {
            BodyPart.RIGHT_KNEE
          }
        jointHighlights[weakerSide] = symmetryCheck.severity
      }
    }

    // 3. Alignment Check (Torso Lean)
    if (torsoLean != null) {
      val alignmentCheck = evaluateAlignment(torsoLean)
      checks.add(alignmentCheck)
      if (alignmentCheck.severity != FeedbackSeverity.GOOD) {
        jointHighlights[BodyPart.LEFT_SHOULDER] = alignmentCheck.severity
        jointHighlights[BodyPart.RIGHT_SHOULDER] = alignmentCheck.severity
      }
    }

    // 4. Tempo Check (only if we have timing data)
    if (lastEccentricDuration > 0 || lastConcentricDuration > 0) {
      val tempoCheck = evaluateTempo()
      checks.add(tempoCheck)
    }

    // Calculate overall score
    val overallScore = calculateOverallScore(checks)
    val overallSeverity =
      when {
        overallScore >= 80 -> FeedbackSeverity.GOOD
        overallScore >= 55 -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }

    // Generate primary message (most critical feedback)
    val primaryMessage = generatePrimaryMessage(checks, currentPhase, avgKneeAngle)
    val secondaryMessage = generateSecondaryMessage(checks, overallScore)

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
    currentPhase = SquatPhase.STANDING
    phaseStartTime = 0L
    lastEccentricDuration = 0L
    lastConcentricDuration = 0L
    deepestAngle = 180.0
    lastLeftAngle = null
    lastRightAngle = null
    lastTorsoLean = null
    repScores.clear()
    leftAngleBuffer.clear()
    rightAngleBuffer.clear()
  }

  // ─── Phase Tracking ──────────────────────────────────────────────

  private fun updatePhase(kneeAngle: Double, time: Long) {
    when (currentPhase) {
      SquatPhase.STANDING -> {
        if (kneeAngle < 150.0) {
          currentPhase = SquatPhase.GOING_DOWN
          phaseStartTime = time
          deepestAngle = kneeAngle
        }
      }
      SquatPhase.GOING_DOWN -> {
        if (kneeAngle <= 110.0) {
          lastEccentricDuration = time - phaseStartTime
          currentPhase = SquatPhase.AT_BOTTOM
          phaseStartTime = time
        } else if (kneeAngle > 160.0) {
          // Aborted, reset
          currentPhase = SquatPhase.STANDING
        }
      }
      SquatPhase.AT_BOTTOM -> {
        if (kneeAngle > 120.0) {
          currentPhase = SquatPhase.GOING_UP
          phaseStartTime = time
        }
      }
      SquatPhase.GOING_UP -> {
        if (kneeAngle >= 160.0) {
          lastConcentricDuration = time - phaseStartTime
          currentPhase = SquatPhase.STANDING
          // Record this rep's score
          recordRepScore()
          deepestAngle = 180.0
        } else if (kneeAngle < 100.0) {
          currentPhase = SquatPhase.AT_BOTTOM
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
    val alignmentScore = lastTorsoLean?.let { calculateAlignmentScore(it) } ?: 100
    val tempoScore = calculateTempoScore()

    val overall =
      (depthScore * WEIGHT_DEPTH +
        symmetryScore * WEIGHT_SYMMETRY +
        alignmentScore * WEIGHT_ALIGNMENT +
        tempoScore * WEIGHT_TEMPO)
        .toInt()
        .coerceIn(0, 100)

    repScores.add(overall)
    Log.d(
      TAG,
      "Rep ${repScores.size} scored: $overall (D:$depthScore S:$symmetryScore A:$alignmentScore T:$tempoScore)"
    )
  }

  // ─── Individual Checks ───────────────────────────────────────────

  private fun evaluateDepth(kneeAngle: Double): FormCheck {
    val score = calculateDepthScore(kneeAngle)
    val severity =
      when {
        kneeAngle <= DEPTH_GOOD -> FeedbackSeverity.GOOD
        kneeAngle <= DEPTH_FAIR -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }
    val message =
      when (severity) {
        FeedbackSeverity.GOOD -> "Great depth! ${String.format("%.0f", kneeAngle)}°"
        FeedbackSeverity.WARNING ->
          "Go deeper! ${String.format("%.0f", kneeAngle)}° (target: <${DEPTH_GOOD.toInt()}°)"
        FeedbackSeverity.ERROR -> "Insufficient depth: ${String.format("%.0f", kneeAngle)}°"
      }
    return FormCheck(
      name = "Depth",
      severity = severity,
      message = message,
      value = kneeAngle,
      target = DEPTH_GOOD,
      score = score
    )
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
        FeedbackSeverity.GOOD -> "Balanced form (${String.format("%.0f", diff)}° diff)"
        FeedbackSeverity.WARNING -> "$weakerSide side lagging (${String.format("%.0f", diff)}°)"
        FeedbackSeverity.ERROR ->
          "Asymmetric! $weakerSide side weak (${String.format("%.0f", diff)}°)"
      }
    return FormCheck(
      name = "Symmetry",
      severity = severity,
      message = message,
      value = diff,
      target = 0.0,
      score = score
    )
  }

  private fun evaluateAlignment(torsoLean: Double): FormCheck {
    val score = calculateAlignmentScore(torsoLean)
    val severity =
      when {
        torsoLean <= LEAN_GOOD -> FeedbackSeverity.GOOD
        torsoLean <= LEAN_WARNING -> FeedbackSeverity.WARNING
        else -> FeedbackSeverity.ERROR
      }
    val message =
      when (severity) {
        FeedbackSeverity.GOOD -> "Good posture (${String.format("%.0f", torsoLean)}° lean)"
        FeedbackSeverity.WARNING -> "Leaning forward (${String.format("%.0f", torsoLean)}°)"
        FeedbackSeverity.ERROR ->
          "Excessive lean! (${String.format("%.0f", torsoLean)}°) Keep chest up"
      }
    return FormCheck(
      name = "Alignment",
      severity = severity,
      message = message,
      value = torsoLean,
      target = 0.0,
      score = score
    )
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
        !eccentricOk -> "Slow down! Going down too fast (${lastEccentricDuration}ms)"
        else -> "Control the ascent! Rising too fast (${lastConcentricDuration}ms)"
      }
    return FormCheck(
      name = "Tempo",
      severity = severity,
      message = message,
      value = (lastEccentricDuration + lastConcentricDuration).toDouble(),
      target = (MIN_ECCENTRIC_MS + MIN_CONCENTRIC_MS).toDouble(),
      score = score
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

  private fun calculateAlignmentScore(lean: Double): Int {
    return when {
      lean <= LEAN_GOOD -> 100
      lean <= LEAN_WARNING ->
        (100 - ((lean - LEAN_GOOD) / (LEAN_WARNING - LEAN_GOOD) * 30)).toInt()
      else -> (70 - ((lean - LEAN_WARNING) / 20.0 * 70)).toInt().coerceAtLeast(0)
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
    val alignmentScore = checks.find { it.name == "Alignment" }?.score ?: 100
    val tempoScore = checks.find { it.name == "Tempo" }?.score ?: 100

    return (depthScore * WEIGHT_DEPTH +
      symmetryScore * WEIGHT_SYMMETRY +
      alignmentScore * WEIGHT_ALIGNMENT +
      tempoScore * WEIGHT_TEMPO)
      .toInt()
      .coerceIn(0, 100)
  }


  private fun generatePrimaryMessage(
    checks: List<FormCheck>,
    phase: SquatPhase,
    angle: Double
  ): String {

    val worstCheck =
      checks.filter { it.severity != FeedbackSeverity.GOOD }.maxByOrNull {
        if (it.severity == FeedbackSeverity.ERROR) 2 else 1
      }

    if (worstCheck != null) return worstCheck.message

    // If all good, show phase-appropriate encouragement
    return when (phase) {
      SquatPhase.STANDING -> "Ready — Stand tall"
      SquatPhase.GOING_DOWN -> "Going down... controlled"
      SquatPhase.AT_BOTTOM -> "Hold! Great depth ${String.format("%.0f", angle)}°"
      SquatPhase.GOING_UP -> "Drive up! Push through heels"
    }
  }

  private fun generateSecondaryMessage(checks: List<FormCheck>, overallScore: Int): String {
    val avgScore = getAverageScore()
    return if (avgScore != null) {
      "Score: $overallScore | Avg: $avgScore (${repScores.size} reps)"
    } else {
      "Score: $overallScore"
    }
  }



  private fun calculateTorsoLean(person: Person): Double? {
    val leftShoulder = person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
    val rightShoulder = person.keypoints.getOrNull(BodyPart.RIGHT_SHOULDER)
    val leftHip = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
    val rightHip = person.keypoints.getOrNull(BodyPart.RIGHT_HIP)

    if (leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null)
      return null

    val minConf = 0.3f
    if (leftShoulder.score < minConf ||
      rightShoulder.score < minConf ||
      leftHip.score < minConf ||
      rightHip.score < minConf
    ) return null

    // Midpoint of shoulders and hips
    val shoulderMidX = (leftShoulder.x + rightShoulder.x) / 2.0
    val shoulderMidY = (leftShoulder.y + rightShoulder.y) / 2.0
    val hipMidX = (leftHip.x + rightHip.x) / 2.0
    val hipMidY = (leftHip.y + rightHip.y) / 2.0

    // Calculate angle from vertical (lean forward/backward)
    // In image coordinates, Y increases downward
    val dx = (shoulderMidX - hipMidX).toFloat()
    val dy = (hipMidY - shoulderMidY).toFloat() // Inverted because Y goes down

    if (dy == 0f) return 90.0

    val angleRad = kotlin.math.atan2(dx.toDouble(), dy.toDouble())
    return Math.toDegrees(kotlin.math.abs(angleRad))
  }

  private fun smoothAngle(buffer: ArrayDeque<Double>, value: Double): Double {
    if (buffer.size >= 5) buffer.removeFirst()
    buffer.addLast(value)
    return buffer.average()
  }
}
