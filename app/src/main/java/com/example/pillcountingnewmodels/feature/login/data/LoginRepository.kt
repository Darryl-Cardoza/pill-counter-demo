package com.example.pillcountingnewmodels.feature.login.data

import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.feature.login.data.remote.ILoginApi
import com.example.pillcountingnewmodels.feature.login.domain.data.ILoginRepository
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Default implementation of [ILoginRepository] that interacts with both
 * a remote API and a local Room database.
 *
 * @property userDao DAO for user-related local persistence.
 * @property loginApi Retrofit service for network authentication.
 * @property ioDispatcher Coroutine dispatcher for offloading I/O operations.
 */
class LoginRepository @Inject constructor(
    private val loginApi: ILoginApi,
    private val ioDispatcher: CoroutineDispatcher
) : ILoginRepository {

    /**
     * Authenticates the user against the remote API.
     *
     * @param username The email/username used for login.
     * @return A [Result] wrapping either a [LoginResponse] or an exception on failure.
     */
    override suspend fun login(username: String): Result<LoginResponse> =
        withContext(ioDispatcher) {
            try {
                // Build request body
                val request = LoginRequest(email = username)

                // Call remote API (headers are injected globally via Interceptor)
                val response = loginApi.login(request)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
