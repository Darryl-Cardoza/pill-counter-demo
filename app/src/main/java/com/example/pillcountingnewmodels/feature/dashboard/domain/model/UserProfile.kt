package com.example.pillcountingnewmodels.feature.dashboard.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents nested user profile information.
 */
@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)