package com.application.skripsiarvan.domain.model

/**
 * Represents a single body keypoint/joint detected by pose estimation
 * @param x X coordinate (normalized 0.0-1.0)
 * @param y Y coordinate (normalized 0.0-1.0)
 * @param score Confidence score (0.0-1.0)
 * @param label Body part label (e.g., "nose", "left_shoulder")
 */
data class Keypoint(
    val x: Float,
    val y: Float,
    val score: Float,
    val label: String = ""
)
