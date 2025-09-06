package com.example.pillcountingnewmodels.feature.forgotPassword.domain.data

import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordResponse

/**
 * Defines the contract for password recovery data operations.
 */
interface IForgotPasswordRepository {
    /**
     * Sends a request to the remote API to initiate password recovery for the given email.
     *
     * @param email The user's registered email address.
     * @return A [Result] wrapper containing the [ForgotPasswordResponse] on success or an exception on failure.
     */
    suspend fun sendOtp(email: String): Result<ForgotPasswordResponse>
}
