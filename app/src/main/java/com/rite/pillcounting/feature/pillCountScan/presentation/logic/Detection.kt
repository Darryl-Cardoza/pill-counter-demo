package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.RectF

data class Detection(
    val rect: RectF,
    val confidence: Float
) {
    val centerX: Float get() = rect.centerX()
    val centerY: Float get() = rect.centerY()
}
