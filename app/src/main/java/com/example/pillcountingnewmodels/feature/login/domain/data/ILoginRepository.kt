package com.example.pillcountingnewmodels.feature.login.domain.data

import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse

/**
 * Defines the contract for authentication-related data operations.
 *
 * This interface abstracts the data source, allowing for different implementations
 * (e.g., a fake repository for testing) and promoting a loosely coupled architecture.
 */
interface ILoginRepository {
    /**
     * Attempts to authenticate a user with the given credentials via the remote API.
     * @param username The user's email address.
     * @param password The user's password.
     * @return A [Result] wrapper containing the [LoginResponse] on success or an exception on failure.
     */
    suspend fun login(username: String, password: String): Result<LoginResponse>
}