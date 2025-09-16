package com.example.pillcountingnewmodels.feature.login.data.remote

import com.example.pillcountingnewmodels.BuildConfig
import com.example.pillcountingnewmodels.core.utils.URLConstant
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse
import com.example.pillcountingnewmodels.feature.login.domain.model.LogoutRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LogoutResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit service interface for user authentication APIs.
 */
interface ILoginApi {

    /**
     * Authenticates the user with given credentials.
     *
     * @param request The login request containing user details.
     * @return A [LoginResponse] containing authentication results such as access tokens.
     */
    @POST(URLConstant.SEND_OTP)
    @Headers(
        "Content-Type: ${URLConstant.CONTENT_TYPE}",
        "X-Server-Key: ${BuildConfig.SERVER_KEY}"
    )
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    /**
     * Logs out the user by invalidating the refresh token.
     *
     * @param request The logout request containing the refresh token to invalidate.
     * @return A [LogoutResponse] indicating success or failure of the logout operation.
     */
    @POST(URLConstant.LOGOUT)
    @Headers(
        "Content-Type: ${URLConstant.CONTENT_TYPE}",
//        "X-Server-Key: ${BuildConfig.SERVER_KEY}"
    )
    suspend fun logout(
        @Body request: LogoutRequest
    ): LogoutResponse
}
