package com.example.pillcountingnewmodels.feature.forgotPassword.data

import com.example.pillcountingnewmodels.feature.forgotPassword.data.remote.IForgotPasswordAPI
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.data.IForgotPasswordRepository
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordRequest
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The default implementation of [IForgotPasswordRepository].
 */
class ForgotPasswordRepository @Inject constructor(
    private val forgotPasswordAPI: IForgotPasswordAPI,
    private val ioDispatcher: CoroutineDispatcher
) : IForgotPasswordRepository {

    override suspend fun sendOtp(email: String): Result<ForgotPasswordResponse> =
        withContext(ioDispatcher) {
            try {
                val request = ForgotPasswordRequest(email = email)
                val response = forgotPasswordAPI.forgotPassword(request)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
