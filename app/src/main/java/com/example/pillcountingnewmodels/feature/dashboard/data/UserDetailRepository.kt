package com.example.pillcountingnewmodels.feature.dashboard.data

import com.example.pillcountingnewmodels.core.models.RefreshTokenRequest
import com.example.pillcountingnewmodels.core.network.IApplicationSettingInterface
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.dashboard.data.remote.IUserDetailAPI
import com.example.pillcountingnewmodels.feature.dashboard.domain.data.IUserDetailRepository
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.UserDetailResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Default implementation of [IUserDetailRepository] that interacts with
 * a remote API to fetch user details, handling token expiration.
 *
 * @property api Retrofit service for user details API.
 * @property preferenceHelper Helper for managing stored tokens.
 * @property ioDispatcher Coroutine dispatcher for offloading I/O operations.
 */
class UserDetailRepository @Inject constructor(
    private val api: IUserDetailAPI,
    private val applicationSettingApi: IApplicationSettingInterface,
    private val preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher
) : IUserDetailRepository {

    private val logger = AppLogger.create<UserDetailRepository>()

    /**
     * Fetches the authenticated user's details from the server,
     * automatically refreshing the access token if expired.
     *
     * @param token Current access token for authorization.
     * @return A [Result] wrapping either a [UserDetail] on success,
     * or an exception on failure.
     */
    override suspend fun getUserDetail(token: String): Result<UserDetailResponse> =
        withContext(ioDispatcher) {
            try {
                logger.i("Fetching user detail with token: $token")
                val response = api.getUserDetail("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let {
                        logger.i("User detail fetched successfully.")
                        Result.success(it)
                    } ?: run {
                        logger.e("Empty response body while fetching user detail.")
                        Result.failure(Exception("Empty response body"))
                    }
                } else if (response.code() == 401) {
                    logger.w("Access token expired. Attempting token refresh...")

                    val refreshToken = preferenceHelper.getRefreshToken()
                        ?: return@withContext Result.failure(Exception("No refresh token available"))

                    val refreshResponse = try {
                        applicationSettingApi.refreshToken(RefreshTokenRequest(refreshToken))
                    } catch (e: Exception) {
                        logger.e("Token refresh request failed", e)
                        return@withContext Result.failure(e)
                    }

                    if (refreshResponse.accessToken != null) {
                        logger.i("Token refreshed successfully. Saving new tokens.")
                        preferenceHelper.saveTokens(
                            accessToken = refreshResponse.accessToken,
                            refreshToken = refreshResponse.refreshToken ?: refreshToken
                        )

                        // Retry original request with new access token
                        val retryResponse = api.getUserDetail("Bearer ${refreshResponse.accessToken}")
                        if (retryResponse.isSuccessful) {
                            retryResponse.body()?.let {
                                return@withContext Result.success(it)
                            } ?: run {
                                return@withContext Result.failure(Exception("Empty response body after retry"))
                            }
                        } else {
                            return@withContext Result.failure(
                                Exception("Failed to fetch user detail after token refresh: HTTP ${retryResponse.code()}")
                            )
                        }
                    } else {
                        return@withContext Result.failure(Exception("Failed to refresh token: ${refreshResponse.message}"))
                    }
                } else {
                    logger.e("Error fetching user detail. HTTP code: ${response.code()}")
                    Result.failure(Exception("Error fetching user detail: ${response.code()}"))
                }
            } catch (e: HttpException) {
                logger.e("HttpException while fetching user detail", e)
                Result.failure(e)
            } catch (e: Exception) {
                logger.e("Unexpected error while fetching user detail", e)
                Result.failure(e)
            }
        }
}
