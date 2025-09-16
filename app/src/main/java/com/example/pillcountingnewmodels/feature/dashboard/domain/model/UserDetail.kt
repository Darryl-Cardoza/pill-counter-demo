package com.example.pillcountingnewmodels.feature.dashboard.domain.model

import com.squareup.moshi.JsonClass

/**
 * Data model representing the details of the authenticated user.
 */
@JsonClass(generateAdapter = true)
data class UserDetail(
    val id: String,
    val name: String,
    val email: String,
)
