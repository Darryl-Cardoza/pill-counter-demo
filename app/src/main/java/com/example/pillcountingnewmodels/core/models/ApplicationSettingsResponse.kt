package com.example.pillcountingnewmodels.core.models

import com.squareup.moshi.JsonClass

/**
 * Represents the entire settings response from the API.
 */
@JsonClass(generateAdapter = true)
data class ApplicationSettingsResponse(
    val colors: ColorSettings,
    val appLogo: String,
    val placeholderLogo: String
)