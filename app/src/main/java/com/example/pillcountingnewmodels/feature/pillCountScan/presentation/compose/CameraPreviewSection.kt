package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.DetectedPill
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic.CameraHelper
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

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

    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay pills
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 🔹 Helper function inside Canvas scope
            fun mapToViewCoordinates(
                x: Float,
                y: Float,
                imageWidth: Int,
                imageHeight: Int
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

            pills.forEach { pill ->
                val offset = mapToViewCoordinates(
                    pill.x,
                    pill.y,
                    imageWidth = 640,
                    imageHeight = 640
                )

                drawCircle(
                    color = Color.Red.copy(alpha = 0.8f),
                    radius = 10.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}


