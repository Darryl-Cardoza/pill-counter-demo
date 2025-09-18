package com.example.pillcountingnewmodels.feature.profile.data

import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.profile.data.remote.IProfileApi
import com.example.pillcountingnewmodels.feature.profile.domain.data.IProfileRepository
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileDeleteResponse
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateRequest
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper

/**
 * Default implementation of [IProfileRepository].
 *
 * Interacts with remote [IProfileApi] and local Room database [UserDao].
 */
class ProfileRepository @Inject constructor(
    private val userDao: UserDao,
    private val profileApi: IProfileApi,
    private val ioDispatcher: CoroutineDispatcher,
    private val preferenceHelper: PreferenceHelper,
) : IProfileRepository {

    private val logger = AppLogger.create<ProfileRepository>()

    /**
     * Update the user profile on the remote server.
     *
     * @param request Profile update request body.
     * @return [Result] containing [ProfileUpdateResponse] on success, or an exception on failure.
     */
    override suspend fun updateProfile(
        request: ProfileUpdateRequest
    ): Result<ProfileUpdateResponse> =
        withContext(ioDispatcher) {
            try {
                logger.i("Updating profile for user: ${request.fullName}")
                val response = profileApi.updateProfile(
                    authorization = "Bearer ${preferenceHelper.getAccessToken()}",
                    request = request
                )
                logger.i("Profile update successful.")
                Result.success(response)
            } catch (e: Exception) {
                logger.e("Profile update failed.", e)
                Result.failure(e)
            }
        }

    /**
     * Delete the user profile permanently from the remote server.
     *
     * @return [Result] containing [ProfileDeleteResponse] on success, or an exception on failure.
     */
    override suspend fun deleteProfile(): Result<ProfileDeleteResponse> =
        withContext(ioDispatcher) {
            try {
                logger.i("Deleting user profile.")
                val response = profileApi.deleteProfile()
                logger.i("Profile deletion successful.")
                Result.success(response)
            } catch (e: Exception) {
                logger.e("Profile deletion failed.", e)
                Result.failure(e)
            }
        }
}
