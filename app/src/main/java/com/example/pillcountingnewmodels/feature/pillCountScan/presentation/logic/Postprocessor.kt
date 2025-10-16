package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic

import android.graphics.Matrix
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

/**
 * Handles post-processing of model outputs into real-screen coordinates
 * with proper rotation, scaling, and portrait handling.
 */
object Postprocessor {

    private const val INPUT_SIZE = 640
    private const val CONFIDENCE_THRESHOLD = 0.5f
    private const val IDX_CX = 0
    private const val IDX_CY = 1
    private const val CLASS_START = 4
    private const val NUM_CLASSES = 2

    data class Detection(
        val boundingBox: RectF?,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    /**
     * Converts normalized model outputs to screen-space detections.
     */
    fun parseDetections(
        det: Array<FloatArray>,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int,
        viewWidth: Int,
        viewHeight: Int,
        isFrontCamera: Boolean = false
    ): Pair<List<Detection>, Matrix> {

        val cxArr = det[IDX_CX]
        val cyArr = det[IDX_CY]
        val rawDetections = mutableListOf<Detection>()

        val rotation = normalizeRotation(rotationDegrees)

        // Step 1: Determine rotated frame dimensions
        val rotatedFrameWidth: Float
        val rotatedFrameHeight: Float
        if (rotation == 90 || rotation == 270) {
            rotatedFrameWidth = imageHeight.toFloat()
            rotatedFrameHeight = imageWidth.toFloat()
        } else {
            rotatedFrameWidth = imageWidth.toFloat()
            rotatedFrameHeight = imageHeight.toFloat()
        }

        // Step 2: Build a robust transformation matrix (model → screen)
        val transformationMatrix = Matrix().apply {
            // Undo model scaling (640x640 → actual input frame size)
            postScale(
                rotatedFrameWidth / INPUT_SIZE.toFloat(),
                rotatedFrameHeight / INPUT_SIZE.toFloat()
            )

            // Scale to fill the PreviewView (CENTER_CROP behavior)
            val viewToFrameScale = max(
                viewWidth / rotatedFrameWidth,
                viewHeight / rotatedFrameHeight
            )
            postScale(viewToFrameScale, viewToFrameScale)

            // Center image inside PreviewView
            val scaledImageWidth = rotatedFrameWidth * viewToFrameScale
            val scaledImageHeight = rotatedFrameHeight * viewToFrameScale
            val dx = (viewWidth - scaledImageWidth) / 2f
            val dy = (viewHeight - scaledImageHeight) / 2f
            postTranslate(dx, dy)

            // Handle camera rotation (rotate to match display)
            if (rotation != 0) {
                postRotate(rotation.toFloat(), viewWidth / 2f, viewHeight / 2f)
            }

            // Optional: Mirror horizontally for front camera
            if (isFrontCamera) {
                postScale(-1f, 1f, viewWidth / 2f, viewHeight / 2f)
            }
        }

        // Step 3: Map all detections
        val modelPoint = floatArrayOf(0f, 0f)
        for (i in cxArr.indices) {
            var bestScore = Float.NEGATIVE_INFINITY
            for (ci in 0 until NUM_CLASSES) {
                val score = det[CLASS_START + ci][i]
                if (score > bestScore) bestScore = score
            }
            if (bestScore < CONFIDENCE_THRESHOLD) continue

            modelPoint[0] = cxArr[i] * INPUT_SIZE
            modelPoint[1] = cyArr[i] * INPUT_SIZE

            transformationMatrix.mapPoints(modelPoint)

            val px = modelPoint[0]
            val py = modelPoint[1]
            val boxSize = 20f
            val detectionBox = RectF(
                px - boxSize / 2f,
                py - boxSize / 2f,
                px + boxSize / 2f,
                py + boxSize / 2f
            )

            rawDetections.add(
                Detection(
                    boundingBox = detectionBox,
                    confidence = bestScore,
                    pixelX = px.coerceIn(0f, viewWidth.toFloat()),
                    pixelY = py.coerceIn(0f, viewHeight.toFloat())
                )
            )
        }

        val filtered = nonMaxSuppression(rawDetections, 0.5f)
        return Pair(filtered, transformationMatrix)
    }

    private fun normalizeRotation(deg: Int): Int {
        return ((deg % 360) + 360) % 360
    }

    // ---------- Non-max suppression ----------
    private fun nonMaxSuppression(
        detections: List<Detection>,
        iouThreshold: Float
    ): List<Detection> {
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val result = mutableListOf<Detection>()

        while (sorted.isNotEmpty()) {
            val highest = sorted.removeAt(0)
            result.add(highest)
            sorted.removeAll { iou(highest.boundingBox, it.boundingBox) > iouThreshold }
        }
        return result
    }

    private fun iou(a: RectF?, b: RectF?): Float {
        if (a == null || b == null) return 0f
        val left = max(a.left, b.left)
        val top = max(a.top, b.top)
        val right = min(a.right, b.right)
        val bottom = min(a.bottom, b.bottom)
        val interArea = max(0f, right - left) * max(0f, bottom - top)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        return if (areaA + areaB - interArea == 0f) 0f else interArea / (areaA + areaB - interArea)
    }
}
