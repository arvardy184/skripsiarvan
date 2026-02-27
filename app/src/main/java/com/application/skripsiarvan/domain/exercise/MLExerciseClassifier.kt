package com.application.skripsiarvan.domain.exercise

import android.content.Context
import android.util.Log
import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.Person
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter

/**
 * ML-based Exercise Phase Classifier using a custom TFLite model.
 *
 * Instead of rule-based state machines with angle thresholds, this classifier uses a trained MLP
 * model to directly predict exercise phases from raw keypoints.
 *
 * Model Architecture:
 * - Input: 34 features (17 keypoints ├ù [x, y], normalized 0-1)
 * - Output: 5 classes [STANDING, GOING_DOWN, AT_BOTTOM, GOING_UP, UNKNOWN]
 * - Size: ~10KB (float16 quantized)
 *
 * Advantages over rule-based:
 * 1. Learns complex pose patterns beyond simple angle thresholds
 * 2. More robust to camera angle variations
 * 3. Can generalize to different body types
 * 4. Easy to retrain with more data
 *
 * Rep counting logic: Uses phase transitions: STANDING ΓåÆ GOING_DOWN ΓåÆ AT_BOTTOM ΓåÆ GOING_UP ΓåÆ
 * STANDING = 1 rep
 */
class MLExerciseClassifier(context: Context) : ExerciseDetector {

    companion object {
        private const val TAG = "MLExerciseClassifier"
        private const val MODEL_FILE = "exercise_classifier.tflite"
        private const val NUM_KEYPOINTS = 17
        private const val NUM_FEATURES = NUM_KEYPOINTS * 2 // x, y per keypoint
        private const val NUM_CLASSES = 5
        private const val CONFIDENCE_THRESHOLD = 0.5f

        // Phase indices (must match training label order: LabelEncoder sorts alphabetically)
        // Sorted: AT_BOTTOM=0, GOING_DOWN=1, GOING_UP=2, STANDING=3, UNKNOWN=4
        private const val PHASE_AT_BOTTOM = 0
        private const val PHASE_GOING_DOWN = 1
        private const val PHASE_GOING_UP = 2
        private const val PHASE_STANDING = 3
        private const val PHASE_UNKNOWN = 4

        private val PHASE_NAMES =
                arrayOf("AT_BOTTOM", "GOING_DOWN", "GOING_UP", "STANDING", "UNKNOWN")
    }

    private var interpreter: Interpreter? = null
    private var isModelLoaded = false

    // State tracking for rep counting
    private var currentPhase = PHASE_UNKNOWN
    private var previousPhase = PHASE_UNKNOWN
    private var repetitionCount = 0
    private var lastAngle: Double? = null

    // Phase transition tracking for valid rep counting
    private var hasBeenStanding = false
    private var hasGoneDown = false
    private var hasBeenAtBottom = false
    private var hasGoneUp = false

    // Smoothing: use majority voting over last N predictions
    private val predictionBuffer = ArrayDeque<Int>(7)
    private val SMOOTHING_WINDOW = 7

    init {
        loadModel(context)
    }

    /** Load the TFLite model from assets. */
    private fun loadModel(context: Context) {
        try {
            val modelBuffer = loadModelFile(context, MODEL_FILE)
            val options = Interpreter.Options()
            options.setNumThreads(2)
            interpreter = Interpreter(modelBuffer, options)
            isModelLoaded = true

            // Log model info
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            Log.d(TAG, "Γ£à Model loaded: input=$inputShape, output=$outputShape")
        } catch (e: Exception) {
            Log.e(TAG, "Γ¥î Failed to load model: ${e.message}")
            isModelLoaded = false
        }
    }

