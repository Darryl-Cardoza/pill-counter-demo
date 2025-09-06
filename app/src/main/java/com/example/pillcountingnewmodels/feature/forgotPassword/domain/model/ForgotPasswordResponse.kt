package com.example.pillcountingnewmodels.feature.forgotPassword.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a successful forgot password API response.
 *
 * @property message A success message from the server (e.g., "OTP sent successfully").
 */
data class ForgotPasswordResponse(
    @SerializedName("message")
    val message: String
)
