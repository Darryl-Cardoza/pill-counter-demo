@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.responsiveSp
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.CameraHelper
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import kotlinx.coroutines.flow.conflate
import androidx.compose.ui.text.TextStyle

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
                    fontSize = responsiveSp(18.sp),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f), // shadow color
                            offset = Offset(1f, 1f),                 // x and y offset
                            blurRadius = 4f                          // blur amount
                        )
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = trackWidth * fraction)
                )

            }

            // SLIDER
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

                // CUSTOM TRACK
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
    imageFrameWidth: Int,
    imageFrameHeight: Int,
    onFrame: (ImageProxy) -> Unit,
    onFilteredCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onPreviewStarted: (() -> Unit)? = null,
    onPreviewSizeKnown: ((width: Int, height: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    val cameraHelper = remember {
        CameraHelper(context, lifecycleOwner, ContextCompat.getMainExecutor(context))
    }

    val zoomRatio = remember { mutableFloatStateOf(1f) }
    val minZoom = 1f
    val maxZoom = 2f

    var showPop by remember { mutableStateOf(false) }
    var popKey by remember { mutableIntStateOf(0) }
    var popText by remember { mutableStateOf("+0") }

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

    // FILL_CENTER ensures it fully fills your 70% pane!
    LaunchedEffect(Unit) {
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
    }

    LaunchedEffect(Unit) {
        viewModel.addPopEvents.collect { count ->
            popText = "+$count"
            popKey++
            showPop = true
            kotlinx.coroutines.delay(700)
            showPop = false
        }
    }

    // Pause/Resume
    LaunchedEffect(isCameraPaused) {
        if (isCameraPaused) cameraHelper.pauseCamera()
        else cameraHelper.resumeCamera(previewView)
    }

    // =========================================================
    // MAIN LAYOUT
    // =========================================================
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {

        // This Box simply fills the available space (70% of the screen)
        Box(modifier = Modifier.fillMaxSize()) {

            // CAMERA PREVIEW
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
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

            AnimatedVisibility(
                visible = showPop,
                enter = fadeIn() +
                        slideInVertically(initialOffsetY = { it / 2 }) +
                        scaleIn(),
                exit = fadeOut() +
                        slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1f)
            ) {
                key(popKey) {
                    AddCountBubble(text = popText)
                }
            }

            // PILL MARKER OVERLAY
            Canvas(modifier = Modifier.matchParentSize()) {
                val previewW = size.width
                val previewH = size.height
                val isLandscape = previewW > previewH

                // Align camera frame dimensions to match the screen's orientation
                val actualFrameW =
                    if (isLandscape) maxOf(imageFrameWidth, imageFrameHeight).toFloat() else minOf(
                        imageFrameWidth,
                        imageFrameHeight
                    ).toFloat()
                val actualFrameH =
                    if (isLandscape) minOf(imageFrameWidth, imageFrameHeight).toFloat() else maxOf(
                        imageFrameWidth,
                        imageFrameHeight
                    ).toFloat()

                var mapped = pills.map { it to Offset(0f, 0f) }

                // THE MATH: Calculate FILL_CENTER cropping correctly
                if (actualFrameW > 0f && actualFrameH > 0f) {
                    // Find the scale applied by FILL_CENTER
                    val scale = maxOf(previewW / actualFrameW, previewH / actualFrameH)

                    val scaledW = actualFrameW * scale
                    val scaledH = actualFrameH * scale

                    // The offsets are the parts of the image that get cropped out!
                    val offsetX = (previewW - scaledW) / 2f
                    val offsetY = (previewH - scaledH) / 2f

                    // Map the coordinates using the offsets
                    mapped = pills.map { pill ->
                        val px = (pill.x * scaledW) + offsetX
                        val py = (pill.y * scaledH) + offsetY
                        pill to Offset(px, py)
                    }
                }

                // Update count based on mapped pills
                viewModel.updateFilteredPills(mapped.map { it.first })
                onFilteredCountChanged(mapped.size)

                drawIntoCanvas { canvas ->
                    mapped.forEachIndexed { i, (_, pos) ->

                        // Prevent drawing dots that are outside the visible cropped area!
                        if (pos.x in 0f..previewW && pos.y in 0f..previewH) {
                            val isLast = i == mapped.lastIndex
                            val outerRadius = 7.dp.toPx()
                            val strokeWidth = 2.dp.toPx()

                            drawCircle(
                                color = Color.Black.copy(alpha = 0.6f),
                                radius = outerRadius,
                                center = pos
                            )

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
        }

        // ==========================
        // FINAL ZOOM BAR
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
