package com.example.pillcountingnewmodels.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.models.ApplicationSettingsResponse
import com.example.pillcountingnewmodels.core.models.ApplicationSettingsUiState
import com.example.pillcountingnewmodels.core.models.ColorSettings
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.toColor // Assuming the extension function is here
import com.example.pillcountingnewmodels.feature.settings.data.model.ThemeColors
import com.example.pillcountingnewmodels.feature.settings.domain.repository.IApplicationSettingsRepository
import com.example.pillcountingnewmodels.feature.settings.domain.viewmodel.IApplicationSettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
     * Fetches application settings from the repository and updates the UI state accordingly.
     * Handles both success and failure cases, applying a fallback on error.
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
     * Applies and stores settings from a successful API response.
     */
    private fun applyAndStoreSettings(settings: ApplicationSettingsResponse) {
        logger.i("Successfully fetched and applied remote settings.")
        _uiState.update {
            it.copy(
                colorSettings = settings.colors,
                appLogoUrl = settings.appLogo
            )
        }
        // Here you would also save these settings to a local preferences service.
    }

    /**
     * Applies and stores hardcoded fallback settings.
     */
    private fun applyFallbackSettings() {
        logger.w("Applying hardcoded fallback settings.")

        // Define fallback colors as hex strings
        val primaryColorLight = "#01BBD3"
        val secondaryColorLight = "#FD82B5"
        val tertiaryColorLight = "#333333"
        val primaryBackgroundLight = "#EDEEEE"
        val secondaryBackgroundLight = "#FFFFFF"
        val textColorLight = "#666666"
        val inputBackgroundLight = "#FFFFFF"
        val statusChipBackgroundOnPrimaryLight = "#FFFFFF"
        val statusChipBackgroundOnSecondaryLight = "#F5F4F4"

        val primaryColorDark = "#01BBD3"
        val secondaryColorDark = "#FD82B5"
        val tertiaryColorDark = "#FFFFFF"
        val primaryBackgroundDark = "#333333"
        val secondaryBackgroundDark = "#191919"
        val textColorDark = "#EDEEEE"
        val inputBackgroundDark = "#191919"
        val statusChipBackgroundOnPrimaryDark = "#191919"
        val statusChipBackgroundOnSecondaryDark = "#333333"

        val fallbackColors = ColorSettings(
            light = ThemeColors(
                primary = primaryColorLight.toColor(),
                secondary = secondaryColorLight.toColor(),
                tertiary = tertiaryColorLight.toColor(),
                primaryBackground = primaryBackgroundLight.toColor(),
                secondaryBackground = secondaryBackgroundLight.toColor(),
                textColor = textColorLight.toColor(),
                inputBackground = inputBackgroundLight.toColor(),
                statusChipBackgroundOnPrimary = statusChipBackgroundOnPrimaryLight.toColor(),
                statusChipBackgroundOnSecondary = statusChipBackgroundOnSecondaryLight.toColor()
            ),
            dark = ThemeColors(
                primary = primaryColorDark.toColor(),
                secondary = secondaryColorDark.toColor(),
                tertiary = tertiaryColorDark.toColor(),
                primaryBackground = primaryBackgroundDark.toColor(),
                secondaryBackground = secondaryBackgroundDark.toColor(),
                textColor = textColorDark.toColor(),
                inputBackground = inputBackgroundDark.toColor(),
                statusChipBackgroundOnPrimary = statusChipBackgroundOnPrimaryDark.toColor(),
                statusChipBackgroundOnSecondary = statusChipBackgroundOnSecondaryDark.toColor()
            )
        )

        _uiState.update {
            it.copy(
                colorSettings = fallbackColors,
                appLogoUrl = "default_logo_placeholder" // A local drawable name
            )
        }
        // Also save fallback to preferences.
    }
}