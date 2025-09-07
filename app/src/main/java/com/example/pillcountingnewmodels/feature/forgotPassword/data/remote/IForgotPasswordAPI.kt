package com.example.pillcountingnewmodels.feature.forgotPassword.data.remote

import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordRequest
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit service for handling "Forgot Password" related network requests.
 */
interface IForgotPasswordAPI {

    /**
     * Triggers the forgot password process for the given user.
     *
     * Typically, this endpoint sends a password reset link or OTP
     * to the user's registered email/phone.
     *
     * @param request A [ForgotPasswordRequest] containing the user's identifier (e.g., email).
     * @return A [ForgotPasswordResponse] containing the server response (success/failure).
     */
    @POST("v1/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): ForgotPasswordResponse
}
