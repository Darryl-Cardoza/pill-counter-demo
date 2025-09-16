package com.example.pillcountingnewmodels.feature.settings.data.repository

import com.example.pillcountingnewmodels.core.models.ApplicationSettingsResponse
import com.example.pillcountingnewmodels.core.network.IApplicationSettingInterface
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.settings.domain.repository.IApplicationSettingsRepository
import javax.inject.Inject

/**
 * Concrete implementation of the [ApplicationSettingsRepository] interface.
 * It uses a Retrofit service to fetch settings from the network.
 *
 * @param apiService The Retrofit service, injected by Hilt.
 * @param prefs The shared preference helper, injected by Hilt.
 */
class ApplicationSettingsRepository @Inject constructor(
    private val apiService: IApplicationSettingInterface,
    private val prefs: PreferenceHelper
) : IApplicationSettingsRepository {

    private val logger = AppLogger.create<ApplicationSettingsRepository>()

    /**
     * Fetches application settings by delegating the call to the injected [apiService].
     * Automatically attaches the saved access token as a Bearer token.
     */
    override suspend fun getApplicationSettings(): ApplicationSettingsResponse {
//        val token = prefs.getAccessToken()
//            ?: throw IllegalStateException("Access token not found. User may not be logged in.")

//        logger.i("Fetching application settings with token: Bearer $token")

        val response = apiService.getApplicationSettings(
//            authHeader = "Bearer $token"
        )

        logger.d("Application settings response: $response")

        return response
    }
}
