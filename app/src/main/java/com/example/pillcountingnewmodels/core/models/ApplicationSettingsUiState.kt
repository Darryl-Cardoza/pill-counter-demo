package com.example.pillcountingnewmodels.core.models


/**
 * Data class representing the state of the Application Settings UI.
 */
data class ApplicationSettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val colorSettings: ColorSettings? = null,
    val appLogoUrl: String? = null,
)