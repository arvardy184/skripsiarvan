package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.FormFeedback
import com.application.skripsiarvan.domain.model.Person

/**
 * Interface for analyzing exercise form quality in real-time.
 *
 * Evaluates biomechanical correctness based on:
 * - Depth quality (angle relative to target)
 * - Tempo/speed (phase durations)
 * - Symmetry (left vs right side comparison)
 * - Body alignment (torso lean, knee tracking)
 *
 * Implementations should be stateful to track temporal metrics like phase duration and rep-to-rep
 * consistency.
 */
interface FormAnalyzer {

  /**
   * Analyze the current frame's form quality. Should be called every frame during active
   * exercise.
   *
   * @param person Detected pose keypoints
   * @return FormFeedback with real-time assessment
   */
  fun analyzeForm(person: Person?): FormFeedback

  /**
   * Get the cumulative average form score across all completed reps.
   * @return Average score (0-100) or null if no reps completed
   */
  fun getAverageScore(): Int?

  /** Reset all accumulated form data. */
  fun reset()
}
