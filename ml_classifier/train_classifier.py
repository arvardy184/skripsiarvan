"""
Exercise Phase Classifier — Training Pipeline

This script trains a lightweight MLP classifier to recognize exercise phases
(STANDING, GOING_DOWN, AT_BOTTOM, GOING_UP) from normalized pose keypoints.

The model takes 34 features (17 keypoints × [x, y]) and outputs
probabilities for each phase. Designed
to run on-device via TFLite.

Usage:
    python train_classifier.py                          # Train with synthetic data
    python train_classifier.py --data collected_data.csv # Train with real data

Output:
    exercise_classifier.tflite  — TFLite model for Android deployment
    training_report.png          — Training curves visualization

Author: AccelPose ML Pipeline
"""

import argparse
import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from pathlib import Path

import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers, regularizers, callbacks
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix
import json


# ─── Constants ──────────────────────────────────────────────────────

# COCO 17 keypoint names (order must match Android app)
KEYPOINT_NAMES = [
    "nose", "left_eye", "right_eye", "left_ear", "right_ear",
    "left_shoulder", "right_shoulder", "left_elbow", "right_elbow",
    "left_wrist", "right_wrist", "left_hip", "right_hip",
    "left_knee", "right_knee", "left_ankle", "right_ankle"
]

# Exercise phases to classify
PHASE_LABELS = ["STANDING", "GOING_DOWN", "AT_BOTTOM", "GOING_UP", "UNKNOWN"]

NUM_KEYPOINTS = 17
NUM_FEATURES = NUM_KEYPOINTS * 2  # x, y for each keypoint = 34 features
NUM_CLASSES = len(PHASE_LABELS)

# Keypoint indices (matching BodyPart in Android)
NOSE = 0
LEFT_SHOULDER = 5
RIGHT_SHOULDER = 6
LEFT_ELBOW = 7
RIGHT_ELBOW = 8
LEFT_WRIST = 9
RIGHT_WRIST = 10
LEFT_HIP = 11
RIGHT_HIP = 12
LEFT_KNEE = 13
RIGHT_KNEE = 14
LEFT_ANKLE = 15
RIGHT_ANKLE = 16


# ─── Synthetic Data Generator ──────────────────────────────────────

def generate_standing_pose(noise=0.02):
    """Generate a standing pose with person upright."""
    kps = np.zeros((NUM_KEYPOINTS, 2))

    # Head
    kps[NOSE]           = [0.50, 0.10]
    kps[1]              = [0.48, 0.08]  # left_eye
    kps[2]              = [0.52, 0.08]  # right_eye
    kps[3]              = [0.45, 0.10]  # left_ear
    kps[4]              = [0.55, 0.10]  # right_ear

    # Shoulders
    kps[LEFT_SHOULDER]  = [0.40, 0.25]
    kps[RIGHT_SHOULDER] = [0.60, 0.25]

    # Elbows (arms at sides)
    kps[LEFT_ELBOW]     = [0.38, 0.40]
    kps[RIGHT_ELBOW]    = [0.62, 0.40]

    # Wrists
    kps[LEFT_WRIST]     = [0.37, 0.52]
    kps[RIGHT_WRIST]    = [0.63, 0.52]

    # Hips
    kps[LEFT_HIP]       = [0.43, 0.50]
    kps[RIGHT_HIP]      = [0.57, 0.50]

    # Knees (straight)
    kps[LEFT_KNEE]      = [0.43, 0.70]
    kps[RIGHT_KNEE]     = [0.57, 0.70]

    # Ankles
    kps[LEFT_ANKLE]     = [0.43, 0.90]
    kps[RIGHT_ANKLE]    = [0.57, 0.90]

    # Add noise
    kps += np.random.normal(0, noise, kps.shape)
    return kps.clip(0, 1)


