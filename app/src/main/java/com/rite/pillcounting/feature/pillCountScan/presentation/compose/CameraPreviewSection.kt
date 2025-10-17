package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import android.annotation.SuppressLint
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.CameraHelper
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CameraPreviewSection(
    viewModel: PillScanningViewModel,
    pills: List<DetectedPill>,
    isCameraPaused: Boolean,
    onFrame: (androidx.camera.core.ImageProxy) -> Unit,
    onFilteredCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val previewView = remember { PreviewView(context) }
    var lastDelay = 120L

    val cameraHelper = remember {
        CameraHelper(
            context = context,
            lifecycleOwner = lifecycleOwner,
            executor = ContextCompat.getMainExecutor(context)
        )
    }

    // Start camera off main thread
    LaunchedEffect(Unit) {
        cameraHelper.startCamera(previewView)

        coroutineScope.launch(Dispatchers.Default) {
            cameraHelper.frameFlow.collect { image ->
                val start = System.currentTimeMillis()
                onFrame(image)
                val elapsed = System.currentTimeMillis() - start
                lastDelay = (elapsed * 0.5).coerceIn(80.0, 200.0).toLong()
                kotlinx.coroutines.delay(lastDelay)
            }
        }
    }


    LaunchedEffect(isCameraPaused) {
        if (isCameraPaused) {
            cameraHelper.pauseCamera()
        } else {
            cameraHelper.resumeCamera(previewView)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.attachCameraHelper(cameraHelper)
    }

    // ───────────────────────────────────────
    // BoxWithConstraints gives us the container size
    // ───────────────────────────────────────
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = constraints.maxWidth.toFloat() - with(density) { 40.dp.toPx() }
        val containerHeight = constraints.maxHeight.toFloat() - with(density) { 40.dp.toPx() }

        // ───────────────────────────────────────
        // ✅ Ratio-based sizing (responsive)
        // ───────────────────────────────────────
        val baseSide = min(containerWidth, containerHeight)
        val minSizePx = baseSide * 0.50f                   // 15% of smaller side
        val initialBoxSide = baseSide * 0.75f              // 35% of smaller side
        val initialBoxSize = Size(initialBoxSide, initialBoxSide)

        var boxSize by remember { mutableStateOf(initialBoxSize) }
        var boxOffset by remember { mutableStateOf(Offset.Zero) }

        // Filtered pill count
        var filteredCount by remember { mutableStateOf(0) }
        LaunchedEffect(filteredCount) { onFilteredCountChanged(filteredCount) }

        // Derived pills
        val mappedPills by remember(pills) { derivedStateOf { pills.sortedBy { it.x } } }

        // ───────────────────────────────────────
        // Center + ratio-based offset
        // ───────────────────────────────────────
        LaunchedEffect(containerWidth, containerHeight) {
            val horizontalShiftRatio = 0.05f   // 5% to right
            val verticalShiftRatio = 0.05f    // 3% upward

            val centeredX =
                (containerWidth - boxSize.width) / 2 + (containerWidth * horizontalShiftRatio)
            val centeredY =
                (containerHeight - boxSize.height) / 2 + (containerHeight * verticalShiftRatio)
            boxOffset = Offset(centeredX, centeredY)
        }

        // ───────────────────────────────────────
        // Camera preview layer
        // ───────────────────────────────────────
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // ───────────────────────────────────────
        // Gesture Control (move + pinch)
        // ───────────────────────────────────────
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val left = boxOffset.x
                        val top = boxOffset.y
                        val right = left + boxSize.width
                        val bottom = top + boxSize.height

                        val handleCenter = Offset(
                            x = right + with(density) { 15.dp.toPx() },
                            y = bottom - with(density) { 5.dp.toPx() }
                        )
                        val handleRadius = with(density) { 40.dp.toPx() }

                        val isNearHandle =
                            (centroid.x in (handleCenter.x - handleRadius)..(handleCenter.x + handleRadius) &&
                                    centroid.y in (handleCenter.y - handleRadius)..(handleCenter.y + handleRadius))

                        val isInsideBox = centroid.x in left..right && centroid.y in top..bottom

                        val maxX = (containerWidth - boxSize.width).coerceAtLeast(0f)
                        val maxY = (containerHeight - boxSize.height).coerceAtLeast(0f)

                        // Move only if dragging handle
                        if (isNearHandle) {
                            boxOffset = Offset(
                                (boxOffset.x + pan.x).coerceIn(0f, maxX),
                                (boxOffset.y + pan.y).coerceIn(0f, maxY)
                            )
                        }

                        // Resize when pinching inside the box OR near handle
                        if (isInsideBox || isNearHandle) {
                            val safeZoom = zoom.coerceIn(0.8f, 1.2f)
                            val newWidth = (boxSize.width * safeZoom)
                                .coerceAtLeast(minSizePx)
                                .coerceAtMost(containerWidth)
                            val newHeight = (boxSize.height * safeZoom)
                                .coerceAtLeast(minSizePx)
                                .coerceAtMost(containerHeight)

                            // Keep box centered during resize
                            val deltaW = (newWidth - boxSize.width) / 2
                            val deltaH = (newHeight - boxSize.height) / 2
                            val newOffset = Offset(
                                (boxOffset.x - deltaW).coerceIn(0f, containerWidth - newWidth),
                                (boxOffset.y - deltaH).coerceIn(0f, containerHeight - newHeight)
                            )

                            boxOffset = newOffset
                            boxSize = Size(newWidth, newHeight)
                        }
                    }
                }
        )

        // ───────────────────────────────────────
        // Overlay Canvas (black 80% with clear center)
        // ───────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val corner = with(density) { 16.dp.toPx() }
            val edge = with(density) { 26.dp.toPx() }
            val stroke = with(density) { 4.dp.toPx() }
            val dotRadius = with(density) { 3.dp.toPx() }
            val dotSpacingY = with(density) { 12.dp.toPx() }
            val dotSpacingX = with(density) { 14.dp.toPx() }
            val handleOffsetX = with(density) { 12.dp.toPx() }
            val handleOffsetY = with(density) { 5.dp.toPx() }

            val left = boxOffset.x
            val top = boxOffset.y
            val right = left + boxSize.width
            val bottom = top + boxSize.height

            // Dimmed background (80% black)
            drawRect(color = Color.Black.copy(alpha = 0.8f))

            // Transparent "hole" window
            drawRoundRect(
                color = Color.Transparent,
                topLeft = boxOffset,
                size = boxSize,
                cornerRadius = CornerRadius(corner),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            // Corner brackets
            fun drawCorner(x1: Float, y1: Float, dx: Float, dy: Float) {
                drawLine(Color.White, Offset(x1, y1), Offset(x1 + dx, y1), strokeWidth = stroke)
                drawLine(Color.White, Offset(x1, y1), Offset(x1, y1 + dy), strokeWidth = stroke)
            }
            drawCorner(left, top, edge, edge)
            drawCorner(right, top, -edge, edge)
            drawCorner(left, bottom, edge, -edge)
            drawCorner(right, bottom, -edge, -edge)

            // Handle (6 dots)
            val handleX = right + handleOffsetX
            val handleY = bottom - handleOffsetY
            val startY = handleY - (dotSpacingY * 2)
            for (col in 0..1) {
                for (row in 0..2) {
                    val x = handleX + (col * dotSpacingX)
                    val y = startY + (row * dotSpacingY)
                    drawCircle(Color.White, dotRadius, Offset(x, y))
                }
            }

            // Pills inside the box only
            val mapped = mappedPills.map {
                it to mapToViewCoordinates(it.x, it.y, 640, 640, containerWidth, containerHeight)
            }

            val inside = mapped
                .filter { (_, pos) -> pos.x in left..right && pos.y in top..bottom }
                .sortedWith(compareBy<Pair<DetectedPill, Offset>> { it.second.y }.thenBy { it.second.x })

            if (inside.size != filteredCount) filteredCount = inside.size

            inside.forEachIndexed { index, (_, pos) ->
                // Draw outer circle (slightly larger with highlight)
                drawCircle(
                    color = Color.Black.copy(alpha = 0.9f),
                    radius = with(density) { 10.dp.toPx() },
                    center = pos
                )

                // Add a white border stroke
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = with(density) { 13.dp.toPx() },
                    center = pos,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )

                // Draw the bold number with subtle shadow
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 28f
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT_BOLD,
                            android.graphics.Typeface.BOLD
                        )
                        setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
                    }
                    drawText("${index + 1}", pos.x, pos.y + 10f, paint)
                }
            }
        }
    }
}

/** Map normalized (0–1) coordinates to preview space. */
private fun mapToViewCoordinates(
    x: Float,
    y: Float,
    imageWidth: Int = 640,
    imageHeight: Int = 640,
    canvasWidth: Float,
    canvasHeight: Float
): Offset {
    val scale = max(canvasWidth / imageWidth, canvasHeight / imageHeight)
    val scaledW = imageWidth * scale
    val scaledH = imageHeight * scale
    val dx = (canvasWidth - scaledW) / 2
    val dy = (canvasHeight - scaledH) / 2

    return Offset(x * scaledW + dx, y * scaledH + dy)
}
