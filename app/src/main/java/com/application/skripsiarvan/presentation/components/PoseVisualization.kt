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
        sourceAspectRatio: Float? = null,
        formFeedback: FormFeedback? = null,
        debugMode: Boolean = false,
        modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        person?.let {
            val transform = calculateFitCenterTransform(viewWidth, viewHeight, sourceAspectRatio)
            drawPoseSkeleton(it, transform, formFeedback)
            if (debugMode) drawDebugLabels(it, transform)
        }
    }
}

// Threshold minimum keypoint confidence untuk ditampilkan di skeleton.
// Harus konsisten dengan CONFIDENCE_THRESHOLD di detektor (BlazePose & MoveNet = 0.5f).
// Keypoint dengan score di bawah ini TIDAK digambar — mencegah joint salah posisi ikut terhubung.
private const val MIN_DRAW_CONFIDENCE = 0.15f

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
        transform: PoseRenderTransform,
        formFeedback: FormFeedback?
) {
    val keypoints = person.keypoints
    val highlights = formFeedback?.jointHighlights ?: emptyMap()

    // Layer 1: Glow effects on highlighted joints (drawn first, behind everything)
    if (highlights.isNotEmpty()) {
        drawJointGlows(keypoints, transform, highlights)
    }

    // Layer 2: Connections (bones)
    drawConnections(keypoints, transform, highlights)

    // Layer 3: Angle arcs at key joints
    drawAngleArcs(keypoints, transform, highlights)

    // Layer 4: Keypoints (joints) — drawn on top
    drawKeypoints(keypoints, transform, highlights)

    // Layer 5: Angle value labels at key joints
    drawAngleLabels(keypoints, transform)
}

private data class PoseRenderTransform(
        val offsetX: Float,
        val offsetY: Float,
        val width: Float,
        val height: Float
) {
    val minDimension: Float = min(width, height)

    fun point(keypoint: Keypoint): Offset =
            Offset(
                    x = offsetX + keypoint.x * width,
                    y = offsetY + keypoint.y * height
            )
}

private fun calculateFitCenterTransform(
        viewWidth: Float,
        viewHeight: Float,
        sourceAspectRatio: Float?
): PoseRenderTransform {
    if (viewWidth <= 0f || viewHeight <= 0f) {
        return PoseRenderTransform(0f, 0f, viewWidth, viewHeight)
    }

    val aspectRatio =
            sourceAspectRatio?.takeIf { it.isFinite() && it > 0f }
                    ?: return PoseRenderTransform(0f, 0f, viewWidth, viewHeight)

    val viewAspectRatio = viewWidth / viewHeight
    return if (viewAspectRatio > aspectRatio) {
        val contentHeight = viewHeight
        val contentWidth = contentHeight * aspectRatio
        PoseRenderTransform(
                offsetX = (viewWidth - contentWidth) / 2f,
                offsetY = 0f,
                width = contentWidth,
                height = contentHeight
        )
    } else {
        val contentWidth = viewWidth
        val contentHeight = contentWidth / aspectRatio
        PoseRenderTransform(
                offsetX = 0f,
                offsetY = (viewHeight - contentHeight) / 2f,
                width = contentWidth,
                height = contentHeight
        )
    }
}

// ─── Glow Effects ────────────────────────────────────────────────