def generate_going_down_pose(depth_factor=0.5, noise=0.02):
    """Generate a mid-descent squat pose. depth_factor: 0=standing, 1=full squat."""
    kps = generate_standing_pose(noise=0)

    # Bend knees and lower hips
    hip_drop = 0.10 * depth_factor
    knee_bend = 0.08 * depth_factor  # Knees come forward slightly

    # Lower everything from hips up slightly
    head_drop = hip_drop * 0.3
    kps[NOSE][1] += head_drop
    kps[1][1] += head_drop
    kps[2][1] += head_drop
    kps[3][1] += head_drop
    kps[4][1] += head_drop

    kps[LEFT_SHOULDER][1] += hip_drop * 0.5
    kps[RIGHT_SHOULDER][1] += hip_drop * 0.5

    # Hips drop
    kps[LEFT_HIP][1] += hip_drop
    kps[RIGHT_HIP][1] += hip_drop

    # Knees bend forward and drop slightly
    kps[LEFT_KNEE][1] += hip_drop * 0.3
    kps[RIGHT_KNEE][1] += hip_drop * 0.3
    kps[LEFT_KNEE][0] -= knee_bend * 0.3  # Knees go slightly outward/forward
    kps[RIGHT_KNEE][0] += knee_bend * 0.3

    # Torso lean forward slightly
    lean = 0.02 * depth_factor
    kps[LEFT_SHOULDER][1] += lean
    kps[RIGHT_SHOULDER][1] += lean

    kps += np.random.normal(0, noise, kps.shape)
    return kps.clip(0, 1)


def generate_at_bottom_pose(noise=0.02):
    """Generate a full squat (bottom) pose."""
    kps = generate_standing_pose(noise=0)

    hip_drop = 0.20
    knee_bend = 0.12

    # Significant lowering
    head_drop = hip_drop * 0.4
    for i in range(5):
        kps[i][1] += head_drop

    kps[LEFT_SHOULDER][1] += hip_drop * 0.6
    kps[RIGHT_SHOULDER][1] += hip_drop * 0.6

    # Hips drop significantly
    kps[LEFT_HIP][1] += hip_drop
    kps[RIGHT_HIP][1] += hip_drop

    # Knees bend — hips at or below knee level
    kps[LEFT_KNEE][1] += hip_drop * 0.3
    kps[RIGHT_KNEE][1] += hip_drop * 0.3
    kps[LEFT_KNEE][0] -= knee_bend * 0.4
    kps[RIGHT_KNEE][0] += knee_bend * 0.4

    # Forward lean
    lean = 0.04
    kps[LEFT_SHOULDER][1] += lean
    kps[RIGHT_SHOULDER][1] += lean

    # Arms may reach forward for balance
    kps[LEFT_ELBOW][1] += hip_drop * 0.3
    kps[RIGHT_ELBOW][1] += hip_drop * 0.3
    kps[LEFT_WRIST][1] += hip_drop * 0.2
    kps[RIGHT_WRIST][1] += hip_drop * 0.2

    kps += np.random.normal(0, noise, kps.shape)
    return kps.clip(0, 1)


def generate_going_up_pose(ascent_factor=0.5, noise=0.02):
    """Generate a mid-ascent squat pose. Similar to going_down but can differ in torso angle."""
    # Reuse going_down with slight variations
    kps = generate_going_down_pose(depth_factor=1 - ascent_factor, noise=0)

    # Slight differences: more upright torso when pushing up
    uprightness = 0.01 * ascent_factor
    kps[LEFT_SHOULDER][1] -= uprightness
    kps[RIGHT_SHOULDER][1] -= uprightness
    kps[NOSE][1] -= uprightness * 0.5

    kps += np.random.normal(0, noise, kps.shape)
    return kps.clip(0, 1)


def generate_unknown_pose(noise=0.04):
    """Generate random non-exercise pose (sitting, lying, partial visibility)."""
    kps = np.random.uniform(0.1, 0.9, (NUM_KEYPOINTS, 2))
    kps += np.random.normal(0, noise, kps.shape)
    return kps.clip(0, 1)


def generate_synthetic_dataset(samples_per_class=2000, noise_range=(0.01, 0.04)):
    """Generate a balanced synthetic dataset for all exercise phases."""
    X = []
    y = []

    print(f"Generating synthetic data: {samples_per_class} samples per class...")

    for _ in range(samples_per_class):
        noise = np.random.uniform(*noise_range)

        # STANDING
        kps = generate_standing_pose(noise)
        X.append(kps.flatten())
        y.append("STANDING")

        # GOING_DOWN (various depths)
        depth = np.random.uniform(0.2, 0.8)
        kps = generate_going_down_pose(depth, noise)
        X.append(kps.flatten())
        y.append("GOING_DOWN")

        # AT_BOTTOM
        kps = generate_at_bottom_pose(noise)
        X.append(kps.flatten())
        y.append("AT_BOTTOM")

        # GOING_UP (various heights)
        ascent = np.random.uniform(0.2, 0.8)
        kps = generate_going_up_pose(ascent, noise)
        X.append(kps.flatten())
        y.append("GOING_UP")

        # UNKNOWN
        kps = generate_unknown_pose(noise * 2)
        X.append(kps.flatten())
        y.append("UNKNOWN")

    X = np.array(X, dtype=np.float32)
    y = np.array(y)

    print(f"Generated {len(X)} total samples")
    print(f"Feature shape: {X.shape}")
    print(f"Class distribution: {dict(zip(*np.unique(y, return_counts=True)))}")

    return X, y


