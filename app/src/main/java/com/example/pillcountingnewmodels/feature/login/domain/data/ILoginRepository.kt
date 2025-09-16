package com.example.pillcountingnewmodels.feature.login.domain.data

import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse

/**
 * Contract for authentication-related data operations.
 *
 * This interface abstracts the data source, allowing multiple implementations
 * (e.g., a real repository, fake/test repository, or mock for unit testing).
 * It helps maintain a clean architecture and promotes testability.
 */
interface ILoginRepository {

    /**
     * Attempts to authenticate a user with the given credentials via the remote API.
     *
     * @param username The user's email/username.
     * @return A [Result] wrapping either:
     *   - [LoginResponse] on success, or
     *   - an exception on failure.
     */
    suspend fun login(
        username: String,
    ): Result<LoginResponse>
}
