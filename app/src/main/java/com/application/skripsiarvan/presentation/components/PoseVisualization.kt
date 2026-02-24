package com.application.skripsiarvan.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.FeedbackSeverity
import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Keypoint
import com.application.skripsiarvan.domain.model.Person
import kotlin.math.atan2
import kotlin.math.min

/**
 * Enhanced composable that draws pose skeleton over camera preview with form feedback
 * visualization:
 * - Color-coded skeleton bones based on form quality
 * - Highlighted joints with severity-based colors
 * - Angle arc indicators at key joints (knees, elbows)
 * - Pulsing glow effect on problematic joints
 */
@Composable
fun PoseVisualization(
        person: Person?,
        viewWidth: Float,
        viewHeight: Float,
        formFeedback: FormFeedback? = null,
        modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        person?.let { drawPoseSkeleton(it, viewWidth, viewHeight, formFeedback) }
    }
}

// ─── Color Palette ───────────────────────────────────────────────

private object PoseColors {
    // Severity-based colors
    val GOOD = Color(0xFF00E676) // Vibrant green
    val GOOD_GLOW = Color(0x4000E676) // Green glow
    val WARNING = Color(0xFFFFD600) // Amber yellow
    val WARNING_GLOW = Color(0x40FFD600) // Yellow glow
    val ERROR = Color(0xFFFF1744) // Red accent
    val ERROR_GLOW = Color(0x40FF1744) // Red glow

    // Default skeleton colors
    val BONE_DEFAULT = Color(0xFF00E5FF) // Cyan
    val JOINT_DEFAULT = Color(0xFF00E676) // Green
    val JOINT_BORDER = Color(0xFFFFFFFF) // White border

    // Arc/angle indicator
    val ARC_FILL = Color(0x6000E5FF) // Semi-transparent cyan
    val ARC_STROKE = Color(0xFF00E5FF) // Cyan

    fun forSeverity(severity: FeedbackSeverity): Color =
            when (severity) {
                FeedbackSeverity.GOOD -> GOOD
                FeedbackSeverity.WARNING -> WARNING
                FeedbackSeverity.ERROR -> ERROR
            }

    fun glowForSeverity(severity: FeedbackSeverity): Color =
            when (severity) {
                FeedbackSeverity.GOOD -> GOOD_GLOW
                FeedbackSeverity.WARNING -> WARNING_GLOW
                FeedbackSeverity.ERROR -> ERROR_GLOW
            }
}

// ─── Main Drawing ────────────────────────────────────────────────

private fun DrawScope.drawPoseSkeleton(
        person: Person,
        viewWidth: Float,
        viewHeight: Float,
        formFeedback: FormFeedback?
) {
    val keypoints = person.keypoints
    val highlights = formFeedback?.jointHighlights ?: emptyMap()

    // Layer 1: Glow effects on highlighted joints (drawn first, behind everything)
    if (highlights.isNotEmpty()) {
        drawJointGlows(keypoints, viewWidth, viewHeight, highlights)
    }

    // Layer 2: Connections (bones)
    drawConnections(keypoints, viewWidth, viewHeight, highlights)

    // Layer 3: Angle arcs at key joints
    drawAngleArcs(keypoints, viewWidth, viewHeight, highlights)

    // Layer 4: Keypoints (joints) — drawn on top
    drawKeypoints(keypoints, viewWidth, viewHeight, highlights)

    // Layer 5: Angle value labels at key joints
    drawAngleLabels(keypoints, viewWidth, viewHeight)
}

// ─── Glow Effects ────────────────────────────────────────────────

private fun DrawScope.drawJointGlows(
        keypoints: List<Keypoint>,
        viewWidth: Float,
        viewHeight: Float,
        highlights: Map<Int, FeedbackSeverity>
) {
    val minConfidence = 0.3f

    highlights.forEach { (jointIdx, severity) ->
        if (severity == FeedbackSeverity.GOOD) return@forEach // Skip glow for good joints

        val kp = keypoints.getOrNull(jointIdx) ?: return@forEach
        if (kp.score < minConfidence) return@forEach

        val center = Offset(kp.x * viewWidth, kp.y * viewHeight)
        val glowColor = PoseColors.glowForSeverity(severity)
        val glowRadius = if (severity == FeedbackSeverity.ERROR) 40f else 28f

        // Outer glow
        drawCircle(color = glowColor, radius = glowRadius, center = center)
        // Inner glow (stronger)
        drawCircle(
                color = glowColor.copy(alpha = 0.4f),
                radius = glowRadius * 0.6f,
                center = center
        )
    }
}