# ─── Real Data Loader ──────────────────────────────────────────────

def load_real_data(csv_path):
    """
    Load real data collected from the Android app.
    Expected CSV format:
        label, kp0_x, kp0_y, kp1_x, kp1_y, ..., kp16_x, kp16_y
    """
    print(f"Loading real data from: {csv_path}")
    df = pd.read_csv(csv_path)

    # Expect 'label' column + 34 keypoint columns
    if 'label' not in df.columns:
        raise ValueError("CSV must have a 'label' column")

    y = df['label'].values
    feature_cols = [c for c in df.columns if c != 'label']
    X = df[feature_cols].values.astype(np.float32)

    if X.shape[1] != NUM_FEATURES:
        raise ValueError(f"Expected {NUM_FEATURES} features, got {X.shape[1]}")

    print(f"Loaded {len(X)} samples")
    print(f"Class distribution: {dict(zip(*np.unique(y, return_counts=True)))}")

    return X, y


# ─── Model Definition ──────────────────────────────────────────────

def build_model(input_dim=NUM_FEATURES, num_classes=NUM_CLASSES):
    """
    Build a lightweight MLP classifier.

    Architecture:
        Input (34) → Dense(64, ReLU) → Dropout(0.3) →
        Dense(32, ReLU) → Dropout(0.2) →
        Dense(5, Softmax)

    Total params: ~4K — tiny model suitable for mobile inference.
    """
    model = keras.Sequential([
        layers.Input(shape=(input_dim,), name="keypoint_input"),

        layers.Dense(64, activation='relu',
                     kernel_regularizer=regularizers.l2(1e-4),
                     name="hidden_1"),
        layers.BatchNormalization(name="bn_1"),
        layers.Dropout(0.3, name="dropout_1"),

        layers.Dense(32, activation='relu',
                     kernel_regularizer=regularizers.l2(1e-4),
                     name="hidden_2"),
        layers.BatchNormalization(name="bn_2"),
        layers.Dropout(0.2, name="dropout_2"),

        layers.Dense(num_classes, activation='softmax', name="output")
    ], name="exercise_phase_classifier")

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=1e-3),
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )

    return model


# ─── Training ──────────────────────────────────────────────────────

def train_model(X, y, epochs=50, batch_size=32, validation_split=0.2):
    """Train the model and return training history."""

    # Encode labels
    le = LabelEncoder()
    le.fit(PHASE_LABELS)
    y_encoded = le.transform(y)

    # Split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y_encoded, test_size=validation_split, random_state=42, stratify=y_encoded
    )

    print(f"\nTraining set: {len(X_train)} samples")
    print(f"Test set: {len(X_test)} samples")

    # Build model
    model = build_model()
    model.summary()

    # Callbacks
    cb_list = [
        callbacks.EarlyStopping(
            monitor='val_loss',
            patience=10,
            restore_best_weights=True,
            verbose=1
        ),
        callbacks.ReduceLROnPlateau(
            monitor='val_loss',
            factor=0.5,
            patience=5,
            min_lr=1e-6,
            verbose=1
        )
    ]

    # Train
    history = model.fit(
        X_train, y_train,
        validation_data=(X_test, y_test),
        epochs=epochs,
        batch_size=batch_size,
        callbacks=cb_list,
        verbose=1
    )

    # Evaluate
    print("\n=== Final Evaluation ===")
    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)
    print(f"Test Loss: {test_loss:.4f}")
    print(f"Test Accuracy: {test_acc:.4f}")

    # Classification report
    y_pred = model.predict(X_test, verbose=0)
    y_pred_classes = np.argmax(y_pred, axis=1)
    print("\nClassification Report:")
    print(classification_report(y_test, y_pred_classes, target_names=le.classes_))

    return model, le, history


# ─── Export to TFLite ──────────────────────────────────────────────

