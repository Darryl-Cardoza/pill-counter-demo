package com.example.pillcountingnewmodels.feature.profile.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileUpdateRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "pharmacy_name") val pharmacyName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "npi_id") val npiId: String,
    @Json(name = "is_profile_complete") val isProfileComplete: Boolean,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "notifications_enabled") val notificationsEnabled: Boolean,
    @Json(name = "language") val language: String,
    @Json(name = "timezone") val timezone: String
)