package com.application.skripsiarvan.presentation.camera

import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.skripsiarvan.domain.model.DelegateType
import com.application.skripsiarvan.domain.model.ExerciseType
import com.application.skripsiarvan.domain.model.ModelType
import com.application.skripsiarvan.presentation.components.PoseVisualization
import com.application.skripsiarvan.presentation.viewmodel.DeviceInfo
import com.application.skripsiarvan.presentation.viewmodel.PoseUiState
import com.application.skripsiarvan.presentation.viewmodel.PoseViewModel
import java.util.concurrent.Executors

/** Main camera screen with pose detection and benchmarking controls */
@Composable
fun CameraScreen(viewModel: PoseViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Show toast for export results
    LaunchedEffect(uiState.lastExportPath) {
        uiState.lastExportPath?.let { path ->
            Toast.makeText(context, "Exported to: $path", Toast.LENGTH_LONG).show()
        }
    }

    // Camera Setup with LaunchedEffect to prevent constant rebinding
    LaunchedEffect(
            uiState.isInitialized,
            uiState.selectedModel,
            uiState.selectedDelegate,
            previewView
    ) {
        val currentPreviewView = previewView
        if (uiState.isInitialized && currentPreviewView != null) {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()

            val preview =
                    Preview.Builder().build().also {
                        it.setSurfaceProvider(currentPreviewView.surfaceProvider)
                    }

            val imageAnalysis =
                    ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

            viewModel.getDetector()?.let { detector ->
                imageAnalysis.setAnalyzer(
                        cameraExecutor,
                        PoseImageAnalyzer(
                                poseDetector = detector,
                                resourceProfiler = viewModel.getResourceProfiler()
                        ) {
                                person,
                                inferenceTime,
                                fps,
                                cpuUsage,
                                memoryUsage,
                                powerConsumption,
                                isWarmUpFrame ->
                            viewModel.updateResults(
                                    person,
                                    inferenceTime,
                                    fps,
                                    cpuUsage,
                                    memoryUsage,
                                    powerConsumption,
                                    isWarmUpFrame
                            )
                        }
                )
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "Use case binding failed", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
                factory = { ctx -> PreviewView(ctx).also { previewView = it } },
                modifier = Modifier.fillMaxSize()
        )

        // Pose Visualization Overlay
        PoseVisualization(
                person = uiState.detectedPerson,
                viewWidth = previewView?.width?.toFloat() ?: 1080f,
                viewHeight = previewView?.height?.toFloat() ?: 1920f
        )

        // Warm-up indicator
        if (!uiState.isWarmUpComplete) {
            WarmUpIndicator(
                    framesRemaining = uiState.warmUpFramesRemaining,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
            )
        }

        // Control Panel (Bottom Sheet)
        ControlPanel(
                uiState = uiState,
                onModelSelected = viewModel::selectModel,
                onDelegateSelected = viewModel::selectDelegate,
                onExerciseSelected = viewModel::selectExercise,
                onStartLogging = viewModel::startLogging,
                onStopLogging = viewModel::stopLogging,
                onExportCsv = viewModel::exportToCsv,
                onResetExercise = viewModel::resetExercise,
                modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Loading indicator
        if (!uiState.isInitialized) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Snackbar(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)) {
                Text(text = error)
            }
        }
    }

    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }
}

