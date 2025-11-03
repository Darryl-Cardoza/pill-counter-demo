package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Matrix
import android.graphics.RectF
import com.rite.pillcounting.core.utils.logger.AppLogger
import kotlin.math.max
import kotlin.math.min

/**
 * Converts raw TensorFlow model outputs into screen-space detections.
 *
 * The transformation mirrors the preprocessing pipeline used before inference:
 *  1. Original rotated bitmap → center-cropped and scaled to 640×640 (model input)
 *  2. This class reverses that process, mapping model-space detections
 *     back to the preview surface shown on screen.
 *
 * Responsibilities:
 *  - Map normalized model coordinates to actual preview coordinates
 *  - Recreate crop/scale offsets for correct spatial alignment
 *  - Optionally mirror coordinates for front-facing cameras
 *  - Perform Non-Maximum Suppression (NMS) to remove overlapping detections
 */
object Postprocessor {

    private const val INPUT_SIZE = 640
    private const val CONFIDENCE_THRESHOLD = 0.70f
    private const val IOU_THRESHOLD = 0.5f

//    private const val IDX_CX = 0
//    private const val IDX_CY = 1
//    private const val CLASS_START = 4
//    private const val NUM_CLASSES = 2

    private val logger = AppLogger.create<Postprocessor>()

