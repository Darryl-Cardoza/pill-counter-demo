package com.example.pillcountingnewmodels.feature.login.data.remote

import com.example.pillcountingnewmodels.BuildConfig
import com.example.pillcountingnewmodels.core.utils.URLConstant
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginRequest
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginResponse
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
}
