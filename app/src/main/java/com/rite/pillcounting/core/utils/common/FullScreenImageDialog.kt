package com.rite.pillcounting.core.utils.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import java.io.File
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun FullScreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val scope = rememberCoroutineScope()

        val scale = remember { Animatable(1f) }
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }

        var containerSize by remember { mutableStateOf(IntSize.Zero) }

        val minScale = 1f
        val maxScale = 5f
        val doubleTapScale = 2.5f

        fun clampOffsets(s: Float, ox: Float, oy: Float): Pair<Float, Float> {
            val maxX = (containerSize.width * (s - 1f)) / 2f
            val maxY = (containerSize.height * (s - 1f)) / 2f
            return ox.coerceIn(-maxX, maxX) to oy.coerceIn(-maxY, maxY)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onSizeChanged { containerSize = it }
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = File(imagePath)),
                contentDescription = "Fullscreen Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()

                    // Double tap zoom (animated)
                    .pointerInput(containerSize, scale.value) {
                        detectTapGestures(
                            onTap = {
                                if (scale.value <= 1.01f) onDismiss()
                            },
                            onDoubleTap = { tap ->
                                scope.launch {
                                    val current = scale.value

                                    if (current > 1.01f) {
                                        // zoom out
                                        coroutineScope {
                                            launch { scale.animateTo(1f, tween(180)) }
                                            launch { offsetX.animateTo(0f, tween(180)) }
                                            launch { offsetY.animateTo(0f, tween(180)) }
                                        }
                                    } else {
                                        val target = doubleTapScale.coerceIn(minScale, maxScale)

                                        val cx = containerSize.width / 2f
                                        val cy = containerSize.height / 2f
                                        val dx = tap.x - cx
                                        val dy = tap.y - cy

                                        val targetOx = -(dx * (target - 1f))
                                        val targetOy = -(dy * (target - 1f))

                                        val (clampedX, clampedY) =
                                            clampOffsets(target, targetOx, targetOy)

                                        coroutineScope {
                                            launch { scale.animateTo(target, tween(180)) }
                                            launch { offsetX.animateTo(clampedX, tween(180)) }
                                            launch { offsetY.animateTo(clampedY, tween(180)) }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // Pinch zoom + pan (snap)
                    .pointerInput(containerSize) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scope.launch {
                                val newScale = (scale.value * zoom).coerceIn(minScale, maxScale)
                                scale.snapTo(newScale)

                                val newOx = offsetX.value + if (newScale > 1f) pan.x else 0f
                                val newOy = offsetY.value + if (newScale > 1f) pan.y else 0f

                                val (clampedX, clampedY) = clampOffsets(newScale, newOx, newOy)
                                offsetX.snapTo(clampedX)
                                offsetY.snapTo(clampedY)

                                if (newScale <= 1.01f) {
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offsetX.value
                        translationY = offsetY.value
                    }
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}