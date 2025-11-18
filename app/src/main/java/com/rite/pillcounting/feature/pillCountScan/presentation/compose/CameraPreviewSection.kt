package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.CameraHelper
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import kotlinx.coroutines.flow.conflate

// =========================================================
// ZOOM CONTROL COMPOSABLE
// =========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoomControls(
    zoom: Float,
    min: Float = 1f,
    max: Float = 3f,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        // --- MINUS CIRCLE ---
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White.copy(alpha = 0.85f), shape = CircleShape)
                .clickable { onMinus() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "-",
                color = Color.Black,
                fontSize = 30.sp
            )
        }

        // --- SLIM MODERN SLIDER ---
        Slider(
            value = zoom,
            onValueChange = onChange,
            valueRange = min..max,
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .width(150.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.95f),
                inactiveTrackColor = Color.White.copy(alpha = 0.4f)
            ),
            track = { sliderState ->
                Box(
                    modifier = Modifier
                        .height(2.dp) // << ULTRA THIN TRACK
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(1.dp))
                )
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth(sliderState.value)
                        .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(1.dp))
                )
            }
        )


        // --- PLUS CIRCLE ---
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White.copy(alpha = 0.85f), shape = CircleShape)
                .clickable { onPlus() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.Black,
                fontSize = 25.sp
            )
        }
    }
}

// =========================================================
// MAIN CAMERA PREVIEW
// =========================================================
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CameraPreviewSection(
    viewModel: PillScanningViewModel,
    pills: List<DetectedPill>,
    isCameraPaused: Boolean,
    onFrame: (ImageProxy) -> Unit,
    onFilteredCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onPreviewStarted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val density = LocalDensity.current
    val previewView = remember { PreviewView(context) }

    val cameraHelper = remember {
        CameraHelper(context, lifecycleOwner, ContextCompat.getMainExecutor(context))
    }

    val zoomRatio = remember { mutableFloatStateOf(1f) }
    val minZoom = 1f
    val maxZoom = 2f

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Listen to CameraHelper zoom
    LaunchedEffect(Unit) {
        cameraHelper.zoomFlow.collect { zoomRatio.value = it }
    }

    // Start camera
    LaunchedEffect(cameraHelper) {
        viewModel.attachCameraHelper(cameraHelper)
        cameraHelper.startCamera(previewView)
        onPreviewStarted?.invoke()

        cameraHelper.frameFlow
            .conflate()
            .collect { image -> onFrame(image) }
    }

    // Pause/Resume
    LaunchedEffect(isCameraPaused) {
        if (isCameraPaused) cameraHelper.pauseCamera()
        else cameraHelper.resumeCamera(previewView)
    }

    // =========================================================
    // MAIN LAYOUT
    // =========================================================
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = if (isLandscape) 0.dp else 60.dp,
                start = if (isLandscape) 60.dp else 0.dp
            )
    ) {
        val squareSide = minOf(constraints.maxWidth, constraints.maxHeight)

        // ==========================
        // SQUARE CAMERA PREVIEW AREA
        // ==========================
        Box(
            modifier = Modifier
                .size(with(density) { squareSide.toDp() })
                .align(Alignment.Center)
        ) {
            // CAMERA PREVIEW
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            val current = cameraHelper.getCurrentZoomRatio() ?: 1f
                            val target = (current * zoom).coerceIn(minZoom, maxZoom)
                            zoomRatio.value = target
                            cameraHelper.setZoom(target)
                        }
                    }
            )

            // PILL MARKER OVERLAY
            Canvas(modifier = Modifier.matchParentSize()) {
                val previewWidth = size.width
                val previewHeight = size.height
                // Prepare text paint
                val textPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    textSize = 26f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                    setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
                }

                // Map normalized coordinates to preview pixels
                val mapped = pills.map { pill ->
                    val px = pill.x * previewWidth
                    val py = pill.y * previewHeight
                    pill to Offset(px, py)
                }

                // Update count
                viewModel.updateFilteredPills(mapped.map { it.first })
                onFilteredCountChanged(mapped.size)

                // Draw pills
                drawIntoCanvas { canvas ->
                    mapped.forEachIndexed { i, (_, pos) ->
                        val isLast = i == mapped.lastIndex
                        val scale = 1f

                        val innerRadius =
                            with(density) { (if (isLast) 15.dp else 11.dp).toPx() * scale }
                        val outerRadius =
                            with(density) { (if (isLast) 19.dp else 14.dp).toPx() * scale }
                        val strokeWidth =
                            with(density) { (if (isLast) 3.dp else 2.dp).toPx() * scale }
                        val textSizePx = (if (isLast) 34 else 28) * scale

                        textPaint.textSize = textSizePx
                        if (isLast) {
                            textPaint.setShadowLayer(8f, 0f, 0f, android.graphics.Color.YELLOW)
                        } else {
                            textPaint.setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
                        }

                        // background + border
                        drawCircle(
                            color = if (isLast) Color.Black.copy(alpha = 0.9f) else Color.Black.copy(
                                alpha = 0.5f
                            ),
                            radius = innerRadius,
                            center = pos
                        )
                        drawCircle(
                            color = if (isLast) Color.White.copy(alpha = 0.7f) else Color.White.copy(
                                alpha = 0.5f
                            ),
                            radius = outerRadius,
                            center = pos,
                            style = Stroke(width = strokeWidth)
                        )

                        // pill number
                        val fm = textPaint.fontMetrics
                        val off = (fm.descent - fm.ascent) / 2 - fm.descent
                        canvas.nativeCanvas.drawText("${i + 1}", pos.x, pos.y + off, textPaint)
                    }
                }
            }
        }

        // ==========================
        // FINAL ZOOM BAR (Real Bottom)
        // ==========================
        ZoomControls(
            zoom = zoomRatio.value,
            min = minZoom,
            max = maxZoom,
            onMinus = {
                val z = (zoomRatio.value - 0.1f).coerceIn(minZoom, maxZoom)
                zoomRatio.value = z
                cameraHelper.setZoom(z)
            },
            onPlus = {
                val z = (zoomRatio.value + 0.1f).coerceIn(minZoom, maxZoom)
                zoomRatio.value = z
                cameraHelper.setZoom(z)
            },
            onChange = {
                zoomRatio.value = it
                cameraHelper.setZoom(it)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}
