package com.rite.pillcounting.core.settings.data

import android.content.Context
import com.rite.pillcounting.core.models.ApiResponse
import com.rite.pillcounting.core.settings.data.remote.IApplicationSettingInterface
import com.rite.pillcounting.core.settings.domain.data.IApplicationSettingsRepository
import com.rite.pillcounting.core.settings.domain.model.SettingsDataDto
import com.rite.pillcounting.core.utils.common.HelperFunctions
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
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
    private val preferenceHelper: PreferenceHelper,
    private val context: Context
) : IApplicationSettingsRepository {

    private val logger = AppLogger.Companion.create<ApplicationSettingsRepository>()

    override suspend fun getApplicationSettings(): ApiResponse<SettingsDataDto> {
        try {
            val version = HelperFunctions.getAppVersionName(context)
            logger.d("Fetching app settings for version: $version")

            return apiService.getApplicationSettings(androidVersion = version)
        } catch (e: HttpException) {
            logger.e("Http error when fetching settings", e)
            throw e
        } catch (e: Exception) {
            logger.e("Unexpected error when fetching settings", e)
            throw e
        }
    }
}