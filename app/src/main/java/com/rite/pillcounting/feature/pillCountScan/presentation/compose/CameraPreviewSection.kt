package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import androidx.camera.core.ImageProxy
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CameraPreviewSection(
    viewModel: PillScanningViewModel,
    pills: List<DetectedPill>,
    isCameraPaused: Boolean,
    onFrame: (ImageProxy) -> Unit,
    onFilteredCountChanged: (Int) -> Unit,
    onPreviewReady: (PreviewView) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val previewView = remember { PreviewView(context) }
    var lastDelay: Long

    // 🔹 Setup CameraHelper
    val cameraHelper = remember {
        CameraHelper(
            context = context,
            lifecycleOwner = lifecycleOwner,
            executor = ContextCompat.getMainExecutor(context)
        )
    }

    // 🔹 Notify ViewModel
    LaunchedEffect(Unit) {
        viewModel.attachCameraHelper(cameraHelper)
    }

    // 🔹 Start camera and collect frames
    LaunchedEffect(Unit) {
        cameraHelper.startCamera(previewView)

        coroutineScope.launch(Dispatchers.Default) {
            cameraHelper.frameFlow.collect { image ->
                val start = System.currentTimeMillis()
                onFrame(image)
                val elapsed = System.currentTimeMillis() - start
                lastDelay = (elapsed * 0.5).coerceIn(80.0, 200.0).toLong()
                delay(lastDelay)
            }
        }
    }

    // 🔹 Pause / Resume handling
    LaunchedEffect(isCameraPaused) {
        if (isCameraPaused) {
            cameraHelper.pauseCamera()
        } else {
            cameraHelper.resumeCamera(previewView)
        }
    }

    // 🔹 Keep pills synced with CameraHelper (for snapshot overlay)
    LaunchedEffect(pills) {
        cameraHelper.updateDetectionsForOverlay(pills)
    }

    // 🔹 Expose preview view to parent
    LaunchedEffect(previewView) {
        onPreviewReady(previewView)
    }

    // ───────────────────────────────────────────────
    //  UI Layout and Overlay Drawing
    // ───────────────────────────────────────────────
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = constraints.maxWidth.toFloat() - with(density) { 40.dp.toPx() }
        val containerHeight = constraints.maxHeight.toFloat() - with(density) { 40.dp.toPx() }

        val baseSide = min(containerWidth, containerHeight)
        val minSizePx = baseSide * 0.50f
        val initialBoxSide = baseSide * 0.75f
        val initialBoxSize = Size(initialBoxSide, initialBoxSide)

        var boxSize by remember { mutableStateOf(initialBoxSize) }
        var boxOffset by remember { mutableStateOf(Offset.Zero) }

        var filteredCount by remember { mutableStateOf(0) }
        LaunchedEffect(filteredCount) { onFilteredCountChanged(filteredCount) }

        val mappedPills by remember(pills) { derivedStateOf { pills.sortedBy { it.x } } }

        // Position the scanning window
        LaunchedEffect(containerWidth, containerHeight) {
            val horizontalShiftRatio = 0.05f
            val verticalShiftRatio = 0.05f
            val centeredX =
                (containerWidth - boxSize.width) / 2 + (containerWidth * horizontalShiftRatio)
            val centeredY =
                (containerHeight - boxSize.height) / 2 + (containerHeight * verticalShiftRatio)
            boxOffset = Offset(centeredX, centeredY)
        }

        LaunchedEffect(boxOffset, boxSize, containerWidth, containerHeight) {
            // Normalize box coordinates (0–1)
            val left = boxOffset.x / containerWidth
            val top = boxOffset.y / containerHeight
            val right = (boxOffset.x + boxSize.width) / containerWidth
            val bottom = (boxOffset.y + boxSize.height) / containerHeight
            viewModel.updateScanBox(left, top, right, bottom)
        }

        // Camera Preview
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Gesture (move + pinch)
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

                        // Move box
                        if (isNearHandle) {
                            boxOffset = Offset(
                                (boxOffset.x + pan.x).coerceIn(0f, maxX),
                                (boxOffset.y + pan.y).coerceIn(0f, maxY)
                            )
                        }

                        // Resize box
                        if (isInsideBox || isNearHandle) {
                            val safeZoom = zoom.coerceIn(0.8f, 1.2f)
                            val newWidth = (boxSize.width * safeZoom)
                                .coerceAtLeast(minSizePx)
                                .coerceAtMost(containerWidth)
                            val newHeight = (boxSize.height * safeZoom)
                                .coerceAtLeast(minSizePx)
                                .coerceAtMost(containerHeight)

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

        // Overlay Canvas
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

            // Dim background
            drawRect(color = Color.Black.copy(alpha = 0.8f))

            // Clear central window
            drawRoundRect(
                color = Color.Transparent,
                topLeft = boxOffset,
                size = boxSize,
                cornerRadius = CornerRadius(corner),
                blendMode = BlendMode.Clear
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

            // Handle dots
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
            val transform = viewModel.transformationMatrix

            val mapped = mappedPills.map { pill ->
                val pts = floatArrayOf(pill.x * containerWidth, pill.y * containerHeight)
                transform?.mapPoints(pts)
                pill to Offset(pts[0], pts[1])
            }

            val inside = mapped
                .filter { (_, pos) -> pos.x in left..right && pos.y in top..bottom }
                .sortedWith(compareBy<Pair<DetectedPill, Offset>> { it.second.y }.thenBy { it.second.x })

            if (inside.size != filteredCount) filteredCount = inside.size

            inside.forEachIndexed { index, (_, pos) ->
                // Outer dark circle
                drawCircle(
                    color = Color.Black.copy(alpha = 0.9f),
                    radius = with(density) { 10.dp.toPx() },
                    center = pos
                )
                // White border
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = with(density) { 13.dp.toPx() },
                    center = pos,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Text number
                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textAlign = Paint.Align.CENTER
                        textSize = 28f
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                        setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
                    }

                    // Calculate true vertical centering based on font metrics
                    val fontMetrics = paint.fontMetrics
                    val textHeight = fontMetrics.descent - fontMetrics.ascent
                    val textOffset = (textHeight / 2) - fontMetrics.descent

                    drawText("${index + 1}", pos.x, pos.y + textOffset, paint)
                }
            }
        }
    }
}
