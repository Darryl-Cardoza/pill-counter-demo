package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.RectF

object Postprocessor {

    fun decode(
        coords: Array<FloatArray>,
        conf: Array<FloatArray>,
        confThreshold: Float,
        scale: Float,
        padX: Float,
        padY: Float
    ): List<Detection> {

        val results = mutableListOf<Detection>()

        for (i in coords.indices) {
            val score = conf[i][0]
            if (score < confThreshold) continue

            // MODEL SPACE (640)
            val cx = coords[i][0] * 640f
            val cy = coords[i][1] * 640f
            val w = coords[i][2] * 640f
            val h = coords[i][3] * 640f

            var x1 = cx - w / 2f
            var y1 = cy - h / 2f
            var x2 = cx + w / 2f
            var y2 = cy + h / 2f

            // REVERSE LETTERBOX
            x1 = (x1 - padX) / scale
            y1 = (y1 - padY) / scale
            x2 = (x2 - padX) / scale
            y2 = (y2 - padY) / scale

            val rect = RectF(x1, y1, x2, y2)

            results.add(
                Detection(
                    rect = rect,
                    confidence = score
                )
            )
        }

        return results
    }
}
