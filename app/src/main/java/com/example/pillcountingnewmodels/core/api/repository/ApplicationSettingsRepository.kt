package com.example.pillcountingnewmodels.core.api.repository

import com.example.pillcountingnewmodels.core.api.implementation.IApplicationSettingsRepository
import com.example.pillcountingnewmodels.core.api.`interface`.IApplicationSettingInterface
import com.example.pillcountingnewmodels.core.models.ApiResponse
import com.example.pillcountingnewmodels.core.models.RefreshTokenRequest
import com.example.pillcountingnewmodels.core.models.SettingsDataDto
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Implementation of [IApplicationSettingsRepository] that retrieves settings via network.
 *
 * @property apiService Retrofit service for settings and auth.
 * @property preferenceHelper Manages local access and refresh tokens.
 */
class ApplicationSettingsRepository @Inject constructor(
    private val apiService: IApplicationSettingInterface,
    private val preferenceHelper: PreferenceHelper
) : IApplicationSettingsRepository {

    private val logger = AppLogger.create<ApplicationSettingsRepository>()

    override suspend fun getApplicationSettings(): ApiResponse<SettingsDataDto> {
        try {
            logger.d("Fetching application settings")
            return apiService.getApplicationSettings()
        } catch (e: HttpException) {
            // Handle unauthorized error (token expired)
            if (e.code() == 401) {
                logger.w("Access token expired. Attempting to refresh token...")
                val refreshToken = preferenceHelper.getRefreshToken()
                    ?: throw Exception("No refresh token available")

                val refreshResponse = apiService.refreshToken(
                    RefreshTokenRequest(refreshToken = refreshToken)
                )

                if (refreshResponse.accessToken != null) {
                    logger.i("Token refreshed successfully. Retrying settings fetch.")

                    refreshResponse.refreshToken?.let {
                        preferenceHelper.saveTokens(
                            accessToken = refreshResponse.accessToken,
                            refreshToken = refreshResponse.refreshToken
                        )
                    }

                    // Retry the original request with the new token
                    return apiService.getApplicationSettings()
                } else {
                    throw Exception("Failed to refresh token: ${refreshResponse.message}")
                }
            } else {
                logger.e("Http error when fetching settings", e)
                throw e
            }
        } catch (e: Exception) {
            logger.e("Unexpected error when fetching settings", e)
            throw e
        }
    }
}