// ─── Connections (Bones) ─────────────────────────────────────────

private fun DrawScope.drawConnections(
        keypoints: List<Keypoint>,
        viewWidth: Float,
        viewHeight: Float,
        highlights: Map<Int, FeedbackSeverity>
) {
    val connections =
            listOf(
                    // Head
                    Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
                    Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
                    Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
                    Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),

                    // Torso
                    Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
                    Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
                    Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
                    Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),

                    // Left arm
                    Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
                    Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),

                    // Right arm
                    Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
                    Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),

                    // Left leg
                    Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
                    Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),

                    // Right leg
                    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
                    Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
            )

    val minConfidence = 0.3f

    connections.forEach { (startIdx, endIdx) ->
        val start = keypoints.getOrNull(startIdx)
        val end = keypoints.getOrNull(endIdx)

        if (start != null &&
                        end != null &&
                        start.score >= minConfidence &&
                        end.score >= minConfidence
        ) {
            // Determine bone color based on connected joint severity
            val startSeverity = highlights[startIdx]
            val endSeverity = highlights[endIdx]

            val boneColor =
                    when {
                        startSeverity == FeedbackSeverity.ERROR ||
                                endSeverity == FeedbackSeverity.ERROR -> PoseColors.ERROR
                        startSeverity == FeedbackSeverity.WARNING ||
                                endSeverity == FeedbackSeverity.WARNING -> PoseColors.WARNING
                        startSeverity == FeedbackSeverity.GOOD ||
                                endSeverity == FeedbackSeverity.GOOD -> PoseColors.GOOD
                        else -> PoseColors.BONE_DEFAULT
                    }

            val startOffset = Offset(start.x * viewWidth, start.y * viewHeight)
            val endOffset = Offset(end.x * viewWidth, end.y * viewHeight)

            // Shadow/outline stroke (for visibility)
            drawLine(
                    color = Color.Black.copy(alpha = 0.5f),
                    start = startOffset,
                    end = endOffset,
                    strokeWidth = 12f,
                    cap = StrokeCap.Round
            )

            // Main bone stroke
            drawLine(
                    color = boneColor,
                    start = startOffset,
                    end = endOffset,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
            )
        }
    }
}

// ─── Keypoints (Joints) ──────────────────────────────────────────

private fun DrawScope.drawKeypoints(
        keypoints: List<Keypoint>,
        viewWidth: Float,
        viewHeight: Float,
        highlights: Map<Int, FeedbackSeverity>
) {
    val minConfidence = 0.3f
    val defaultRadius = 10f
    val highlightedRadius = 14f

    keypoints.forEachIndexed { index, keypoint ->
        if (keypoint.score >= minConfidence) {
            val center = Offset(x = keypoint.x * viewWidth, y = keypoint.y * viewHeight)

            val severity = highlights[index]
            val isHighlighted = severity != null
            val radius = if (isHighlighted) highlightedRadius else defaultRadius

            // White border
            drawCircle(color = PoseColors.JOINT_BORDER, radius = radius + 2f, center = center)

            // Colored fill
            val fillColor =
                    if (isHighlighted) {
                        PoseColors.forSeverity(severity!!)
                    } else {
                        PoseColors.JOINT_DEFAULT
                    }

            drawCircle(color = fillColor, radius = radius, center = center)
        }
    }
}

// ─── Angle Arc Indicators ────────────────────────────────────────

private fun DrawScope.drawAngleArcs(
        keypoints: List<Keypoint>,
        viewWidth: Float,
        viewHeight: Float,
        highlights: Map<Int, FeedbackSeverity>
) {
    val minConfidence = 0.3f

    // Knee angles (for squat)
    drawAngleArc(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.LEFT_HIP,
            BodyPart.LEFT_KNEE,
            BodyPart.LEFT_ANKLE,
            highlights[BodyPart.LEFT_KNEE],
            minConfidence
    )
    drawAngleArc(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.RIGHT_HIP,
            BodyPart.RIGHT_KNEE,
            BodyPart.RIGHT_ANKLE,
            highlights[BodyPart.RIGHT_KNEE],
            minConfidence
    )

    // Elbow angles (for push-up)
    drawAngleArc(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.LEFT_SHOULDER,
            BodyPart.LEFT_ELBOW,
            BodyPart.LEFT_WRIST,
            highlights[BodyPart.LEFT_ELBOW],
            minConfidence
    )
    drawAngleArc(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.RIGHT_SHOULDER,
            BodyPart.RIGHT_ELBOW,
            BodyPart.RIGHT_WRIST,
            highlights[BodyPart.RIGHT_ELBOW],
            minConfidence
    )
}

