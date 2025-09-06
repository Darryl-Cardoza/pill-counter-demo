package com.example.pillcountingnewmodels.feature.forgotPassword.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a forgot password API request.
 *
 * @property email The user's registered email address.
 */
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)
