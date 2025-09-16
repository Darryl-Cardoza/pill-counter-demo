package com.example.pillcountingnewmodels.core.models

import com.example.pillcountingnewmodels.feature.settings.data.model.ThemeColors
import com.squareup.moshi.JsonClass

/**
 * Contains the theme colors for both light and dark modes.
 */
@JsonClass(generateAdapter = true)
data class ColorSettings(
    val light: ThemeColors,
    val dark: ThemeColors
)