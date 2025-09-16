package com.example.pillcountingnewmodels.core.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object (DTO) representing the full set of application settings
 * received from the backend API.
 *
 * This model encapsulates metadata like versioning and feature toggles
 * (e.g., maintenance mode), along with the actual theming and branding settings.
 *
 * @property version The version of the settings schema or configuration from the server.
 * @property isMaintenanceMode Flag indicating whether the app should operate in maintenance mode.
 * @property settings The actual application settings payload (e.g., theme colors, branding assets).
 */
@JsonClass(generateAdapter = true)
data class SettingsDataDto(
    @Json(name = "version") val version: String,
    @Json(name = "is_maintenance_mode") val isMaintenanceMode: Boolean,
    @Json(name = "settings") val settings: ApplicationSettingsResponse
)
