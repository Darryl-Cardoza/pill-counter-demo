package com.example.pillcountingnewmodels.core.models

/**
 * Represents the UI state for the Application Settings screen.
 *
 * This state is observed by the UI to react to data changes such as loading status,
 * error messages, and theming settings.
 *
 * @property isLoading Indicates whether the settings are currently being loaded.
 * @property errorMessage A nullable error message to display in the UI if loading fails.
 * @property colorSettings The color settings for light and dark themes.
 * @property appLogoUrl The URL or identifier for the application logo to be displayed.
 */
data class ApplicationSettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val colorSettings: ColorSettings? = null,
    val appLogoUrl: String? = null,
)
