package com.rite.pillcounting.feature.register.data.remote

import com.rite.pillcounting.feature.register.domain.model.RegisterRequest
import com.rite.pillcounting.feature.register.domain.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Defines the network endpoints for user authentication using Retrofit.
 */
interface IRegisterAPI {
    /**
     * Sends user details to the remote server to create a new account.
     * @param request A data object containing the user's email and password.
     * @return A [RegisterResponse] containing a success message.
     */
    @POST("v1/register") // TODO(Replace with your actual register endpoint)
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}
