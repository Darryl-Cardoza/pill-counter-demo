package com.example.pillcountingnewmodels.feature.register.data.remote

import com.example.pillcountingnewmodels.core.utils.constants.URLConstant.VERIFY_OTP
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinRequest
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Defines the network endpoints for user authentication using Retrofit.
 */
interface IVerifyPinAPI {
    /**
     * Sends user details to the remote server to create a new account.
     * @param request A data object containing the user's email and password.
     * @return A [VerifyPinRequest] containing a success message.
     */
    @POST(VERIFY_OTP)
    suspend fun verifyPin(@Body request: VerifyPinRequest): VerifyPinResponse
}
