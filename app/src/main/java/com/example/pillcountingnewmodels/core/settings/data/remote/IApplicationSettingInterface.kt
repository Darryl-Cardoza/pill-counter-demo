package com.example.pillcountingnewmodels.core.settings.data.remote

import com.example.pillcountingnewmodels.core.models.ApiResponse
import com.example.pillcountingnewmodels.core.refreshToken.domain.model.RefreshTokenRequest
import com.example.pillcountingnewmodels.core.refreshToken.domain.model.RefreshTokenResponse
import com.example.pillcountingnewmodels.core.settings.domain.model.SettingsDataDto
import com.example.pillcountingnewmodels.core.utils.constants.URLConstant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Defines the network endpoints for the application using Retrofit.
 */
interface IApplicationSettingInterface {

    /**
     * Fetches the application settings from the remote server.
     * Pass Authorization header dynamically from SharedPreferences.
     */
    @GET(URLConstant.MOBILE_SETTINGS)
    suspend fun getApplicationSettings(): ApiResponse<SettingsDataDto>

    /**
     * Refreshes access token using a valid refresh token.
     */
    @POST(URLConstant.REFRESH_TOKEN)
    @Headers("Content-Type: ${URLConstant.CONTENT_TYPE}")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse

}