package com.example.pillcountingnewmodels.feature.dashboard.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the full structure of the user details returned from the API.
 */
@JsonClass(generateAdapter = true)
data class UserDetail(

    /** User's email address. */
    @Json(name = "email")
    val email: String,

    /** Whether the user's email is verified. */
    @Json(name = "is_verified")
    val isVerified: Boolean,

    /** Role ID associated with the user. */
    @Json(name = "role")
    val role: String,

    /** Optional profile data like full name, phone, and avatar. */
    @Json(name = "profile")
    val profile: UserProfile? = null,

    /** Optional user settings such as theme, language, etc. */
    @Json(name = "settings")
    val settings: UserSettings? = null
)



