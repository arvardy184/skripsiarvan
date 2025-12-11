package com.application.skripsiarvan.domain.model

/**
 * Represents a detected person with all body keypoints
 * @param keypoints List of 17 body keypoints (COCO format)
 * @param score Overall detection confidence
 */
data class Person(
    val keypoints: List<Keypoint>,
    val score: Float
)

/**
 * Body keypoint indices for COCO format (17 keypoints)
 * Used by both MoveNet and MediaPipe BlazePose
 */
object BodyPart {
    const val NOSE = 0
    const val LEFT_EYE = 1
    const val RIGHT_EYE = 2
    const val LEFT_EAR = 3
    const val RIGHT_EAR = 4
    const val LEFT_SHOULDER = 5
    const val RIGHT_SHOULDER = 6
    const val LEFT_ELBOW = 7
    const val RIGHT_ELBOW = 8
    const val LEFT_WRIST = 9
    const val RIGHT_WRIST = 10
    const val LEFT_HIP = 11
    const val RIGHT_HIP = 12
    const val LEFT_KNEE = 13
    const val RIGHT_KNEE = 14
    const val LEFT_ANKLE = 15
    const val RIGHT_ANKLE = 16

    val labels = listOf(
        "nose", "left_eye", "right_eye", "left_ear", "right_ear",
        "left_shoulder", "right_shoulder", "left_elbow", "right_elbow",
        "left_wrist", "right_wrist", "left_hip", "right_hip",
        "left_knee", "right_knee", "left_ankle", "right_ankle"
    )
}
