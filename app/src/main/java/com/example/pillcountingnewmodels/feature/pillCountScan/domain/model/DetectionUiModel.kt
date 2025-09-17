package com.example.pillcountingnewmodels.feature.pillCountScan.domain.model

import androidx.compose.ui.graphics.Color

data class DetectionUiModel(
    val x: Float,            // relative X (0f..1f)
    val y: Float,            // relative Y (0f..1f)
    val radius: Float = 10f, // default circle radius in pixels
    val color: Color = Color.Red // default color
)
