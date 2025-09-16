package com.example.pillcountingnewmodels.feature.register.domain.model

import com.example.pillcountingnewmodels.core.models.ErrorDetail
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the data structure for the OTP verification API response.
 *
 * On success: includes tokens, expiration, and user info.
 * On failure: includes validation or server error details.
 */
@JsonClass(generateAdapter = true)
data class VerifyPinResponse(

    // Common fields
    @Json(name = "status")
    val status: Int? = null,

    @Json(name = "is_success")
    val isSuccess: Boolean? = null,

    @Json(name = "message")
    val message: String? = null,

    // Success fields
    @Json(name = "access_token")
    val accessToken: String? = null,

    @Json(name = "refresh_token")
    val refreshToken: String? = null,

    @Json(name = "expires_in")
    val expiresIn: Int? = null,

    @Json(name = "user")
    val user: Any? = null,

    @Json(name = "detail")
    val detail: List<ErrorDetail>? = null
)


