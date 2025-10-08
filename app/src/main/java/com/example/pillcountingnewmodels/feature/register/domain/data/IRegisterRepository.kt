package com.example.pillcountingnewmodels.feature.register.domain.data

import com.example.pillcountingnewmodels.feature.register.domain.model.RegisterResponse

/**
 * Defines the contract for user registration data operations.
 *
 * This interfaceDetail abstracts the underlying data source (e.g., remote API, local database),
 * allowing for a clean separation of concerns and improved testability. By depending on this
 * interfaceDetail, ViewModels remain agnostic of the data layer's implementation details.
 */
interface IRegisterRepository {

    /**
     * Attempts to create a new user account with the given credentials.
     *
     * @param username The email address for the new account.
     * @param password The chosen password for the new account.
     * @return A [Result] wrapper which contains the [RegisterResponse] on success,
     * or an [Exception] on failure. This pattern ensures that all possible outcomes,
     * including network errors, are handled gracefully.
     */
    suspend fun register(username: String, password: String): Result<RegisterResponse>
}
