package com.example.pillcountingnewmodels.core.refreshToken.data.remote

import com.example.pillcountingnewmodels.core.refreshToken.domain.model.RefreshTokenRequest
import com.example.pillcountingnewmodels.core.refreshToken.domain.model.RefreshTokenResponse
import com.example.pillcountingnewmodels.core.utils.constants.URLConstant
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * **Refresh Token API Service**
 *
 * Defines the Retrofit endpoints responsible for refreshing access tokens
 * when they expire or become invalid. This API is invoked automatically
 * by [com.example.pillcountingnewmodels.core.api.interceptor.TokenAuthenticator]
 * when the backend returns a `401 Unauthorized` response.
 *
 * ---
 * ### ⚙️ Design Notes
 * - Uses `@POST` to securely exchange the existing refresh token for a new pair of tokens.
 * - Content type is explicitly set to `application/json` using [URLConstant.CONTENT_TYPE].
 * - This API must **not** reuse the main Retrofit client used for normal API calls,
 *   to avoid circular dependency with [TokenAuthenticator].
 *
 * ---
 * ### Example Usage
 * ```kotlin
 * val request = RefreshTokenRequest(refreshToken = currentRefreshToken)
 * val response = refreshTokenApi.refreshToken(request)
 * val newAccessToken = response.accessToken
 * val newRefreshToken = response.refreshToken
 * ```
 */
interface IRefreshTokenAPI {

    /**
     * Exchanges a valid refresh token for a new access token (and optionally a new refresh token).
     *
     * - Called automatically by the authenticator when an access token expires.
     * - Should return both tokens and their expiration details.
     *
     * @param request The [RefreshTokenRequest] containing the current refresh token.
     * @return A [RefreshTokenResponse] containing the new token pair and metadata.
     */
    @POST(URLConstant.REFRESH_TOKEN)
    @Headers("Content-Type: ${URLConstant.CONTENT_TYPE}")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse
}