private fun DrawScope.drawJointGlows(
        keypoints: List<Keypoint>,
        transform: PoseRenderTransform,
        highlights: Map<Int, FeedbackSeverity>
) {
    val minConfidence = MIN_DRAW_CONFIDENCE

    highlights.forEach { (jointIdx, severity) ->
        if (severity == FeedbackSeverity.GOOD) return@forEach // Skip glow for good joints

        val kp = keypoints.getOrNull(jointIdx) ?: return@forEach
        if (kp.score < minConfidence) return@forEach

        val center = transform.point(kp)
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
        transform: PoseRenderTransform,
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

    val minConfidence = MIN_DRAW_CONFIDENCE

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

            val startOffset = transform.point(start)
            val endOffset = transform.point(end)

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
        transform: PoseRenderTransform,
        highlights: Map<Int, FeedbackSeverity>
) {
    val minConfidence = MIN_DRAW_CONFIDENCE
    val defaultRadius = 10f
    val highlightedRadius = 14f

    keypoints.forEachIndexed { index, keypoint ->
        if (keypoint.score >= minConfidence) {
            val center = transform.point(keypoint)

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
        transform: PoseRenderTransform,
        highlights: Map<Int, FeedbackSeverity>
) {
    val minConfidence = MIN_DRAW_CONFIDENCE

    // Knee angles (for squat)
    drawAngleArc(
            keypoints,
            transform,
            BodyPart.LEFT_HIP,
            BodyPart.LEFT_KNEE,
            BodyPart.LEFT_ANKLE,
            highlights[BodyPart.LEFT_KNEE],
            minConfidence
    )
    drawAngleArc(
            keypoints,
            transform,
            BodyPart.RIGHT_HIP,
            BodyPart.RIGHT_KNEE,
            BodyPart.RIGHT_ANKLE,
            highlights[BodyPart.RIGHT_KNEE],
            minConfidence
    )

    // Elbow angles (for push-up)
    drawAngleArc(
            keypoints,
            transform,
            BodyPart.LEFT_SHOULDER,
            BodyPart.LEFT_ELBOW,
            BodyPart.LEFT_WRIST,
            highlights[BodyPart.LEFT_ELBOW],
            minConfidence
    )
    drawAngleArc(
            keypoints,
            transform,
            BodyPart.RIGHT_SHOULDER,
            BodyPart.RIGHT_ELBOW,
            BodyPart.RIGHT_WRIST,
            highlights[BodyPart.RIGHT_ELBOW],
            minConfidence
    )
}

private fun DrawScope.drawAngleArc(
        keypoints: List<Keypoint>,
        transform: PoseRenderTransform,
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

    val middlePos = transform.point(middle)
    val firstPos = transform.point(first)
    val lastPos = transform.point(last)

    // Calculate angles for arc drawing
    val angle1 = atan2((firstPos.y - middlePos.y).toDouble(), (firstPos.x - middlePos.x).toDouble())
    val angle2 = atan2((lastPos.y - middlePos.y).toDouble(), (lastPos.x - middlePos.x).toDouble())

    // Arc radius proportional to view size (but small)
    val arcRadius = transform.minDimension * 0.04f

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
        transform: PoseRenderTransform
) {
    val minConfidence = MIN_DRAW_CONFIDENCE
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
            transform,
            BodyPart.LEFT_HIP,
            BodyPart.LEFT_KNEE,
            BodyPart.LEFT_ANKLE,
            paint,
            minConfidence
    )
    drawAngleLabel(
            keypoints,
            transform,
            BodyPart.RIGHT_HIP,
            BodyPart.RIGHT_KNEE,
            BodyPart.RIGHT_ANKLE,
            paint,
            minConfidence
    )

    // Draw elbow angle labels
    drawAngleLabel(
            keypoints,
            transform,
            BodyPart.LEFT_SHOULDER,
            BodyPart.LEFT_ELBOW,
            BodyPart.LEFT_WRIST,
            paint,
            minConfidence
    )
    drawAngleLabel(
            keypoints,
            transform,
            BodyPart.RIGHT_SHOULDER,
            BodyPart.RIGHT_ELBOW,
            BodyPart.RIGHT_WRIST,
            paint,
            minConfidence
    )
}

private fun DrawScope.drawAngleLabel(
        keypoints: List<Keypoint>,
        transform: PoseRenderTransform,
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

    val middlePos = transform.point(middle)

    // Offset label slightly from joint
    val labelX = middlePos.x + 20f
    val labelY = middlePos.y - 10f

    drawContext.canvas.nativeCanvas.drawText("${angle.toInt()}°", labelX, labelY, paint)
}

// ─── Debug Overlay ───────────────────────────────────────────────
// Aktifkan dengan debugMode = true di PoseVisualization.
// Menampilkan nama body part dan skor di setiap keypoint — berguna untuk verifikasi
// bahwa mapping COCO index → body part sudah benar untuk kedua model.

private val DEBUG_SHORT_NAMES = listOf(
    "nose", "L.eye", "R.eye", "L.ear", "R.ear",
    "L.sh", "R.sh", "L.el", "R.el",
    "L.wr", "R.wr", "L.hip", "R.hip",
    "L.kn", "R.kn", "L.ank", "R.ank"
)

private fun DrawScope.drawDebugLabels(person: Person, transform: PoseRenderTransform) {
    val paint = android.graphics.Paint().apply {
        textSize = 22f
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
    }

    person.keypoints.forEachIndexed { index, kp ->
        // Tampilkan semua keypoint — termasuk yang score rendah — supaya bisa debug
        val point = transform.point(kp)
        val px = point.x
        val py = point.y
        val name = DEBUG_SHORT_NAMES.getOrElse(index) { "#$index" }
        val scoreStr = "%.2f".format(kp.score)

        // Warna: hijau jika score >= 0.5, kuning jika 0.2–0.5, merah jika < 0.2
        paint.color = when {
            kp.score >= 0.5f -> android.graphics.Color.GREEN
            kp.score >= 0.2f -> android.graphics.Color.YELLOW
            else             -> android.graphics.Color.RED
        }

        // Titik kecil untuk semua keypoint (termasuk yang tidak melewati threshold)
        drawCircle(
            color = if (kp.score >= 0.5f) Color(0xFF00E676)
                    else if (kp.score >= 0.2f) Color(0xFFFFD600)
                    else Color(0xFFFF1744),
            radius = 5f,
            center = Offset(px, py)
        )

        // Label: "[idx] name\nscore"
        drawContext.canvas.nativeCanvas.drawText(
            "[$index]$name", px + 8f, py - 4f, paint
        )
        drawContext.canvas.nativeCanvas.drawText(
            scoreStr, px + 8f, py + 18f, paint
        )
    }
}
