package com.example.pillcountingnewmodels.feature.register.domain.data

import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinResponse

/**
 * Defines the contract for OTP verification data operations.
 */
interface IVerifyPinRepository {
    /**
     * Attempts to verify the user's OTP via the remote API.
     *
     * @param email The user's email address.
     * @param otp The one-time password.
     * @return A [Result] wrapper containing the [VerifyPinResponse] on success or an exception on failure.
     */
    suspend fun verifyPin(email: String, otp: String): Result<VerifyPinResponse>
}
