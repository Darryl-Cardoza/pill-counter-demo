package com.rite.pillcounting.feature.dashboard.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents user preferences/settings.
 */
@JsonClass(generateAdapter = true)
data class UserSettings(
    @Json(name = "notifications_enabled") val notificationsEnabled: Boolean? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "timezone") val timezone: String? = null
)