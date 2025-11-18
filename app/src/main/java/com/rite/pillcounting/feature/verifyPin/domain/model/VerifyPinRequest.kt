package com.rite.pillcounting.feature.verifyPin.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a verify OTP API request.
 *
 * @property email The user's email address to associate the OTP with.
 * @property otp The one-time password entered by the user.
 */
data class VerifyPinRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("otp")
    val otp: String,
)
