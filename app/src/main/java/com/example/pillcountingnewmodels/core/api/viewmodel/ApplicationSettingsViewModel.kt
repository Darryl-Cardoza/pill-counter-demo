package com.example.pillcountingnewmodels.core.api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.models.ApiResponse
import com.example.pillcountingnewmodels.core.models.ApplicationSettingsUiState
import com.example.pillcountingnewmodels.core.models.ColorSettings
import com.example.pillcountingnewmodels.core.models.SettingsDataDto
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
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
 * Responsibilities:
 * - Loads cached theme from [PreferenceHelper] immediately for fast startup.
 * - Fetches remote settings in the background and updates both UI + cache.
 * - Determines if app should show maintenance/update screen based on remote flags.
 * - Falls back to hardcoded defaults if neither cache nor remote settings are available.
 *
 * This ensures the user never waits on a blocking loading screen for theme data.
 */
@HiltViewModel
class ApplicationSettingsViewModel @Inject constructor(
    private val repository: IApplicationSettingsRepository,
    private val preferenceHelper: PreferenceHelper
) : ViewModel(), IApplicationSettingsViewModel {

    private val logger = AppLogger.create<ApplicationSettingsViewModel>()

    private val _uiState = MutableStateFlow(ApplicationSettingsUiState())
    override val uiState = _uiState.asStateFlow()

    init {
        // Load cached/fallback theme instantly
        loadCachedOrFallbackTheme()

        // Start fetching remote theme in background
        fetchApplicationSettings()
    }

    /**
     * Loads cached theme colors if available, else applies fallback defaults.
     * This is called synchronously on init to avoid UI blocking.
     */
    private fun loadCachedOrFallbackTheme() {
        val cached = preferenceHelper.getThemeColors()
        if (cached != null) {
            logger.i("Loaded cached theme from preferences.")
            _uiState.update { it.copy(colorSettings = cached) }
        } else {
            logger.w("No cached theme found, applying fallback.")
            applyFallbackSettings()
        }
    }

    /**
     * Initiates a remote fetch for application settings in the background.
     * Updates the cache and UI if successful, otherwise retains cached/fallback values.
     */
    override fun fetchApplicationSettings() {
        viewModelScope.launch {
            try {
                logger.i("Fetching remote application settings...")
                val response = repository.getApplicationSettings()
                applyAndStoreSettings(response)
            } catch (e: Exception) {
                logger.e("Failed to fetch settings. Keeping cached/fallback values.", e)
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                logger.d("Settings fetch process finished.")
            }
        }
    }

    /**
     * Applies new remote settings and updates cache + UI.
     */
    private fun applyAndStoreSettings(settings: ApiResponse<SettingsDataDto>) {
        logger.i("Successfully fetched remote settings.")

        val dto = settings.data
        val theme = dto?.settings?.colors

        _uiState.update {
            it.copy(
                colorSettings = theme ?: it.colorSettings, // keep existing if null
                appLogoUrl = dto?.settings?.appLogo ?: it.appLogoUrl,
                appSettings = dto,
                isMaintenanceMode = dto?.isMaintenanceMode ?: false,
                isUpdateRequired = isUpdateRequired(dto?.version)
            )
        }

        theme?.let {
            preferenceHelper.saveThemeColors(it) // persist for next launch
            logger.i("Updated cached theme colors in preferences.")
        }
    }

    /**
     * Applies hardcoded fallback color settings if cache + remote both fail.
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
                appLogoUrl = "default_logo_placeholder",
                isMaintenanceMode = false,
                isUpdateRequired = false
            )
        }
    }

    /**
     * Checks if update is required based on remote version vs current app version.
     */
    private fun isUpdateRequired(remoteVersion: String?): Boolean {
        if (remoteVersion.isNullOrBlank()) return false
        return try {
            val current = getCurrentAppVersion()
            compareVersions(remoteVersion, current) > 0
        } catch (e: Exception) {
            false
        }
    }

    /** Gets current app versionName from PackageManager. */
    private fun getCurrentAppVersion(): String {
        return try {
            val ctx = preferenceHelper.getContext()
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "0.0.0"
        } catch (_: Exception) {
            "0.0.0"
        }
    }

    /**
     * Compare two semantic version strings.
     * @return >0 if v1 > v2, <0 if v1 < v2, 0 if equal.
     */
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".")
        val parts2 = v2.split(".")
        val maxLength = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLength) {
            val num1 = parts1.getOrNull(i)?.toIntOrNull() ?: 0
            val num2 = parts2.getOrNull(i)?.toIntOrNull() ?: 0
            if (num1 != num2) return num1 - num2
        }
        return 0
    }
}
