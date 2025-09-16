package com.example.pillcountingnewmodels.feature.settings.data.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

/**
 * Defines a complete and extended color palette for a theme.
 * This class holds all custom colors used throughout the application, ensuring
 * a single source of truth for both light and dark modes.
 */
@JsonClass(generateAdapter = true)
data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val primaryBackground: Color,
    val secondaryBackground: Color,
    val textColor: Color,
    val inputBackground: Color,
    val statusChipBackgroundOnPrimary: Color,
    val statusChipBackgroundOnSecondary: Color
)