    /**
     * Represents a single detection result in screen coordinates.
     *
     * @property boundingBox The bounding box rectangle in display coordinates.
     * @property confidence   Model confidence score (0.0–1.0).
     * @property pixelX       X-coordinate on the preview surface.
     * @property pixelY       Y-coordinate on the preview surface.
     */
    data class Detection(
        val boundingBox: RectF?,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    /**
     * Maps model detections to display-space coordinates.
     *
     * @param det            Flattened output tensor from the TFLite model.
     * @param imageWidth     Width of the rotated bitmap passed to preprocessing.
     * @param imageHeight    Height of the rotated bitmap passed to preprocessing.
     * @param rotationDegrees Rotation degrees (for logging only; already applied upstream).
     * @param viewWidth      Width of the camera preview surface.
     * @param viewHeight     Height of the camera preview surface.
     * @param isFrontCamera  Whether the input source is the front camera.
     * @return Pair of (filtered detections, transformation matrix used).
     */
    /*fun parseDetections(
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
            "Postprocessing started | Rotated=${imageWidth}x$imageHeight, " +
                    "View=${viewWidth}x$viewHeight, Rotation=$rotationDegrees°, FrontCamera=$isFrontCamera"
        )

        val srcW = imageWidth.toFloat()
        val srcH = imageHeight.toFloat()

        // --- Step 1: Recreate preprocessing crop & scale (640x640 center crop) ---
        val scaleModel = max(INPUT_SIZE / srcW, INPUT_SIZE / srcH)
        val scaledW = srcW * scaleModel
        val scaledH = srcH * scaleModel
        val xOffset = (scaledW - INPUT_SIZE) / 2f
        val yOffset = (scaledH - INPUT_SIZE) / 2f

        // --- Step 2: Model → rotated frame (inverse crop & scale) ---
        val transform = Matrix().apply {
            postTranslate(xOffset, yOffset)
            postScale(1f / scaleModel, 1f / scaleModel)
        }

        // --- Step 3: Rotated frame → PreviewView (CENTER_CROP mapping) ---
        val scaleToView = max(viewWidth / srcW, viewHeight / srcH)
        val scaledViewW = srcW * scaleToView
        val scaledViewH = srcH * scaleToView
        val dx = (viewWidth - scaledViewW) / 2f
        val dy = (viewHeight - scaledViewH) / 2f

        transform.postScale(scaleToView, scaleToView)
        transform.postTranslate(dx, dy)

        // Optional mirroring for front-facing camera
        if (isFrontCamera) {
            transform.postScale(-1f, 1f, viewWidth / 2f, viewHeight / 2f)
        }

        // --- Step 4: Map detections to screen coordinates ---
        val cxArr = det[IDX_CX]
        val cyArr = det[IDX_CY]

        val mappedDetections = mutableListOf<Detection>()
        val temp = FloatArray(2)

        for (i in cxArr.indices) {
            // Select highest confidence among all classes
            var bestScore = Float.NEGATIVE_INFINITY
            for (c in 0 until NUM_CLASSES) {
                val score = det[CLASS_START + c][i]
                if (score > bestScore) bestScore = score
            }
            if (bestScore < CONFIDENCE_THRESHOLD) continue

            // Convert normalized model coordinates → pixel coordinates
            temp[0] = cxArr[i] * INPUT_SIZE
            temp[1] = cyArr[i] * INPUT_SIZE

            // Apply combined transformation matrix
            transform.mapPoints(temp)

            val px = temp[0].coerceIn(0f, viewWidth.toFloat())
            val py = temp[1].coerceIn(0f, viewHeight.toFloat())

            val r = 10f
            val box = RectF(px - r, py - r, px + r, py + r)

            mappedDetections += Detection(box, bestScore, px, py)
        }

        // --- Step 5: Non-Maximum Suppression ---
        val filtered = nonMaxSuppression(mappedDetections)
        val elapsed = System.currentTimeMillis() - startTime

        logger.i(
            "Postprocessing completed | DetectionsIn=${mappedDetections.size}, " +
                    "DetectionsOut=${filtered.size}, Elapsed=${elapsed}ms"
        )

        return filtered to transform
    }*/

    fun parseDetections(
        det: Array<FloatArray>,  // shape [5][8400]
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int,
        viewWidth: Int,
        viewHeight: Int,
        isFrontCamera: Boolean = false
    ): Pair<List<Detection>, Matrix> {

        val startTime = System.currentTimeMillis()

        val srcW = imageWidth.toFloat()
        val srcH = imageHeight.toFloat()

        val scaleModel = max(INPUT_SIZE / srcW, INPUT_SIZE / srcH)
        val scaledW = srcW * scaleModel
        val scaledH = srcH * scaleModel
        val xOffset = (scaledW - INPUT_SIZE) / 2f
        val yOffset = (scaledH - INPUT_SIZE) / 2f

        val transform = Matrix().apply {
            postTranslate(xOffset, yOffset)
            postScale(1f / scaleModel, 1f / scaleModel)
        }

        val scaleToView = max(viewWidth / srcW, viewHeight / srcH)
        val scaledViewW = srcW * scaleToView
        val scaledViewH = srcH * scaleToView
        val dx = (viewWidth - scaledViewW) / 2f
        val dy = (viewHeight - scaledViewH) / 2f

        transform.postScale(scaleToView, scaleToView)
        transform.postTranslate(dx, dy)

        if (isFrontCamera) {
            transform.postScale(-1f, 1f, viewWidth / 2f, viewHeight / 2f)
        }

        // Model gives only [x, y, w, h, conf]
        val cxArr = det[0]
        val cyArr = det[1]
        val wArr = det[2]
        val hArr = det[3]
        val confArr = det[4]

        val mappedDetections = mutableListOf<Detection>()
        val temp = FloatArray(2)

        for (i in cxArr.indices) {
            val conf = confArr[i]
            if (conf < CONFIDENCE_THRESHOLD) continue

            temp[0] = cxArr[i] * INPUT_SIZE
            temp[1] = cyArr[i] * INPUT_SIZE
            transform.mapPoints(temp)

            val px = temp[0].coerceIn(0f, viewWidth.toFloat())
            val py = temp[1].coerceIn(0f, viewHeight.toFloat())

            val r = (wArr[i].coerceAtLeast(hArr[i]) * INPUT_SIZE / 2f)
            val box = RectF(px - r, py - r, px + r, py + r)

            mappedDetections += Detection(box, conf, px, py)
        }

        val filtered = nonMaxSuppression(mappedDetections)
        val elapsed = System.currentTimeMillis() - startTime

        logger.i("Postprocessing completed | DetectionsIn=${mappedDetections.size}, DetectionsOut=${filtered.size}, Elapsed=${elapsed}ms")

        return filtered to transform
    }


    /**
     * Performs Non-Maximum Suppression (NMS) to eliminate redundant overlapping detections.
     */
    private fun nonMaxSuppression(
        detections: List<Detection>,
        iouThreshold: Float = IOU_THRESHOLD
    ): List<Detection> {
        if (detections.isEmpty()) return emptyList()
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val result = mutableListOf<Detection>()

        while (sorted.isNotEmpty()) {
            val top = sorted.removeAt(0)
            result += top
            sorted.removeAll { iou(top.boundingBox, it.boundingBox) > iouThreshold }
        }
        return result
    }

    /**
     * Computes Intersection-over-Union (IoU) between two rectangles.
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
        val union = areaA + areaB - interArea
        return if (union <= 0f) 0f else interArea / union
    }
}
