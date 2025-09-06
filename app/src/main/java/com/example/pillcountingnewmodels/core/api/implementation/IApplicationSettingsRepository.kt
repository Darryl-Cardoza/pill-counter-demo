package com.example.pillcountingnewmodels.feature.settings.domain.repository

import com.example.pillcountingnewmodels.core.models.ApplicationSettingsResponse

/**
 * Defines the contract for the Application Settings repository.
 * This abstraction allows for interchangeable data sources (e.g., network, local database)
 * and is crucial for unit testing the components that use it.
 */
interface IApplicationSettingsRepository {

    /**
     * Retrieves the application settings.
     *
     * @return An [ApplicationSettingsResponse] object containing the settings.
     * @throws Exception if the data fetching fails (e.g., network error, parsing error).
     */
    suspend fun getApplicationSettings(): ApplicationSettingsResponse
}

