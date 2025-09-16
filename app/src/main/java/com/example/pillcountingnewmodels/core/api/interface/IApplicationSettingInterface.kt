package com.example.pillcountingnewmodels.core.network

import com.example.pillcountingnewmodels.core.models.ApplicationSettingsResponse
import com.example.pillcountingnewmodels.core.utils.URLConstant
import retrofit2.http.GET

/**
 * Defines the network endpoints for the application using Retrofit.
 */
interface IApplicationSettingInterface {

    /**
     * Fetches the application settings from the remote server.
     * Pass Authorization header dynamically from SharedPreferences.
     */
    @GET(URLConstant.MOBILE_SETTINGS)
    suspend fun getApplicationSettings(
//        @Header("Authorization") authHeader: String,
//        @Header("accept") accept: String = URLConstant.CONTENT_TYPE,
    ): ApplicationSettingsResponse
}
