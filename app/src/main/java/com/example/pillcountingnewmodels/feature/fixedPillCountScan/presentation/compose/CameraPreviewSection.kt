package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data.DetectedPill
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.logic.CameraHelper
import kotlinx.coroutines.launch

/**
 * Displays a live camera preview with overlays for detected pills.
 *
 * @param pills List of detected pills with centroid coordinates (normalized [0f..1f]).
 * @param onFrame Callback with camera frames (to send to ViewModel for analysis).
 * @param modifier Modifier to style this composable.
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

            pills.forEachIndexed { index, pill ->
                val cx = pill.x * canvasWidth
                val cy = pill.y * canvasHeight

                // Draw pill centroid as circle
                drawCircle(
                    color = Color.Red.copy(alpha = 0.8f),
                    radius = 10.dp.toPx(),
                    center = Offset(cx, cy)
                )

                // Draw pill number above the centroid
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 40f
                        isAntiAlias = true
                        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                    }
                    drawText(
                        "#${index + 1}", // pill number
                        cx,
                        cy - 20.dp.toPx(), // little above the circle
                        textPaint
                    )
                }
            }
        }
    }
}

