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
 * The default implementation of [LoginRepository] that interacts with both a remote API and a local Room database.
 *
 * @property userDao The Data Access Object for interacting with the user table.
 * @property loginApi The Retrofit service for making network authentication requests.
 * @property ioDispatcher The coroutine dispatcher for running all operations on a background thread.
 */
class LoginRepository @Inject constructor(
    private val userDao: UserDao,
    private val loginApi: ILoginApi,
    private val ioDispatcher: CoroutineDispatcher
) : ILoginRepository {

    /**
     * Executes the login request against the remote API on an I/O-optimized thread.
     * Upon success, it could also perform local database operations, like caching user data.
     */
    override suspend fun login(username: String, password: String): Result<LoginResponse> =
        withContext(ioDispatcher) {
            try {
                // Create the request body and call the remote API.
                val request = LoginRequest(email = username, password = password)
                val response = loginApi.login(request)

                // You could add logic here to save the user's token or profile data to the local database.
                // For example: userDao.cacheUser(response.userId, ...)

                Result.success(response)
            } catch (e: Exception) {
                // Gracefully handle potential network or API errors (e.g., HttpException, IOException).
                Result.failure(e)
            }
        }


}

