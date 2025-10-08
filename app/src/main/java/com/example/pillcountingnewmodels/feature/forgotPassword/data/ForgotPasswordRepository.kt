package com.example.pillcountingnewmodels.feature.forgotPassword.data

import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.forgotPassword.data.remote.IForgotPasswordAPI
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.data.IForgotPasswordRepository
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordRequest
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of the [IForgotPasswordRepository] interfaceDetail.
 *
 * Handles the Forgot Password feature by communicating with the remote [IForgotPasswordAPI].
 * Responsible for sending OTP requests to the backend and wrapping the response
 * in a [Result] for safe error handling.
 *
 * @param forgotPasswordAPI The Retrofit API service for Forgot Password operations.
 * @param ioDispatcher The coroutine dispatcher used to execute network calls off the main thread.
 */
@Singleton
class ForgotPasswordRepository @Inject constructor(
    private val forgotPasswordAPI: IForgotPasswordAPI,
    private val ioDispatcher: CoroutineDispatcher
) : IForgotPasswordRepository {

    // Instantiate a logger specific to this repository
    private val logger = AppLogger.create<ForgotPasswordRepository>()

    /**
     * Sends an OTP request for password reset to the backend service.
     *
     * @param email The email address of the user requesting the OTP.
     * @return A [Result] wrapping the [ForgotPasswordResponse] if successful,
     *         or a [Result.failure] with the caught [Exception] if an error occurs.
     */
    override suspend fun sendOtp(email: String): Result<ForgotPasswordResponse> =
        withContext(ioDispatcher) {
            try {
                logger.i("Initiating forgot password request for email: '$email'")

                val request = ForgotPasswordRequest(email = email)
                val response = forgotPasswordAPI.forgotPassword(request)

                logger.d("Forgot password request successful. Response: $response")
                Result.success(response)
            } catch (e: Exception) {
                logger.e("Error occurred while sending OTP for email: '$email'", e)
                Result.failure(e)
            }
        }
}
