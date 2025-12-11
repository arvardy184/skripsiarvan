package com.application.skripsiarvan.presentation.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.skripsiarvan.domain.model.DelegateType
import com.application.skripsiarvan.domain.model.ModelType
import com.application.skripsiarvan.presentation.components.PoseVisualization
import com.application.skripsiarvan.presentation.viewmodel.PoseViewModel
import java.util.concurrent.Executors

/**
 * Main camera screen with pose detection
 */
@Composable
fun CameraScreen(
    viewModel: PoseViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView = it }
            },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

                // Image analysis use case
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                // Set analyzer when detector is ready
                if (uiState.isInitialized) {
                    viewModel.getDetector()?.let { detector ->
                        imageAnalysis.setAnalyzer(
                            cameraExecutor,
                            PoseImageAnalyzer(detector) { person, inferenceTime, fps ->
                                viewModel.updateResults(person, inferenceTime, fps)
                            }
                        )
                    }
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
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Pose Visualization Overlay
        PoseVisualization(
            person = uiState.detectedPerson,
            viewWidth = previewView?.width?.toFloat() ?: 1080f,
            viewHeight = previewView?.height?.toFloat() ?: 1920f
        )

        // Control Panel (Bottom Sheet)
        ControlPanel(
            uiState = uiState,
            onModelSelected = viewModel::selectModel,
            onDelegateSelected = viewModel::selectDelegate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Loading indicator
        if (!uiState.isInitialized) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Text(text = error)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

/**
 * Bottom control panel with dropdowns and metrics
 */
@Composable
fun ControlPanel(
    uiState: com.application.skripsiarvan.presentation.viewmodel.PoseUiState,
    onModelSelected: (ModelType) -> Unit,
    onDelegateSelected: (DelegateType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "AccelPose - Performance Benchmark",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

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

            HorizontalDivider(color = Color.Gray)

            // Performance Metrics
            MetricsDisplay(
                inferenceTime = uiState.inferenceTime,
                fps = uiState.fps
            )

            HorizontalDivider(color = Color.Gray)

            // Device Info
            DeviceInfoDisplay(uiState.deviceInfo)
        }
    }
}

/**
 * Generic dropdown selector
 */
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.LightGray
        )

        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray
                )
            ) {
                Text(itemLabel(selectedItem), color = Color.White)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
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

/**
 * Performance metrics display
 */
@Composable
fun MetricsDisplay(
    inferenceTime: Long,
    fps: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Inference Time",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
            Text(
                text = "$inferenceTime ms",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF00FF00)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "FPS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
            Text(
                text = String.format("%.1f", fps),
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF00FF00)
            )
        }
    }
}

/**
 * Device information display
 */
@Composable
fun DeviceInfoDisplay(deviceInfo: com.application.skripsiarvan.presentation.viewmodel.DeviceInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Device: ${deviceInfo.model}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
        Text(
            text = deviceInfo.androidVersion,
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
    }
}