/** Warm-up phase indicator */
@Composable
fun WarmUpIndicator(framesRemaining: Int, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800))
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
            )
            Text(
                    text = "Warm-up: $framesRemaining frames remaining",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/** Bottom control panel with dropdowns, metrics, and logging controls */
@Composable
fun ControlPanel(
        uiState: PoseUiState,
        onModelSelected: (ModelType) -> Unit,
        onDelegateSelected: (DelegateType) -> Unit,
        onExerciseSelected: (ExerciseType) -> Unit,
        onStartLogging: () -> Unit,
        onStopLogging: () -> Unit,
        onExportCsv: () -> Unit,
        onResetExercise: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f))
    ) {
        var isExpanded by remember { mutableStateOf(false) }

        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(12.dp)
                                .then(
                                        if (isExpanded)
                                                Modifier.verticalScroll(rememberScrollState())
                                        else Modifier
                                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with toggle
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                            text = "AccelPose - Thesis Benchmark",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                    )
                    if (!isExpanded) {
                        Text(
                                text =
                                        "${uiState.selectedModel.displayName} • ${uiState.selectedDelegate.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                            imageVector =
                                    if (isExpanded) Icons.Default.KeyboardArrowDown
                                    else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color.White
                    )
                }
            }

            // Important metrics always visible when collapsed
            if (!isExpanded) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BasicMetricItem(label = "FPS", value = String.format("%.1f", uiState.fps))
                        BasicMetricItem(label = "Lat.", value = "${uiState.inferenceTime}ms")
                    }

                    if (uiState.selectedExercise != ExerciseType.NONE) {
                        Text(
                                text = "${uiState.repetitionCount} Reps",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Model Selection
                    DropdownSelector(
                            label = "Model",
                            items = ModelType.entries.toList(),
                            selectedItem = uiState.selectedModel,
                            onItemSelected = onModelSelected,
                            itemLabel = { it.displayName }
                    )

                    // Delegate Selection
                    DropdownSelector(
                            label = "Hardware Accelerator",
                            items = DelegateType.entries.toList(),
                            selectedItem = uiState.selectedDelegate,
                            onItemSelected = onDelegateSelected,
                            itemLabel = { it.displayName }
                    )

                    // Exercise Selection
                    DropdownSelector(
                            label = "Exercise Detection",
                            items = ExerciseType.entries.toList(),
                            selectedItem = uiState.selectedExercise,
                            onItemSelected = onExerciseSelected,
                            itemLabel = { it.displayName }
                    )

                    HorizontalDivider(color = Color.Gray)

                    // Performance Metrics
                    MetricsDisplay(
                            inferenceTime = uiState.inferenceTime,
                            fps = uiState.fps,
                            cpuUsage = uiState.cpuUsage,
                            memoryUsage = uiState.memoryUsage,
                            powerConsumption = uiState.powerConsumption
                    )

                    // Exercise Counter (if exercise selected)
                    if (uiState.selectedExercise != ExerciseType.NONE) {
                        HorizontalDivider(color = Color.Gray)
                        ExerciseDisplay(
                                exerciseType = uiState.selectedExercise,
                                repetitionCount = uiState.repetitionCount,
                                currentAngle = uiState.currentAngle,
                                onReset = onResetExercise
                        )
                    }

                    HorizontalDivider(color = Color.Gray)

                    // Logging Controls
                    LoggingControls(
                            isLogging = uiState.isLogging,
                            loggedFrameCount = uiState.loggedFrameCount,
                            loggingDuration = uiState.loggingDurationSeconds,
                            onStartLogging = onStartLogging,
                            onStopLogging = onStopLogging,
                            onExportCsv = onExportCsv
                    )

                    HorizontalDivider(color = Color.Gray)

                    // Device Info
                    DeviceInfoDisplay(uiState.deviceInfo)
                }
            }
        }
    }
}

/** Simplified metric for collapsed view */
@Composable
fun BasicMetricItem(label: String, value: String) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

/** Generic dropdown selector */
@Composable
fun <T> DropdownSelector(
        label: String,
        items: List<T>,
        selectedItem: T,
        onItemSelected: (T) -> Unit,
        itemLabel: (T) -> String,
        modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)

        Box {
            Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                        itemLabel(selectedItem),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { item ->
                    DropdownMenuItem(
                            text = { Text(itemLabel(item)) },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            }
                    )
                }
            }
        }
    }
}

/** Performance metrics display with all thesis variables */
@Composable
fun MetricsDisplay(
        inferenceTime: Long,
        fps: Float,
        cpuUsage: Float,
        memoryUsage: Float,
        powerConsumption: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
                text = "Performance Metrics",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricItem(label = "Latency", value = "$inferenceTime ms", color = Color(0xFF00FF00))
            MetricItem(label = "FPS", value = String.format("%.1f", fps), color = Color(0xFF00FF00))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricItem(
                    label = "CPU",
                    value = String.format("%.1f%%", cpuUsage),
                    color = Color(0xFF2196F3)
            )
            MetricItem(
                    label = "Memory",
                    value = String.format("%.1f MB", memoryUsage),
                    color = Color(0xFF2196F3)
            )
            MetricItem(
                    label = "Power",
                    value = String.format("%.0f mW", powerConsumption),
                    color = Color(0xFFFF5722)
            )
        }
    }
}

/** Single metric item display */
@Composable
fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

/** Exercise detection display */
@Composable
fun ExerciseDisplay(
        exerciseType: ExerciseType,
        repetitionCount: Int,
        currentAngle: Double,
        onReset: () -> Unit
) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                    text = "${exerciseType.displayName} Counter",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
            )
            Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "$repetitionCount reps",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4CAF50)
                )
                Text(
                        text = "Angle: ${String.format("%.0f", currentAngle)}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                )
            }
        }

        IconButton(onClick = onReset) {
            Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = Color.White
            )
        }
    }
}

/** Logging controls */
@Composable
fun LoggingControls(
        isLogging: Boolean,
        loggedFrameCount: Int,
        loggingDuration: Long,
        onStartLogging: () -> Unit,
        onStopLogging: () -> Unit,
        onExportCsv: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
                text = "Benchmark Logging",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
        )

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLogging) {
                Button(
                        onClick = onStopLogging,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        modifier = Modifier.weight(1f)
                ) {
                    Icon(
                            Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Stop", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Button(
                        onClick = onStartLogging,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                ) {
                    Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Record", style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(
                    onClick = onExportCsv,
                    enabled = loggedFrameCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Export CSV", style = MaterialTheme.typography.bodySmall)
            }
        }

        if (loggedFrameCount > 0 || isLogging) {
            Text(
                    text = "Frames: $loggedFrameCount | Duration: ${loggingDuration}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLogging) Color(0xFF4CAF50) else Color.LightGray
            )
        }
    }
}

/** Device information display */
@Composable
fun DeviceInfoDisplay(deviceInfo: DeviceInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
                text = "Device: ${deviceInfo.model}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
        )
        Text(
                text = "${deviceInfo.androidVersion} • ${deviceInfo.chipset}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
        )
    }
}
