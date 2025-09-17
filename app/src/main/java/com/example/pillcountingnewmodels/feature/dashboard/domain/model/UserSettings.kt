package com.example.pillcountingnewmodels.feature.dashboard.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents user preferences/settings.
 */
@JsonClass(generateAdapter = true)
data class UserSettings(
    @Json(name = "notifications_enabled") val notificationsEnabled: Boolean? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "theme") val theme: String? = null,
    @Json(name = "font_size") val fontSize: String? = null,
    @Json(name = "experimental_features") val experimentalFeatures: List<String> = emptyList()
)