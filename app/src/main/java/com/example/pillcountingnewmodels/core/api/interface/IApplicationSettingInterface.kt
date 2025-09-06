package com.example.pillcountingnewmodels.core.network

import com.example.pillcountingnewmodels.core.models.ApplicationSettingsResponse
import retrofit2.http.GET

/**
 * Defines the network endpoints for the application using Retrofit.
 */
interface IApplicationSettingInterface {

    /**
     * Fetches the application settings from the remote server.
     * This is a suspend function, designed to be called from a coroutine.
     */
    @GET("v1/settings") //TODO(Replace with actual API endpoint path)
    suspend fun getApplicationSettings(): ApplicationSettingsResponse
}
