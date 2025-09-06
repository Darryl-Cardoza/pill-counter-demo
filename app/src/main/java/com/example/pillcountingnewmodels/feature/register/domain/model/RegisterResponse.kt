package com.example.pillcountingnewmodels.feature.register.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a successful registration API response.
 *
 * @property message A success message from the server.
 */
data class RegisterResponse(
    @SerializedName("message")
    val message: String
)
