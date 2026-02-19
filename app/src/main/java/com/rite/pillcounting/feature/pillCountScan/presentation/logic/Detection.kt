package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.RectF

data class Detection(
    val rect: RectF,
    val confidence: Float
)