package com.example.pillcountingnewmodels.feature.otp.data

import com.example.pillcountingnewmodels.feature.register.data.remote.IVerifyPinAPI
import com.example.pillcountingnewmodels.feature.register.domain.data.IVerifyPinRepository
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinRequest
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The default implementation of [IVerifyPinRepository] that interacts with a remote API.
 *
 * This repository is responsible for handling all data operations related to OTP (One-Time Password)
 * verification by communicating with the remote server.
 *
 * @property verifyPinApi The Retrofit service for making network authentication requests, which includes the verify endpoint.
 * @property ioDispatcher The coroutine dispatcher for running all network operations on a background thread.
 */
class VerifyPinRepository @Inject constructor(
    private val verifyPinApi: IVerifyPinAPI,
    private val ioDispatcher: CoroutineDispatcher
) : IVerifyPinRepository {

    /**
     * Executes the OTP verification request against the remote API on an I/O-optimized thread.
     *
     * @param email The user's email address to associate with the OTP.
     * @param otp The one-time password entered by the user.
     * @return A [Result] wrapper containing the [VerifyPinResponse] on success or an exception on failure.
     */
    override suspend fun verifyPin(email: String, otp: String): Result<VerifyPinResponse> =
        // Ensure the network call is always performed off the main thread for performance and stability.
        withContext(ioDispatcher) {
            try {
                // Construct the request body for the API call.
                val request = VerifyPinRequest(email = email, otp = otp)
                val response = verifyPinApi.verifyPin(request)

                // The request was successful; wrap the response in Result.success.
                Result.success(response)
            } catch (e: Exception) {
                // Gracefully handle any exceptions that occur during the network call (e.g., HttpException, IOException)
                // by wrapping the exception in a Result.failure. This allows the ViewModel to handle it cleanly.
                Result.failure(e)
            }
        }
}

