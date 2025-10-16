package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.CameraHelper
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * A composable that renders the live camera feed and overlays detected pill markers.
 *
 * This composable uses [CameraHelper] to start the camera preview and stream frames,
 * while also drawing visual overlays for detected pills on top of the camera preview.
 *
 * ## Responsibilities:
 * - Start and manage the camera preview using [PreviewView].
 * - Provide frames via [onFrame] callback for further processing (e.g., ML analysis).
 * - Overlay detected pill centroids as red circles on the live preview.
 *
 * @param pills List of detected pills with normalized coordinates (`0f..1f` range).
 *              Each pill's `(x, y)` is scaled to match the preview's size.
 * @param onFrame Callback triggered for each camera frame (ImageProxy).
 *                The consumer (e.g., ViewModel) should close the ImageProxy after use.
 * @param modifier Optional [Modifier] for styling and layout.
 */
@Composable
fun CameraPreviewSection(
    pills: List<DetectedPill>,
    onFrame: (androidx.camera.core.ImageProxy) -> Unit,
    onFilteredCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val previewView = remember { PreviewView(context) }
    val cameraHelper = remember {
        CameraHelper(
            context = context,
            lifecycleOwner = lifecycleOwner,
            executor = ContextCompat.getMainExecutor(context)
        )
    }

    LaunchedEffect(Unit) {
        cameraHelper.startCamera(previewView)
        coroutineScope.launch {
            cameraHelper.frameFlow.collect { imageProxy ->
                onFrame(imageProxy)
            }
        }
    }

    // 🔹 Box state
    var boxOffset by remember { mutableStateOf(Offset(300f, 600f)) }
    var boxSize by remember {
        mutableStateOf(
            with(density) {
                androidx.compose.ui.geometry.Size(200.dp.toPx(), 200.dp.toPx())
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Move box with drag gesture
                        boxOffset = Offset(
                            (boxOffset.x + pan.x).coerceIn(0f, size.width - boxSize.width),
                            (boxOffset.y + pan.y).coerceIn(0f, size.height - boxSize.height)
                        )

                        // Resize with pinch (zoom gesture)
                        val newWidth = (boxSize.width * zoom).coerceIn(
                            100.dp.toPx(),
                            size.width.toFloat()
                        )
                        val newHeight = (boxSize.height * zoom).coerceIn(
                            100.dp.toPx(),
                            size.height.toFloat()
                        )
                        boxSize = androidx.compose.ui.geometry.Size(newWidth, newHeight)
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Draw bounding box
            drawRect(
                color = Color.Black.copy(alpha = 0.05f),
                topLeft = boxOffset,
                size = boxSize,
                style = Fill
            )

            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = boxOffset,
                size = boxSize,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw resize handle (bottom-right corner)
            val handleSize = 16.dp.toPx()
            drawRect(
                color = Color.Black,
                topLeft = Offset(
                    boxOffset.x + boxSize.width - handleSize,
                    boxOffset.y + boxSize.height - handleSize
                ),
                size = androidx.compose.ui.geometry.Size(handleSize, handleSize)
            )

            // Filter pills inside bounding box
            val pillsInsideBox = pills.filter { pill ->
                val offset = mapToViewCoordinates(
                    pill.x,
                    pill.y,
                    imageWidth = 640,
                    imageHeight = 640,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
                offset.x in boxOffset.x..(boxOffset.x + boxSize.width) && offset.y in boxOffset.y..(boxOffset.y + boxSize.height)
            }

            // Update pill count state
            onFilteredCountChanged(pillsInsideBox.size)

            // Draw pills inside bounding box
            pillsInsideBox.forEach { pill ->
                val offset = mapToViewCoordinates(
                    pill.x,
                    pill.y,
                    imageWidth = 640,
                    imageHeight = 640,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
                drawCircle(
                    color = Color.Red.copy(alpha = 0.9f),
                    radius = 10.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}

/**
 * Convert normalized (0..1) coordinates to actual view coordinates.
 */
private fun mapToViewCoordinates(
    x: Float,
    y: Float,
    imageWidth: Int,
    imageHeight: Int,
    canvasWidth: Float,
    canvasHeight: Float
): Offset {
    val scale = max(
        canvasWidth / imageWidth.toFloat(),
        canvasHeight / imageHeight.toFloat()
    )
    val scaledWidth = imageWidth * scale
    val scaledHeight = imageHeight * scale
    val dx = (canvasWidth - scaledWidth) / 2
    val dy = (canvasHeight - scaledHeight) / 2

    val mappedX = x * scaledWidth + dx
    val mappedY = y * scaledHeight + dy
    return Offset(mappedX, mappedY)
}




