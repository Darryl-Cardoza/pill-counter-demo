package com.rite.pillcounting.feature.register.data

import com.rite.pillcounting.core.room.dao.UserDao
import com.rite.pillcounting.feature.register.data.remote.IRegisterAPI
import com.rite.pillcounting.feature.register.domain.data.IRegisterRepository
import com.rite.pillcounting.feature.register.domain.model.RegisterRequest
import com.rite.pillcounting.feature.register.domain.model.RegisterResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The default implementation of [IRegisterRepository] that interacts with a remote API.
 *
 * This repository is responsible for handling all data operations related to user registration,
 * primarily by communicating with a remote server.
 *
 * @property userDao The Data Access Object for interacting with the local user table. Currently unused but available for future caching logic.
 * @property registerApi The Retrofit service for making network registration requests.
 * @property ioDispatcher The coroutine dispatcher for running all network operations on a background thread.
 */
class RegisterRepository @Inject constructor(
    private val userDao: UserDao,
    private val registerApi: IRegisterAPI,
    private val ioDispatcher: CoroutineDispatcher
) : IRegisterRepository {

    /**
     * Executes the registration request against the remote API on an I/O-optimized thread.
     *
     * @param username The email address for the new account.
     * @param password The chosen password for the new account.
     * @return A [Result] wrapper containing the [RegisterResponse] on success or an exception on failure.
     */
    override suspend fun register(
        username: String,
        password: String
    ): Result<RegisterResponse> =
        // Ensure the network call is always performed off the main thread.
        withContext(ioDispatcher) {
            try {
                // Construct the request body for the API call.
                val request = RegisterRequest(email = username, password = password)
                val response = registerApi.register(request)

                // Upon a successful registration, you could add logic here to
                // immediately cache the new user's data in the local Room database.
                // For example:
                // val newUser = UserEntity(username = username, ...)
                // userDao.insertUser(newUser)

                Result.success(response)
            } catch (e: Exception) {
                // Gracefully handle potential network or API errors (e.g., HttpException, IOException)
                // by wrapping the exception in a Result.failure.
                Result.failure(e)
            }
        }
}
