package com.example.pillcountingnewmodels.core.models

import androidx.compose.runtime.Immutable
import com.example.pillcountingnewmodels.feature.settings.data.model.ThemeColors

/**
 * Contains the theme colors for both light and dark modes.
 */
@Immutable
data class ColorSettings(
    val light: ThemeColors,
    val dark: ThemeColors
)