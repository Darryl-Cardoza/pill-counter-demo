package com.rite.pillcounting.core.settings.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rite.pillcounting.core.models.ApiResponse
import com.rite.pillcounting.core.room.dao.PillCountTxnDao
import com.rite.pillcounting.core.settings.domain.data.IApplicationSettingsRepository
import com.rite.pillcounting.core.settings.domain.data.IApplicationSettingsViewModel
import com.rite.pillcounting.core.settings.domain.model.ApplicationSettingsUiState
import com.rite.pillcounting.core.settings.domain.model.ColorSettings
import com.rite.pillcounting.core.settings.domain.model.SettingsDataDto
import com.rite.pillcounting.core.settings.domain.model.ThemeColors
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
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
class MainActivityViewModel @Inject constructor(
    private val repository: IApplicationSettingsRepository,
    private val preferenceHelper: PreferenceHelper,
    private val txnDao: PillCountTxnDao
) : ViewModel(), IApplicationSettingsViewModel {

    private val logger = AppLogger.Companion.create<MainActivityViewModel>()

    // Holds the current state of "Ask to Add Notes"
    private val _isAskToAddNotes = MutableStateFlow(preferenceHelper.getShowNotesDialogSetting())
    val isAskToAddNotes: StateFlow<Boolean> = _isAskToAddNotes

    private val _selectedHistoryOption = MutableStateFlow(preferenceHelper.getHistoryRetention())
    val selectedHistoryOption: StateFlow<Int> = _selectedHistoryOption

    // Called when user toggles the switch
    fun toggleAskToAddNotes(newValue: Boolean) {
        _isAskToAddNotes.value = newValue
        preferenceHelper.saveShowNotesDialogSetting(newValue)
    }

    private val _uiState = MutableStateFlow(ApplicationSettingsUiState())
    override val uiState = _uiState.asStateFlow()

    init {
        // Load cached/fallback theme instantly
        loadCachedOrFallbackTheme()

        // Start fetching remote theme in background
        fetchApplicationSettings()

        viewModelScope.launch(Dispatchers.IO) {
            deleteOldTransactions()
        }
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
                isUpdateRequired = isUpdateRequired(dto?.minVersion)
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
        } catch (_: Exception) {
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

    fun updateHistoryOption(optionDays: Int) {
        _selectedHistoryOption.value = optionDays
        preferenceHelper.saveHistoryRetention(optionDays)

        viewModelScope.launch(Dispatchers.IO) {
            deleteOldTransactions()
        }
    }

    private suspend fun deleteOldTransactions() {
        val optionDays = preferenceHelper.getHistoryRetention()

        try {
            // Compute cutoff in UTC to match DB timestamps

            //TODO(Move this to the dateUtils)
            val nowUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val cutoff = nowUtc.timeInMillis - optionDays * 24 * 60 * 60 * 1000L

            //TODO(Change to the logger)
            Log.d("DELETE_TXN", "Retention days: $optionDays, cutoff=${Date(cutoff)}")

            // Get all transactions older than cutoff regardless of isDeleted
            val oldTransactions = txnDao.getTransactionsBefore(cutoff)
            Log.d("DELETE_TXN", "Found ${oldTransactions.size} transactions to delete")

            oldTransactions.forEach { txn ->
                val filesToDelete = mutableListOf<String>()

                // Collect barcode image
                txn.barcodeImage?.let { filesToDelete.add(it) }

                // Collect details images
                val detailImages = txnDao.getTransactionDetailsImages(txn.txnId)
                filesToDelete.addAll(detailImages)

                // Delete transaction (assumes cascade deletes details)
                txnDao.deleteTransaction(txn.txnId)

                // Delete files from storage
                filesToDelete.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d("DELETE_TXN", "Deleted file: $path")
                        } else {
                            Log.w("DELETE_TXN", "Failed to delete file: $path")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("DELETE_TXN", "Error deleting old transactions", e)
        }
    }



}