package com.example.pillcountingnewmodels

import android.annotation.SuppressLint
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pillcountingnewmodels.viewmodel.PillViewModel
import com.example.pillcountingnewmodels.viewmodel.UiState
import kotlinx.coroutines.delay
import java.text.DecimalFormat

/**
 * The main entry point for the screen.
 * It observes the ViewModel's state and displays the appropriate UI.
 */
@Composable
fun HomeScreen(pillViewModel: PillViewModel) {
    val uiState by pillViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading Model...")
                }
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${state.message}\nPlease restart the app.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        is UiState.Ready -> {
            CameraScreen(pillViewModel)
        }
    }
}

/**
 * This composable contains the main application UI that depends on the camera.
 * It is only called when the ViewModel's state is `UiState.Ready`.
 */
@SuppressLint("UnrememberedMutableState")
@Composable
private fun CameraScreen(pillViewModel: PillViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // State from the ViewModel
    val pillCount by pillViewModel.pillCount.collectAsState()
    val isLiveAnalyzing by pillViewModel.isLiveAnalyzing.collectAsState()

    // NEW: State for storing the detected centroids
    var detectedPillCentroids by remember { mutableStateOf(emptyList<PillAnalyzer.Detection>()) }

    // NEW: State for the view dimensions
    var viewDimensions by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val previewView = remember { PreviewView(context) }

    val cameraHelper = remember(lifecycleOwner, viewDimensions) {
        // We only create the cameraHelper after we have the dimensions
        if (viewDimensions == null) return@remember null

        CameraHelper(
            context = context,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = scope,
            previewView = previewView,
            interpreter = pillViewModel.getInterpreter(),
            isLiveAnalyzingFlow = pillViewModel.isLiveAnalyzing,
            onPillCountUpdated = { count, detections ->
                pillViewModel.updatePillCount(count)
                detectedPillCentroids = detections // Store the list of detections
            }
        )
    }

    LaunchedEffect(cameraHelper) {
        cameraHelper?.initializeAndStartCamera()
    }

    val cameraState by (cameraHelper?.cameraState?.collectAsState() ?: mutableStateOf(CameraState.ASLEEP))
    val focusResult by (cameraHelper?.focusResult?.collectAsState() ?: mutableStateOf(null))
    var focusRingPosition by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(focusResult) {
        if (focusResult != null) {
            delay(1000)
            focusRingPosition = null
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // Use onGloballyPositioned to get the view's size
                .onGloballyPositioned { coordinates ->
                    viewDimensions = Pair(coordinates.size.width, coordinates.size.height)
                }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(cameraHelper, cameraState) {
                                detectTapGestures { offset ->
                                    if (cameraState == CameraState.ASLEEP) {
                                        cameraHelper?.wakeUp()
                                    } else {
                                        focusRingPosition = offset
                                        cameraHelper?.handleTapToFocus(
                                            previewView.meteringPointFactory,
                                            offset.x,
                                            offset.y
                                        )
                                    }
                                }
                            }
                    )
                }

                // NEW: Composable for drawing the dots at each pill's centroid
                PillCentroids(detections = detectedPillCentroids)

                SleepOverlay(
                    visible = cameraState == CameraState.ASLEEP,
                    onWakeUp = { cameraHelper?.wakeUp() })
                focusRingPosition?.let { FocusRing(position = it, result = focusResult) }
            }

            InfoPanel(pillCount, getMemoryUsage())

            ActionButtons(
                isLiveAnalyzing = isLiveAnalyzing,
                isTorchOn = false, // TODO: Wire up isTorchOn state
                onCaptureClick = { cameraHelper?.triggerAnalysis() },
                onLiveToggleClick = {
                    cameraHelper?.resetSleepTimer()
                    pillViewModel.toggleLiveAnalyzing()
                },
                onTorchClick = { cameraHelper?.toggleTorch() }
            )
        }
    }
}

/**
 * NEW: Draws a colored dot at the centroid of each detected pill.
 */
@Composable
private fun PillCentroids(detections: List<PillAnalyzer.Detection>) {
    detections.forEach { detection ->
        Box(
            modifier = Modifier
                .offset(x = with(LocalDensity.current) { detection.pixelX.toDp() },
                    y = with(LocalDensity.current) { detection.pixelY.toDp() })
                .size(10.dp)
                .background(Color.Green, CircleShape)
        )
    }
}


@Composable
private fun InfoPanel(pillCount: Int, memoryUsage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Pill Count: $pillCount",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Memory Usage: $memoryUsage",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FocusRing(position: Offset, result: FocusResult?) {
    val density = LocalDensity.current.density
    val (color, size) = when (result) {
        is FocusResult.Success -> Pair(Color.Green, 80.dp)
        is FocusResult.Failure -> Pair(Color.Red, 80.dp)
        else -> Pair(Color.White, 70.dp) // In progress
    }

    Box(
        modifier = Modifier
            .offset(
                x = (position.x / density).dp - size / 2,
                y = (position.y / density).dp - size / 2
            )
            .size(size)
            .border(BorderStroke(2.dp, color), CircleShape)
    )
}

@Composable
fun SleepOverlay(visible: Boolean, onWakeUp: () -> Unit) {
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, label = "alpha")

    if (alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f * alpha))
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Camera Asleep",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Text("Camera is asleep to save battery", color = Color.White)
                Button(onClick = onWakeUp) {
                    Text("Tap Screen or Press to Wake")
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isLiveAnalyzing: Boolean,
    isTorchOn: Boolean,
    onCaptureClick: () -> Unit,
    onLiveToggleClick: () -> Unit,
    onTorchClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Capture Button for one-shot analysis
        Button(
            onClick = onCaptureClick,
            enabled = !isLiveAnalyzing, // Disable when live analysis is on
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Capture")
        }

        // Live Analysis Toggle Button
        Button(
            onClick = onLiveToggleClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isLiveAnalyzing) "Stop Live" else "Start Live")
        }
    }
}


/**
 * Gets the current memory usage of the app's heap.
 */
private fun getMemoryUsage(): String {
    val runtime = Runtime.getRuntime()
    val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
    val df = DecimalFormat("#.##")
    df.isParseIntegerOnly = true
    return "${df.format(usedMemInMB)} MB"
}