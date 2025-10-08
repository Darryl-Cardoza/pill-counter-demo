package com.example.pillcountingnewmodels.core.api.interfaceDetail

import com.example.pillcountingnewmodels.core.models.ApiResponse
import com.example.pillcountingnewmodels.core.models.RefreshTokenRequest
import com.example.pillcountingnewmodels.core.models.RefreshTokenResponse
import com.example.pillcountingnewmodels.core.models.SettingsDataDto
import com.example.pillcountingnewmodels.core.utils.URLConstant
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
    suspend fun refreshToken( //TODO(Shift to the user detail repo)
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse

}
