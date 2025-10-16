package com.rite.pillcounting.core.api.interfaceDetail

import com.rite.pillcounting.BuildConfig
import com.rite.pillcounting.core.utils.constants.URLConstant
import okhttp3.Interceptor
import okhttp3.Response

/**
 * [HeaderInterceptor] is an OkHttp interceptor responsible for adding standard
 * headers to every outgoing HTTP request.
 *
 * This includes:
 * - `X-Server-Key`: A key used to authenticate requests with the backend server.
 * - `Content-Type`: Specifies the media type of the request body, typically "application/json".
 *
 * This interceptor ensures these headers are applied consistently across all network calls
 * made through the configured OkHttp client.
 *
 * @see Interceptor
 */
class HeaderInterceptor : Interceptor {

    /**
     * Intercepts outgoing HTTP requests and adds the required headers before proceeding.
     *
     * @param chain The request chain that allows manipulation of the request or response.
     * @return The HTTP response returned after applying the modified request.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val modifiedRequest = originalRequest.newBuilder()
            .addHeader("X-Server-Key", BuildConfig.SERVER_KEY)
            .addHeader("Content-Type", URLConstant.CONTENT_TYPE)
            .build()

        return chain.proceed(modifiedRequest)
    }
}
