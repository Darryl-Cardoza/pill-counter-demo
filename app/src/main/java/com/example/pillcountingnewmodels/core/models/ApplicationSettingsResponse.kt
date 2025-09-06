package com.example.pillcountingnewmodels.core.models

import androidx.compose.runtime.Immutable

/**
 * Represents the entire settings response from the API.
 */
@Immutable
data class ApplicationSettingsResponse(
    val colors: ColorSettings,
    val appLogo: String,
    val placeholderLogo: String
)