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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
import kotlin.math.min

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CameraPreviewSection(
    viewModel: PillScanningViewModel,
    pills: List<DetectedPill>,
    isCameraPaused: Boolean,
    onFrame: (ImageProxy) -> Unit,
    onFilteredCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val previewView = remember { PreviewView(context) }

    val cameraHelper = remember {
        CameraHelper(context, lifecycleOwner, ContextCompat.getMainExecutor(context))
    }

    // Attach once
    LaunchedEffect(Unit) { viewModel.attachCameraHelper(cameraHelper) }

    // Camera feed collection
    LaunchedEffect(Unit) {
        cameraHelper.startCamera(previewView)
        coroutineScope.launch(Dispatchers.Default) {
            cameraHelper.frameFlow.collect { image ->
                val start = System.currentTimeMillis()
                onFrame(image)
                val elapsed = System.currentTimeMillis() - start
                delay((elapsed * 0.5).coerceIn(60.0, 150.0).toLong())
            }
        }
    }

    LaunchedEffect(isCameraPaused) {
        if (isCameraPaused) cameraHelper.pauseCamera()
        else cameraHelper.resumeCamera(previewView)
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        val baseSide = min(containerWidth, containerHeight)
        val initialBox = Size(baseSide * 0.7f, baseSide * 0.7f)

        var boxOffset by remember {
            mutableStateOf(
                Offset(
                    (containerWidth - initialBox.width) / 2,
                    (containerHeight - initialBox.height) / 2
                )
            )
        }
        var boxSize by remember { mutableStateOf(initialBox) }
        var filteredCount by remember { mutableStateOf(0) }

        // Cache Paint objects to avoid GC churn
        val textPaint = remember {
            Paint().apply {
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 26f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
            }
        }

        // Smooth gesture handler
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
                            right + with(density) { 14.dp.toPx() },
                            bottom - with(density) { 8.dp.toPx() }
                        )
                        val handleRadius = with(density) { 40.dp.toPx() }

                        val isOnHandle = (centroid - handleCenter).getDistance() <= handleRadius

                        // Move if on handle
                        if (isOnHandle) {
                            val maxX = (containerWidth - boxSize.width).coerceAtLeast(0f)
                            val maxY = (containerHeight - boxSize.height).coerceAtLeast(0f)
                            boxOffset = Offset(
                                (boxOffset.x + pan.x).coerceIn(0f, maxX),
                                (boxOffset.y + pan.y).coerceIn(0f, maxY)
                            )
                        }

                        if (zoom != 1f) {
                            val currentZoom = cameraHelper.getCurrentZoomRatio() ?: 1f
                            val targetZoom = (currentZoom * zoom).coerceIn(1f, 1f)
                            cameraHelper.setZoom(targetZoom)
                        }

                        // Resize smoothly (pinch)
                        val minBox = baseSide * 0.3f
                        val newWidth = (boxSize.width * zoom)
                            .coerceIn(minBox, containerWidth)
                        val newHeight = (boxSize.height * zoom)
                            .coerceIn(minBox, containerHeight)

                        if (zoom != 1f) {
                            val deltaW = (newWidth - boxSize.width) / 2
                            val deltaH = (newHeight - boxSize.height) / 2
                            boxOffset = Offset(
                                (boxOffset.x - deltaW).coerceIn(0f, containerWidth - newWidth),
                                (boxOffset.y - deltaH).coerceIn(0f, containerHeight - newHeight)
                            )
                            boxSize = Size(newWidth, newHeight)
                        }
                    }
                }
        )

        // Draw only the overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val corner = with(density) { 16.dp.toPx() }
            val edge = with(density) { 26.dp.toPx() }
            val stroke = with(density) { 4.dp.toPx() }
            val dotR = with(density) { 3.dp.toPx() }
            val dotX = with(density) { 14.dp.toPx() }
            val dotY = with(density) { 12.dp.toPx() }

            val left = boxOffset.x
            val top = boxOffset.y
            val right = left + boxSize.width
            val bottom = top + boxSize.height

            // Dim BG except window
            drawRect(Color.Black.copy(alpha = 0.75f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = boxOffset,
                size = boxSize,
                cornerRadius = CornerRadius(corner),
                blendMode = BlendMode.Clear
            )

            // Corner markers
            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(Color.White, Offset(x, y), Offset(x + dx, y), strokeWidth = stroke)
                drawLine(Color.White, Offset(x, y), Offset(x, y + dy), strokeWidth = stroke)
            }
            corner(left, top, edge, edge)
            corner(right, top, -edge, edge)
            corner(left, bottom, edge, -edge)
            corner(right, bottom, -edge, -edge)

            // Handle dots
            val hx = right + 12.dp.toPx()
            val hy = bottom - 5.dp.toPx()
            val sy = hy - (dotY * 2)
            for (c in 0..1) for (r in 0..2)
                drawCircle(Color.White, dotR, Offset(hx + c * dotX, sy + r * dotY))

            // Draw pills
            val mapped = pills.map {
                it to Offset(it.x * containerWidth, it.y * containerHeight)
            }.filter { (_, p) -> p.x in left..right && p.y in top..bottom }

            if (mapped.size != filteredCount) {
                filteredCount = mapped.size
                val filteredList = mapped.map { it.first }
                viewModel.updateFilteredPills(filteredList)
                onFilteredCountChanged(filteredCount)
            }

            drawIntoCanvas { canvas ->
                // dynamic scale based on box size
                val scale = (boxSize.width / containerWidth).coerceIn(0.4f, 1.2f)
                val textSizePx = 28 * scale

                // use the same cached Paint, just change text size dynamically
                textPaint.textSize = textSizePx

                mapped.forEachIndexed { i, (_, pos) ->
                    // detect if this is the last pill
                    val isLast = i == mapped.lastIndex

                    // base dynamic scale
                    val scale = (boxSize.width / containerWidth).coerceIn(0.4f, 1.2f)

                    // dynamically adjust sizes (last pill gets emphasized)
                    val innerRadius = with(density) { (if (isLast) 15.dp else 11.dp).toPx() * scale }
                    val outerRadius = with(density) { (if (isLast) 19.dp else 14.dp).toPx() * scale }
                    val strokeWidth = with(density) { (if (isLast) 3.dp else 2.dp).toPx() * scale }
                    val textSizePx = (if (isLast) 34 else 28) * scale

                    // update text paint dynamically
                    textPaint.textSize = textSizePx
                    if (isLast) {
                        textPaint.setShadowLayer(8f, 0f, 0f, android.graphics.Color.YELLOW)
                        textPaint.color = android.graphics.Color.WHITE
                    } else {
                        textPaint.setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
                        textPaint.color = android.graphics.Color.WHITE
                    }

                    // draw the circle layers
                    drawCircle(
                        color = if (isLast) Color.Black.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.5f),
                        radius = innerRadius,
                        center = pos
                    )
                    drawCircle(
                        color = if (isLast) Color.Black.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.7f),
                        radius = outerRadius,
                        center = pos,
                        style = Stroke(width = strokeWidth)
                    )

                    // draw the number
                    val fm = textPaint.fontMetrics
                    val off = (fm.descent - fm.ascent) / 2 - fm.descent
                    canvas.nativeCanvas.drawText("${i + 1}", pos.x, pos.y + off, textPaint)
                }

            }
        }
    }
}

