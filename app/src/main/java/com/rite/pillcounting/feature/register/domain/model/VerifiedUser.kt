package com.rite.pillcounting.feature.register.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the user object returned after OTP verification.
 */
@JsonClass(generateAdapter = true)
data class VerifiedUser(

    /** Unique user identifier */
    @Json(name = "user_id")
    val userId: String? = null,

    /** User's email address */
    @Json(name = "email")
    val email: String? = null,

    /** Whether the user's email is verified */
    @Json(name = "is_verified")
    val isVerified: Boolean? = null,

    /** Role object containing id and name */
    @Json(name = "role")
    val role: UserRole? = null,

    /** Whether the user account is locked */
    @Json(name = "auth_is_locked")
    val authIsLocked: Boolean? = null
)
