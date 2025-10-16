package com.rite.pillcounting.feature.forgotPassword.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Response payload returned by the backend after initiating the "Forgot Password" flow.
 *
 * This model represents the server's confirmation that an OTP (One-Time Password)
 * or password recovery link has been successfully sent to the user's email.
 *
 * @property message A human-readable success message from the server
 * (e.g., "OTP sent successfully").
 */
data class ForgotPasswordResponse(
    @SerializedName("message")
    val message: String
)
