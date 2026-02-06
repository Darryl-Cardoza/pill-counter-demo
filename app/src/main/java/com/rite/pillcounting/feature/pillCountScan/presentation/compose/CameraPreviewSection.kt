@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
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
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoomControls(
    zoom: Float,
    min: Float = 1f,
    max: Float = 3f,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val secondary = Color.White
    val zoomLabel = "${String.format("%.1f", zoom)}x"

    // 0f..1f
    val fraction = ((zoom - min) / (max - min)).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {

        val trackWidth = maxWidth - 24.dp

        Column {

            // ZOOM VALUE ABOVE THUMB
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            ) {
                Text(
                    text = zoomLabel,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = trackWidth * fraction)
                )
            }

            // 🔹 SLIDER
            Slider(
                value = zoom,
                onValueChange = onChange,
                valueRange = min..max,
                modifier = Modifier.fillMaxWidth(),

                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),

                // CIRCLE THUMB
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(secondary, CircleShape)
                    )
                },

                // 🧵 CUSTOM TRACK
                track = { sliderState ->
                    val trackFraction =
                        (sliderState.value - sliderState.valueRange.start) /
                                (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                Color.Gray.copy(alpha = 0.35f),
                                RoundedCornerShape(2.dp)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(trackFraction.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
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
    onPreviewStarted: (() -> Unit)? = null,
    onPreviewSizeKnown: ((width: Int, height: Int) -> Unit)? = null
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

    LaunchedEffect(Unit) {
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
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
//            .padding(
//                top = if (isLandscape) 0.dp else 60.dp,
//                start = if (isLandscape) 60.dp else 0.dp
//            )
    ) {
//        val squareSide = minOf(constraints.maxWidth, constraints.maxHeight)

        // ==========================
        // SQUARE CAMERA PREVIEW AREA
        // ==========================
        Box(
            modifier = Modifier
//                .size(with(density) { squareSide.toDp() })
                .align(Alignment.Center)
        ) {
            // CAMERA PREVIEW
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        // This is the REAL preview size on screen
                        onPreviewSizeKnown?.invoke(size.width, size.height)
                    }
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
                val previewW = size.width
                val previewH = size.height

                // 1. Prepare Text Paint
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 28f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.BOLD
                    )
                    setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
                }

                // 2. Map Coordinates with Vertical/Horizontal Shift correction
                val mapped = pills.map { pill ->
                    val px = pill.x * previewW
                    val py = pill.y * previewH
                    pill to Offset(px, py)
                }

                // Update count
                viewModel.updateFilteredPills(mapped.map { it.first })
                onFilteredCountChanged(mapped.size)

                // 3. Draw Pills
                drawIntoCanvas { canvas ->
                    mapped.forEachIndexed { i, (_, pos) ->
                        val isLast = i == mapped.lastIndex

                        // Scaled sizes for better visibility
                        val innerRadius = 6.dp.toPx()
                        val outerRadius = 9.dp.toPx()
                        val strokeWidth = 2.dp.toPx()
                        val textSizePx = if (isLast) 36f else 28f

                        textPaint.textSize = textSizePx
                        if (isLast) {
                            textPaint.setShadowLayer(10f, 0f, 0f, android.graphics.Color.YELLOW)
                        } else {
                            textPaint.setShadowLayer(6f, 0f, 0f, android.graphics.Color.BLACK)
                        }

                        // A. Draw Glow/Background for visibility
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.6f),
                            radius = outerRadius,
                            center = pos
                        )

                        // B. Draw Border
                        drawCircle(
                            color = if (isLast) Color.Yellow else Color.White,
                            radius = outerRadius,
                            center = pos,
                            style = Stroke(width = strokeWidth)
                        )
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
            onChange = { value ->
                val stepped = (value * 10f).toInt() / 10f
                zoomRatio.value = stepped
                cameraHelper.setZoom(stepped)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}
