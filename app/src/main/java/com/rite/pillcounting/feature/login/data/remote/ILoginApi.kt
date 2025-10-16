package com.rite.pillcounting.feature.login.data.remote

import com.rite.pillcounting.core.utils.constants.URLConstant
import com.rite.pillcounting.feature.login.domain.model.LoginRequest
import com.rite.pillcounting.feature.login.domain.model.LoginResponse
import com.rite.pillcounting.feature.login.domain.model.LogoutRequest
import com.rite.pillcounting.feature.login.domain.model.LogoutResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit service interfaceDetail for user authentication APIs.
 */
interface ILoginApi {

    /**
     * Authenticates the user with given credentials.
     *
     * @param request The login request containing user details.
     * @return A [LoginResponse] containing authentication results such as access tokens.
     */
    @POST(URLConstant.SEND_OTP)
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
    suspend fun logout(
        @Body request: LogoutRequest
    ): LogoutResponse
}
