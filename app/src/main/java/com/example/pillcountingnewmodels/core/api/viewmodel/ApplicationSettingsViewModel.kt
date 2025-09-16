package com.example.pillcountingnewmodels.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.models.ApiResponse
import com.example.pillcountingnewmodels.core.models.ApplicationSettingsUiState
import com.example.pillcountingnewmodels.core.models.ColorSettings
import com.example.pillcountingnewmodels.core.models.SettingsDataDto
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.settings.data.model.ThemeColors
import com.example.pillcountingnewmodels.feature.settings.domain.repository.IApplicationSettingsRepository
import com.example.pillcountingnewmodels.feature.settings.domain.viewmodel.IApplicationSettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing and exposing application settings to the UI layer.
 *
 * This ViewModel:
 * - Fetches remote settings from a repository.
 * - Applies fetched settings to the UI state.
 * - Applies fallback settings in case of error.
 * - Provides a read-only [uiState] for observing in the UI.
 *
 * This ViewModel is lifecycle-aware and scoped to the Hilt lifecycle via [@HiltViewModel].
 *
 * @property repository Interface to fetch remote settings.
 */
@HiltViewModel
class ApplicationSettingsViewModel @Inject constructor(
    private val repository: IApplicationSettingsRepository
) : ViewModel(), IApplicationSettingsViewModel {

    private val logger = AppLogger.create<ApplicationSettingsViewModel>()

    private val _uiState = MutableStateFlow(ApplicationSettingsUiState())
    override val uiState = _uiState.asStateFlow()

    init {
        fetchApplicationSettings()
    }

    /**
     * Initiates the process of fetching application settings.
     *
     * Handles both success and failure cases:
     * - On success, settings are applied and stored.
     * - On failure, fallback settings are applied and an error is logged.
     */
    override fun fetchApplicationSettings() {
        logger.i("Fetching application settings...")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val settings = repository.getApplicationSettings()
                applyAndStoreSettings(settings)
            } catch (e: Exception) {
                logger.e("Failed to fetch settings, applying fallback.", e)
                val errorDescription = "Failed to fetch settings: ${e.message}. Applying fallback."
                _uiState.update { it.copy(errorMessage = errorDescription) }
                applyFallbackSettings()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
                logger.d("Settings fetch process finished.")
            }
        }
    }

    /**
     * Applies and updates the UI state with the settings retrieved from a successful API response.
     *
     * @param settings The API response containing application settings data.
     */
    private fun applyAndStoreSettings(settings: ApiResponse<SettingsDataDto>) {
        logger.i("Successfully fetched and applied remote settings.")

        _uiState.update {
            it.copy(
                colorSettings = settings.data?.settings?.colors,
                appLogoUrl = settings.data?.settings?.appLogo
            )
        }

        // TODO: Persist the settings locally if required (e.g., SharedPreferences or DataStore).
    }

    /**
     * Applies a hardcoded set of fallback color settings and updates the UI state.
     *
     * This is used in cases where fetching settings from the remote source fails.
     */
    private fun applyFallbackSettings() {
        logger.w("Applying hardcoded fallback settings.")

        val fallbackColors = ColorSettings(
            light = ThemeColors(
                primary = "#01BBD3",
                secondary = "#FD82B5",
                tertiary = "#333333",
                primaryBackground = "#EDEEEE",
                secondaryBackground = "#FFFFFF",
                textColor = "#666666",
                inputBackground = "#FFFFFF",
                statusChipBackgroundOnPrimary = "#FFFFFF",
                statusChipBackgroundOnSecondary = "#F5F4F4"
            ),
            dark = ThemeColors(
                primary = "#01BBD3",
                secondary = "#FD82B5",
                tertiary = "#FFFFFF",
                primaryBackground = "#333333",
                secondaryBackground = "#191919",
                textColor = "#EDEEEE",
                inputBackground = "#191919",
                statusChipBackgroundOnPrimary = "#191919",
                statusChipBackgroundOnSecondary = "#333333"
            )
        )

        _uiState.update {
            it.copy(
                colorSettings = fallbackColors,
                appLogoUrl = "default_logo_placeholder"
            )
        }
    }
}
