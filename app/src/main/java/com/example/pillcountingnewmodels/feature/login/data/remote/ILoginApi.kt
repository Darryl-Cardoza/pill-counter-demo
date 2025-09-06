package com.example.pillcountingnewmodels.feature.login.data.remote

import com.example.pillcountingnewmodels.feature.login.domain.model.LoginRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Defines the network endpoints for user authentication using Retrofit.
 */
interface ILoginApi {
    /**
     * Sends user credentials to the remote server for authentication.
     * @param request A data object containing the user's email and password.
     * @return A [LoginResponse] containing authentication details like a token.
     */
    @POST("v1/login") // TODO(Replace with your actual login endpoint)
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
