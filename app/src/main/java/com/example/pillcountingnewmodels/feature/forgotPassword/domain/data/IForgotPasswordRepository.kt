package com.example.pillcountingnewmodels.feature.forgotPassword.domain.data

import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordResponse

/**
 * Contract for the Forgot Password feature's data operations.
 *
 * This repository interface abstracts the process of initiating
 * a password recovery request by sending an OTP (One-Time Password)
 * to the user's registered email address.
 *
 * Implementations of this interface should handle the interaction
 * with remote data sources (e.g., a REST API).
 */
interface IForgotPasswordRepository {

    /**
     * Initiates the password recovery process by sending an OTP to the given email.
     *
     * @param email The registered email address of the user requesting the OTP.
     * @return A [Result] containing:
     *   - [ForgotPasswordResponse] on success.
     *   - [Exception] on failure, wrapped in [Result.failure].
     */
    suspend fun sendOtp(email: String): Result<ForgotPasswordResponse>
}