private fun DrawScope.drawAngleArc(
        keypoints: List<Keypoint>,
        viewWidth: Float,
        viewHeight: Float,
        firstIdx: Int,
        middleIdx: Int,
        lastIdx: Int,
        severity: FeedbackSeverity?,
        minConfidence: Float
) {
    val first = keypoints.getOrNull(firstIdx)
    val middle = keypoints.getOrNull(middleIdx)
    val last = keypoints.getOrNull(lastIdx)

    if (first == null || middle == null || last == null) return
    if (first.score < minConfidence || middle.score < minConfidence || last.score < minConfidence)
            return

    val middlePos = Offset(middle.x * viewWidth, middle.y * viewHeight)
    val firstPos = Offset(first.x * viewWidth, first.y * viewHeight)
    val lastPos = Offset(last.x * viewWidth, last.y * viewHeight)

    // Calculate angles for arc drawing
    val angle1 = atan2((firstPos.y - middlePos.y).toDouble(), (firstPos.x - middlePos.x).toDouble())
    val angle2 = atan2((lastPos.y - middlePos.y).toDouble(), (lastPos.x - middlePos.x).toDouble())

    // Arc radius proportional to view size (but small)
    val arcRadius = min(viewWidth, viewHeight) * 0.04f

    val arcColor =
            if (severity != null) {
                PoseColors.forSeverity(severity).copy(alpha = 0.6f)
            } else {
                PoseColors.ARC_FILL
            }

    val strokeColor =
            if (severity != null) {
                PoseColors.forSeverity(severity)
            } else {
                PoseColors.ARC_STROKE
            }

    // Convert to degrees for drawArc
    val startAngle = Math.toDegrees(angle1).toFloat()
    var sweepAngle = Math.toDegrees(angle2 - angle1).toFloat()

    // Normalize sweep angle to smallest arc
    while (sweepAngle > 180f) sweepAngle -= 360f
    while (sweepAngle < -180f) sweepAngle += 360f

    val topLeft = Offset(middlePos.x - arcRadius, middlePos.y - arcRadius)
    val arcSize = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2)

    // Draw filled arc
    drawArc(
            color = arcColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize
    )

    // Draw arc outline
    drawArc(
            color = strokeColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = 2f)
    )
}

// ─── Angle Labels ────────────────────────────────────────────────

private fun DrawScope.drawAngleLabels(
        keypoints: List<Keypoint>,
        viewWidth: Float,
        viewHeight: Float
) {
    val minConfidence = 0.3f
    val paint =
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
                setShadowLayer(6f, 2f, 2f, android.graphics.Color.BLACK)
            }

    // Draw knee angle labels
    drawAngleLabel(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.LEFT_HIP,
            BodyPart.LEFT_KNEE,
            BodyPart.LEFT_ANKLE,
            paint,
            minConfidence
    )
    drawAngleLabel(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.RIGHT_HIP,
            BodyPart.RIGHT_KNEE,
            BodyPart.RIGHT_ANKLE,
            paint,
            minConfidence
    )

    // Draw elbow angle labels
    drawAngleLabel(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.LEFT_SHOULDER,
            BodyPart.LEFT_ELBOW,
            BodyPart.LEFT_WRIST,
            paint,
            minConfidence
    )
    drawAngleLabel(
            keypoints,
            viewWidth,
            viewHeight,
            BodyPart.RIGHT_SHOULDER,
            BodyPart.RIGHT_ELBOW,
            BodyPart.RIGHT_WRIST,
            paint,
            minConfidence
    )
}

private fun DrawScope.drawAngleLabel(
        keypoints: List<Keypoint>,
        viewWidth: Float,
        viewHeight: Float,
        firstIdx: Int,
        middleIdx: Int,
        lastIdx: Int,
        paint: android.graphics.Paint,
        minConfidence: Float
) {
    val first = keypoints.getOrNull(firstIdx) ?: return
    val middle = keypoints.getOrNull(middleIdx) ?: return
    val last = keypoints.getOrNull(lastIdx) ?: return

    if (first.score < minConfidence || middle.score < minConfidence || last.score < minConfidence)
            return

    // Calculate angle
    val angle =
            com.application.skripsiarvan.domain.exercise.AngleCalculator.calculateAngle(
                    first,
                    middle,
                    last
            )

    val middlePos = Offset(middle.x * viewWidth, middle.y * viewHeight)

    // Offset label slightly from joint
    val labelX = middlePos.x + 20f
    val labelY = middlePos.y - 10f

    drawContext.canvas.nativeCanvas.drawText("${angle.toInt()}°", labelX, labelY, paint)
}
