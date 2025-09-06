package com.example.pillcountingnewmodels.feature.settings.data.repository

import com.example.pillcountingnewmodels.core.models.ApplicationSettingsResponse
import com.example.pillcountingnewmodels.core.network.IApplicationSettingInterface
import com.example.pillcountingnewmodels.feature.settings.domain.repository.IApplicationSettingsRepository
import javax.inject.Inject

/**
 * Concrete implementation of the [ApplicationSettingsRepository] interface.
 * It uses a Retrofit service to fetch settings from the network.
 *
 * @param apiService The Retrofit service, injected by Hilt.
 */
class ApplicationSettingsRepository @Inject constructor(
    private val apiService: IApplicationSettingInterface
) : IApplicationSettingsRepository {

    /**
     * Fetches application settings by delegating the call to the injected [apiService].
     */
    override suspend fun getApplicationSettings(): ApplicationSettingsResponse {
        return apiService.getApplicationSettings()
    }
}