    private fun loadModelFile(context: Context, fileName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /** Classify the current frame's exercise phase using ML inference. */
    override fun analyzeFrame(person: Person?): ExerciseState {
        if (!isModelLoaded || person == null) return ExerciseState.IDLE

        // Extract features from keypoints
        val features = extractFeatures(person) ?: return ExerciseState.IDLE

        // Run inference
        val phaseProbs = runInference(features) ?: return ExerciseState.IDLE

        // Get predicted phase
        val rawPrediction = phaseProbs.indices.maxByOrNull { phaseProbs[it] } ?: PHASE_UNKNOWN
        val confidence = phaseProbs[rawPrediction]

        // Apply confidence threshold
        val prediction = if (confidence >= CONFIDENCE_THRESHOLD) rawPrediction else PHASE_UNKNOWN

        // Apply smoothing (majority voting)
        val smoothedPhase = applySmoothingVote(prediction)

        // Also compute knee angle for display (reuse existing calculator)
        lastAngle = AngleCalculator.getAverageKneeAngle(person)

        // Track phase transitions and count reps
        return processPhaseTransition(smoothedPhase, confidence)
    }

    /**
     * Extract normalized features from pose keypoints. Returns a FloatArray of [x0, y0, x1, y1,
     * ..., x16, y16] = 34 values.
     */
    private fun extractFeatures(person: Person): FloatArray? {
        if (person.keypoints.size < NUM_KEYPOINTS) return null

        // Check minimum confidence
        val avgConfidence = person.keypoints.take(NUM_KEYPOINTS).map { it.score }.average()
        if (avgConfidence < 0.3f) return null

        val features = FloatArray(NUM_FEATURES)
        for (i in 0 until NUM_KEYPOINTS) {
            val kp = person.keypoints[i]
            features[i * 2] = kp.x // Already normalized [0, 1]
            features[i * 2 + 1] = kp.y // Already normalized [0, 1]
        }

        return features
    }

    /**
     * Run TFLite inference on extracted features. Returns probability distribution over NUM_CLASSES
     * phases.
     */
    private fun runInference(features: FloatArray): FloatArray? {
        return try {
            // Prepare input buffer [1, 34]
            val inputBuffer = ByteBuffer.allocateDirect(NUM_FEATURES * 4)
            inputBuffer.order(ByteOrder.nativeOrder())
            for (f in features) {
                inputBuffer.putFloat(f)
            }
            inputBuffer.rewind()

            // Prepare output buffer [1, 5]
            val outputBuffer = Array(1) { FloatArray(NUM_CLASSES) }

            // Run inference
            interpreter?.run(inputBuffer, outputBuffer)

            outputBuffer[0]
        } catch (e: Exception) {
            Log.e(TAG, "Inference error: ${e.message}")
            null
        }
    }

    /** Apply majority voting over a sliding window to smooth predictions. */
    private fun applySmoothingVote(prediction: Int): Int {
        if (predictionBuffer.size >= SMOOTHING_WINDOW) {
            predictionBuffer.removeFirst()
        }
        predictionBuffer.addLast(prediction)

        // Majority vote
        val counts = IntArray(NUM_CLASSES)
        predictionBuffer.forEach { counts[it]++ }
        return counts.indices.maxByOrNull { counts[it] } ?: prediction
    }

    /**
     * Process phase transitions to count valid repetitions.
     *
     * Valid rep sequence: STANDING ΓåÆ GOING_DOWN ΓåÆ AT_BOTTOM ΓåÆ GOING_UP ΓåÆ STANDING All phases must
     * be visited in order for a rep to count.
     */
    private fun processPhaseTransition(phase: Int, confidence: Float): ExerciseState {
        if (phase == currentPhase) {
            // Same phase, return current state
            return phaseToExerciseState(phase)
        }

        previousPhase = currentPhase
        currentPhase = phase

        // Track phase visits for rep validation
        when (phase) {
            PHASE_STANDING -> {
                if (hasGoneUp && hasBeenAtBottom && hasGoneDown && hasBeenStanding) {
                    // Complete valid rep!
                    repetitionCount++
                    Log.d(
                            TAG,
                            "Γ£à ML REP #$repetitionCount (conf: ${String.format("%.2f", confidence)})"
                    )
                    // Reset tracking
                    resetPhaseTracking()
                    hasBeenStanding = true // We're standing for the next rep
                    return ExerciseState.COMPLETED
                } else {
                    hasBeenStanding = true
                    // Reset incomplete attempts
                    hasGoneDown = false
                    hasBeenAtBottom = false
                    hasGoneUp = false
                }
            }
            PHASE_GOING_DOWN -> {
                if (hasBeenStanding) hasGoneDown = true
            }
            PHASE_AT_BOTTOM -> {
                if (hasGoneDown) hasBeenAtBottom = true
            }
            PHASE_GOING_UP -> {
                if (hasBeenAtBottom) hasGoneUp = true
            }
        }

        Log.v(
                TAG,
                "Phase: ${PHASE_NAMES[phase]} (conf: ${String.format("%.2f", confidence)})" +
                        " | S:$hasBeenStanding D:$hasGoneDown B:$hasBeenAtBottom U:$hasGoneUp"
        )

        return phaseToExerciseState(phase)
    }

    private fun phaseToExerciseState(phase: Int): ExerciseState {
        return when (phase) {
            PHASE_STANDING -> ExerciseState.IDLE
            PHASE_GOING_DOWN -> ExerciseState.STARTING
            PHASE_AT_BOTTOM -> ExerciseState.IN_MOTION
            PHASE_GOING_UP -> ExerciseState.IN_MOTION
            else -> ExerciseState.IDLE
        }
    }

    private fun resetPhaseTracking() {
        hasBeenStanding = false
        hasGoneDown = false
        hasBeenAtBottom = false
        hasGoneUp = false
    }

    override fun getRepetitionCount(): Int = repetitionCount

    override fun getCurrentAngle(): Double? = lastAngle

    override fun reset() {
        repetitionCount = 0
        currentPhase = PHASE_UNKNOWN
        previousPhase = PHASE_UNKNOWN
        lastAngle = null
        resetPhaseTracking()
        predictionBuffer.clear()
    }

    /** Check if the ML model was loaded successfully. */
    fun isReady(): Boolean = isModelLoaded

    /** Release resources. */
    fun close() {
        interpreter?.close()
        interpreter = null
        isModelLoaded = false
    }
}