def export_to_tflite(model, output_path="exercise_classifier.tflite"):
    """Convert Keras model to TFLite with float16 quantization."""
    print(f"\nExporting to TFLite: {output_path}")

    converter = tf.lite.TFLiteConverter.from_keras_model(model)

    # Float16 quantization (good balance of size and accuracy)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]

    tflite_model = converter.convert()

    with open(output_path, 'wb') as f:
        f.write(tflite_model)

    size_kb = os.path.getsize(output_path) / 1024
    print(f"Model saved: {output_path} ({size_kb:.1f} KB)")

    # Verify
    verify_tflite_model(output_path)

    return output_path


def verify_tflite_model(model_path):
    """Verify the TFLite model loads and runs correctly."""
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print(f"\nTFLite Model Verification:")
    print(f"  Input shape:  {input_details[0]['shape']} dtype: {input_details[0]['dtype']}")
    print(f"  Output shape: {output_details[0]['shape']} dtype: {output_details[0]['dtype']}")

    # Test with dummy input
    test_input = np.random.rand(1, NUM_FEATURES).astype(np.float32)
    interpreter.set_tensor(input_details[0]['index'], test_input)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details[0]['index'])
    print(f"  Test output:  {output}")
    print(f"  Sum of probs: {output.sum():.4f} (should be ~1.0)")
    print("  ✅ Model verification passed!")


# ─── Visualization ─────────────────────────────────────────────────

def plot_training_history(history, save_path="training_report.png"):
    """Plot training and validation curves."""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

    # Accuracy
    ax1.plot(history.history['accuracy'], label='Train Accuracy', linewidth=2)
    ax1.plot(history.history['val_accuracy'], label='Val Accuracy', linewidth=2)
    ax1.set_title('Model Accuracy', fontsize=14, fontweight='bold')
    ax1.set_xlabel('Epoch')
    ax1.set_ylabel('Accuracy')
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    # Loss
    ax2.plot(history.history['loss'], label='Train Loss', linewidth=2)
    ax2.plot(history.history['val_loss'], label='Val Loss', linewidth=2)
    ax2.set_title('Model Loss', fontsize=14, fontweight='bold')
    ax2.set_xlabel('Epoch')
    ax2.set_ylabel('Loss')
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.savefig(save_path, dpi=150, bbox_inches='tight')
    print(f"\nTraining report saved: {save_path}")
    plt.close()


def export_label_mapping(le, output_path="label_mapping.json"):
    """Export label mapping for Android app to use."""
    mapping = {int(i): label for i, label in enumerate(le.classes_)}
    with open(output_path, 'w') as f:
        json.dump(mapping, f, indent=2)
    print(f"Label mapping saved: {output_path}")
    return mapping


# ─── Main ──────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Train Exercise Phase Classifier")
    parser.add_argument("--data", type=str, default=None,
                        help="Path to CSV with real collected data")
    parser.add_argument("--epochs", type=int, default=50,
                        help="Number of training epochs")
    parser.add_argument("--samples", type=int, default=2000,
                        help="Samples per class for synthetic data")
    parser.add_argument("--output", type=str, default="exercise_classifier.tflite",
                        help="Output TFLite model path")
    args = parser.parse_args()

    print("=" * 60)
    print("  Exercise Phase Classifier — Training Pipeline")
    print("=" * 60)

    # Load or generate data
    if args.data:
        X, y = load_real_data(args.data)
    else:
        print("\nNo real data provided. Using synthetic data for training.")
        print("(For best results, collect real data from the Android app)\n")
        X, y = generate_synthetic_dataset(samples_per_class=args.samples)

    # Train
    model, le, history = train_model(X, y, epochs=args.epochs)

    # Export
    output_dir = Path(args.output).parent
    output_dir.mkdir(parents=True, exist_ok=True)

    tflite_path = export_to_tflite(model, args.output)
    plot_training_history(history, str(output_dir / "training_report.png"))
    label_mapping = export_label_mapping(le, str(output_dir / "label_mapping.json"))

    print("\n" + "=" * 60)
    print("  ✅ Training Complete!")
    print("=" * 60)
    print(f"\n  Model:   {tflite_path}")
    print(f"  Labels:  {label_mapping}")
    print(f"\n  Next steps:")
    print(f"  1. Copy {tflite_path} to app/src/main/assets/")
    print(f"  2. The MLExerciseClassifier in Android will load it automatically")
    print(f"  3. Select 'ML Classifier' mode in the app to use it")
    print()


if __name__ == "__main__":
    main()
