package com.example.pillcountingnewmodels.feature.forgotPassword.data.remote

import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordResponse
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Defines the network endpoints for user authentication using Retrofit.
 */
interface IForgotPasswordAPI {
    /**
     * Sends user details to the remote server to create a new account.
     * @param request A data object containing the user's email and password.
     * @return A [ForgotPasswordResponse] containing a success message.
     */
    @POST("v1/register") // TODO(Replace with your actual register endpoint)
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse
}
