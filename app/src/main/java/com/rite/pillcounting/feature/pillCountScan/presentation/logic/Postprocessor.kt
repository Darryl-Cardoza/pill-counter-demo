package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Matrix
import android.graphics.RectF
import com.rite.pillcounting.core.utils.logger.AppLogger
import kotlin.math.max
import kotlin.math.min

object Postprocessor {

    private const val INPUT_SIZE = 640
    private const val CONF_THRESHOLD = 0.70f
    private val logger = AppLogger.create<Postprocessor>()

    data class Detection(
        val boundingBox: RectF,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    /**
     * POSTPROCESS PIPELINE
     *
     * model(640) → remove padding → unscale → preview space
     */
    fun parseDetections(
        det: Array<FloatArray>,      // [x,y,w,h,conf]
        letterbox: Preprocessor.LetterboxInfo,
        viewWidth: Int,
        viewHeight: Int,
        isFrontCamera: Boolean = false
    ): Pair<List<Detection>, Matrix> {

        val transform = Matrix()

        // Source → Preview (CENTER_CROP like PreviewView)
        val scaleToView = max(
            viewWidth / letterbox.srcWidth.toFloat(),
            viewHeight / letterbox.srcHeight.toFloat()
        )

        val dx = (viewWidth - letterbox.srcWidth * scaleToView) / 2f
        val dy = (viewHeight - letterbox.srcHeight * scaleToView) / 2f

        transform.postScale(scaleToView, scaleToView)
        transform.postTranslate(dx, dy)

        if (isFrontCamera) {
            transform.postScale(-1f, 1f, viewWidth / 2f, viewHeight / 2f)
        }

        val results = mutableListOf<Detection>()
        val tmp = FloatArray(2)

        for (i in det[0].indices) {
            val conf = det[4][i]
            if (conf < CONF_THRESHOLD) continue

            // Model → source image
            val xModel = det[0][i] * INPUT_SIZE
            val yModel = det[1][i] * INPUT_SIZE

            val xSrc =
                (xModel - letterbox.padX) / letterbox.scale
            val ySrc =
                (yModel - letterbox.padY) / letterbox.scale

            tmp[0] = xSrc
            tmp[1] = ySrc
            transform.mapPoints(tmp)

            val px = tmp[0].coerceIn(0f, viewWidth.toFloat())
            val py = tmp[1].coerceIn(0f, viewHeight.toFloat())

            val r =
                max(det[2][i], det[3][i]) *
                        INPUT_SIZE / (2f * letterbox.scale)

            results += Detection(
                boundingBox = RectF(px - r, py - r, px + r, py + r),
                confidence = conf,
                pixelX = px,
                pixelY = py
            )
        }

        logger.i("Postprocess | detections=${results.size}")
        return results to transform
    }
}
