package com.application.skripsiarvan.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.application.skripsiarvan.domain.model.BodyPart
import com.application.skripsiarvan.domain.model.Keypoint
import com.application.skripsiarvan.domain.model.Person

/**
 * Composable that draws pose skeleton over camera preview
 */
@Composable
fun PoseVisualization(
    person: Person?,
    viewWidth: Float,
    viewHeight: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        person?.let {
            drawPoseSkeleton(it, viewWidth, viewHeight)
        }
    }
}

/**
 * Draw the complete pose skeleton (keypoints and connections)
 */
private fun DrawScope.drawPoseSkeleton(
    person: Person,
    viewWidth: Float,
    viewHeight: Float
) {
    val keypoints = person.keypoints

    // Draw connections (bones) between keypoints
    drawConnections(keypoints, viewWidth, viewHeight)

    // Draw keypoints (joints)
    drawKeypoints(keypoints, viewWidth, viewHeight)
}

/**
 * Draw lines connecting body parts
 */
private fun DrawScope.drawConnections(
    keypoints: List<Keypoint>,
    viewWidth: Float,
    viewHeight: Float
) {
    val connections = listOf(
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

    val lineColor = Color(0xFF00FF00) // Green
    val minConfidence = 0.3f

    connections.forEach { (startIdx, endIdx) ->
        val start = keypoints.getOrNull(startIdx)
        val end = keypoints.getOrNull(endIdx)

        if (start != null && end != null &&
            start.score >= minConfidence && end.score >= minConfidence
        ) {
            drawLine(
                color = lineColor,
                start = Offset(start.x * viewWidth, start.y * viewHeight),
                end = Offset(end.x * viewWidth, end.y * viewHeight),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draw circles for each keypoint
 */
private fun DrawScope.drawKeypoints(
    keypoints: List<Keypoint>,
    viewWidth: Float,
    viewHeight: Float
) {
    val pointColor = Color(0xFFFF0000) // Red
    val minConfidence = 0.3f
    val radius = 12f

    keypoints.forEach { keypoint ->
        if (keypoint.score >= minConfidence) {
            drawCircle(
                color = pointColor,
                radius = radius,
                center = Offset(
                    x = keypoint.x * viewWidth,
                    y = keypoint.y * viewHeight
                )
            )
        }
    }
}
