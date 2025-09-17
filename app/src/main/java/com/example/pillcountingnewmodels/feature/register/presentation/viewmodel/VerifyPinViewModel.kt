package com.example.pillcountingnewmodels.feature.otp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.models.ErrorResponse
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.otp.data.VerifyPinRepository
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinUiState
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the UI state and business logic for the OTP verification screen.
 *
 * Responsibilities:
 * - Validates OTP input
 * - Calls the repository to verify the OTP
 * - Updates [uiState] with loading/success/failure
 * - Persists tokens on success
 */
@HiltViewModel
class VerifyPinViewModel @Inject constructor(
    private val repository: VerifyPinRepository,
    @ApplicationContext private val context: Context,
    private val prefs: PreferenceHelper
) : ViewModel() {

    private val logger = AppLogger.create<VerifyPinViewModel>()

    private val _uiState = MutableStateFlow<VerifyPinUiState>(VerifyPinUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Verifies the entered OTP against the backend.
     *
     * @param email User's email address (used for logging/debugging).
     * @param otp 4-digit OTP entered by the user.
     */
    fun verifyPin(email: String, otp: String) {
        if (otp.length != 4) {
            _uiState.value = VerifyPinUiState.Error(context.getString(R.string.error_invalid_otp))
            return
        }

        if (_uiState.value is VerifyPinUiState.Loading) return

        viewModelScope.launch {
            logger.i("Attempting OTP verification for email: $email")

            _uiState.value = VerifyPinUiState.Loading

            repository.verifyPin(email, otp)
                .onSuccess { response ->
                    logger.i("OTP verification successful. Status: ${response.status}, Message: ${response.message}")

                    val data = response.data

                    val accessToken = data?.accessToken
                    val refreshToken = data?.refreshToken
                    val user = data?.user

                    logger.d("Access Token: ${accessToken?.take(15)}...") // log only prefix
                    logger.d("Refresh Token: ${refreshToken?.take(15)}...")

                    if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                        prefs.saveTokens(accessToken = accessToken, refreshToken = refreshToken)
                        logger.i("Tokens saved to SharedPreferences.")
                    } else {
                        logger.w("Missing access or refresh token. Tokens not saved.")
                    }

                    if (user != null) {
                        logger.i("User verified: email=${user.email}, isVerified=${user.isVerified}, role=${user.role}")
                    } else {
                        logger.w("User object is null in response.")
                    }

                    _uiState.value = VerifyPinUiState.Success
                    logger.i("UI state set to Success.")
                }
                .onFailure { exception ->
                    logger.e("OTP verification failed for user: $email", exception)

                    val errorMessage = if (exception is retrofit2.HttpException) {
                        val errorBody = exception.response()?.errorBody()?.string()
                        errorBody?.let {
                            try {
                                val errorResponse = Gson().fromJson(it, ErrorResponse::class.java)
                                logger.w("Parsed error message: ${errorResponse.message}")
                                errorResponse.message
                            } catch (e: Exception) {
                                logger.e("Failed to parse error response", e)
                                context.getString(R.string.error_unknown)
                            }
                        } ?: context.getString(R.string.error_unknown)
                    } else {
                        exception.message ?: context.getString(R.string.error_unknown)
                    }

                    _uiState.value = VerifyPinUiState.Error(errorMessage)
                }
        }
    }

    /** Resets UI state to Idle — used after user interaction. */
    fun resetState() {
        if (_uiState.value !is VerifyPinUiState.Idle) {
            _uiState.value = VerifyPinUiState.Idle
        }
    }

    /** Called after success to reset the screen for future usage. */
    fun clearAfterSuccess() {
        _uiState.value = VerifyPinUiState.Idle
    }

    /** Marks the user as logged in persistently. */
    fun setUserLoggedIn(isLoggedIn: Boolean) {
        prefs.setUserLoggedIn(isLoggedIn)
    }
}
