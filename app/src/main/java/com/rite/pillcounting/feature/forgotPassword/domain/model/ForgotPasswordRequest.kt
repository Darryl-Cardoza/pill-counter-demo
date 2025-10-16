package com.rite.pillcounting.feature.forgotPassword.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Request payload for initiating the "Forgot Password" flow.
 *
 * This model is serialized into JSON and sent to the backend API
 * when the user requests an OTP (One-Time Password) for password recovery.
 *
 * @property email The registered email address of the user requesting password recovery.
 */
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)
