package com.example.pillcountingnewmodels.feature.register.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a registration API request.
 *
 * @property email The user's email address.
 * @property password The user's chosen password.
 */
data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
