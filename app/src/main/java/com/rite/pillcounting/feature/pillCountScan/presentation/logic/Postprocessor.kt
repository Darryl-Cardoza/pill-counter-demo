package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Matrix
import android.graphics.RectF
import com.rite.pillcounting.core.utils.logger.AppLogger
import kotlin.math.max
import kotlin.math.min

/**
 * Converts raw model outputs into screen-space detections and transformation matrices.
 *
 * Responsibilities:
 * - Convert model output coordinates (from TFLite) into real display-space points.
 * - Apply scaling, rotation, and mirroring transformations based on device orientation.
 * - Filter low-confidence predictions and perform Non-Maximum Suppression (NMS).
 *
 * This class provides accurate spatial mapping between model detections and the
 * actual camera preview surface, enabling correct overlay visualization.
 */
object Postprocessor {

    private const val INPUT_SIZE = 640
    private const val CONFIDENCE_THRESHOLD = 0.5f
    private const val IOU_THRESHOLD = 0.5f
    private const val IDX_CX = 0
    private const val IDX_CY = 1
    private const val CLASS_START = 4
    private const val NUM_CLASSES = 2

    private val logger = AppLogger.create<Postprocessor>()

    /**
     * Represents a single detection result in screen coordinates.
     *
     * @property boundingBox The bounding rectangle in display coordinates.
     * @property confidence Model confidence score (0.0–1.0).
     * @property pixelX The mapped X-coordinate on the preview surface.
     * @property pixelY The mapped Y-coordinate on the preview surface.
     */
    data class Detection(
        val boundingBox: RectF?,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    /**
     * Converts raw model tensor output into a list of [Detection] objects with proper
     * coordinate transformations for display rendering.
     *
     * Operations:
     *  1. Apply confidence filtering.
     *  2. Handle model-to-screen coordinate mapping and orientation correction.
     *  3. Generate a transformation matrix for overlay rendering.
     *  4. Perform Non-Maximum Suppression to filter overlapping detections.
     *
     * @param det Model detection tensor (flattened YOLO-like output).
     * @param imageWidth Width of the original bitmap/image.
     * @param imageHeight Height of the original bitmap/image.
     * @param rotationDegrees Camera rotation in degrees (0/90/180/270).
     * @param viewWidth Width of the display surface.
     * @param viewHeight Height of the display surface.
     * @param isFrontCamera Whether the input source is the front camera (applies mirroring).
     * @return A [Pair] containing the filtered list of [Detection] and the applied [Matrix].
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
        val startTime = System.currentTimeMillis()
        logger.i(
            "Postprocessing started | Input=${imageWidth}x${imageHeight}, Rotation=${rotationDegrees}°, " +
                    "View=${viewWidth}x${viewHeight}, FrontCamera=$isFrontCamera"
        )

        val rotation = normalizeRotation(rotationDegrees)
        val cxArr = det[IDX_CX]
        val cyArr = det[IDX_CY]
        val rawDetections = mutableListOf<Detection>()

        // Step 1: Compute rotated frame dimensions
        val (rotatedFrameWidth, rotatedFrameHeight) = if (rotation == 90 || rotation == 270) {
            imageHeight.toFloat() to imageWidth.toFloat()
        } else {
            imageWidth.toFloat() to imageHeight.toFloat()
        }
        logger.d("Rotated frame dimensions computed: ${rotatedFrameWidth.toInt()}x${rotatedFrameHeight.toInt()}")

        // Step 2: Build transformation matrix (model → screen)
        val transformationMatrix = Matrix().apply {
            // Undo model normalization (640x640 → actual frame size)
            postScale(
                rotatedFrameWidth / INPUT_SIZE.toFloat(),
                rotatedFrameHeight / INPUT_SIZE.toFloat()
            )

            // Apply scale-to-fit (CENTER_CROP equivalent)
            val scaleToFit = max(
                viewWidth / rotatedFrameWidth,
                viewHeight / rotatedFrameHeight
            )
            postScale(scaleToFit, scaleToFit)

            // Center within view
            val scaledWidth = rotatedFrameWidth * scaleToFit
            val scaledHeight = rotatedFrameHeight * scaleToFit
            val dx = (viewWidth - scaledWidth) / 2f
            val dy = (viewHeight - scaledHeight) / 2f
            postTranslate(dx, dy)

            // Apply rotation correction
            if (rotation != 0) {
                postRotate(rotation.toFloat(), viewWidth / 2f, viewHeight / 2f)
            }

            // Apply horizontal flip for front camera
            if (isFrontCamera) {
                postScale(-1f, 1f, viewWidth / 2f, viewHeight / 2f)
            }
        }

        logger.d(
            "Transformation matrix created | Scale=${"%.2f".format(max(viewWidth / rotatedFrameWidth, viewHeight / rotatedFrameHeight))}, " +
                    "Translate(dx=${"%.1f".format((viewWidth - rotatedFrameWidth) / 2f)}, dy=${"%.1f".format((viewHeight - rotatedFrameHeight) / 2f)})"
        )

        // Step 3: Convert detections to display coordinates
        val mapStart = System.currentTimeMillis()
        val modelPoint = FloatArray(2)

        for (i in cxArr.indices) {
            // Confidence filter
            var bestScore = Float.NEGATIVE_INFINITY
            for (ci in 0 until NUM_CLASSES) {
                val score = det[CLASS_START + ci][i]
                if (score > bestScore) bestScore = score
            }
            if (bestScore < CONFIDENCE_THRESHOLD) continue

            // Map coordinates based on rotation
            when (rotation) {
                90 -> {
                    modelPoint[0] = cyArr[i] * INPUT_SIZE
                    modelPoint[1] = (1f - cxArr[i]) * INPUT_SIZE
                }
                270 -> {
                    modelPoint[0] = (1f - cyArr[i]) * INPUT_SIZE
                    modelPoint[1] = cxArr[i] * INPUT_SIZE
                }
                else -> {
                    modelPoint[0] = cxArr[i] * INPUT_SIZE
                    modelPoint[1] = cyArr[i] * INPUT_SIZE
                }
            }

            // Apply transformation matrix
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

        val detectionTime = System.currentTimeMillis() - mapStart
        logger.d("Mapped ${rawDetections.size} raw detections in $detectionTime ms (confidence ≥ $CONFIDENCE_THRESHOLD)")

        // Step 4: Non-Maximum Suppression
        val nmsStart = System.currentTimeMillis()
        val filtered = nonMaxSuppression(rawDetections)
        val nmsTime = System.currentTimeMillis() - nmsStart
        logger.i("Non-Max Suppression reduced ${rawDetections.size} → ${filtered.size} | IOU=$IOU_THRESHOLD | Time=${nmsTime} ms")

        val totalTime = System.currentTimeMillis() - startTime
        logger.i("Postprocessing completed in $totalTime ms | Final detections=${filtered.size}")

        return Pair(filtered, transformationMatrix)
    }

    /**
     * Normalizes arbitrary rotation degrees into [0, 90, 180, 270].
     *
     * @param deg The rotation in degrees.
     * @return A normalized rotation angle between 0–359.
     */
    private fun normalizeRotation(deg: Int): Int {
        return ((deg % 360) + 360) % 360
    }

    /**
     * Applies Non-Maximum Suppression (NMS) to eliminate redundant overlapping detections.
     *
     * @param detections The full list of detected bounding boxes.
     * @param iouThreshold The IoU threshold above which overlaps are suppressed.
     * @return A filtered list of unique, high-confidence detections.
     */
    private fun nonMaxSuppression(
        detections: List<Detection>,
        iouThreshold: Float = IOU_THRESHOLD
    ): List<Detection> {
        if (detections.isEmpty()) return emptyList()

        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val result = mutableListOf<Detection>()

        while (sorted.isNotEmpty()) {
            val highest = sorted.removeAt(0)
            result.add(highest)
            sorted.removeAll { iou(highest.boundingBox, it.boundingBox) > iouThreshold }
        }

        logger.d("NMS completed | Input=${detections.size}, Output=${result.size}, Threshold=$iouThreshold")
        return result
    }

    /**
     * Calculates the Intersection over Union (IoU) between two bounding boxes.
     *
     * @param a The first bounding box.
     * @param b The second bounding box.
     * @return IoU value between 0.0 and 1.0 (0 = no overlap, 1 = perfect overlap).
     */
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
