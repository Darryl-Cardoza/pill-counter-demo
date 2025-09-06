package com.example.pillcountingnewmodels.feature.login.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a successful login API response.
 *
 * @property userId The unique identifier for the user.
 * @property token The authentication token for subsequent API calls.
 */
data class LoginResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("token")
    val token: String
)
