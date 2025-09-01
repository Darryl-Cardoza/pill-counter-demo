package com.example.pillcountingnewmodels.core.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun String.toColor(): Color = Color(this.toColorInt())