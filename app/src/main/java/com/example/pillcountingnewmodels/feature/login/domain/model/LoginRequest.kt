package com.example.pillcountingnewmodels.feature.login.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a login API request.
 *
 * @property email The user's email address.
 * @property password The user's raw password.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
