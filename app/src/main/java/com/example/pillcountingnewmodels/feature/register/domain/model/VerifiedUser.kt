package com.example.pillcountingnewmodels.feature.register.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the user object returned after OTP verification.
 */
@JsonClass(generateAdapter = true)
data class VerifiedUser(

    /** User's email address */
    @Json(name = "email")
    val email: String? = null,

    /** Whether the user's email is verified */
    @Json(name = "is_verified")
    val isVerified: Boolean? = null,

    /** Role identifier (e.g., UUID) */
    @Json(name = "role")
    val role: String? = null
)
