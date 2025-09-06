package com.example.pillcountingnewmodels.feature.register.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a successful OTP verification API response.
 *
 * @property message A success message from the server.
 */
data class VerifyPinResponse(
    @SerializedName("message")
    val message: String
)
