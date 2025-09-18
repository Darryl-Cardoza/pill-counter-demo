package com.example.pillcountingnewmodels.feature.forgotPassword.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.forgotPassword.data.ForgotPasswordRepository
import com.example.pillcountingnewmodels.feature.forgotPassword.domain.model.ForgotPasswordUiState
import com.example.pillcountingnewmodels.feature.login.domain.CredentialsValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the Forgot Password screen state
 * and coordinating the password recovery workflow.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: ForgotPasswordRepository,
    private val validator: CredentialsValidator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val logger = AppLogger.create<ForgotPasswordViewModel>()

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Attempts to send an OTP to the provided email address.
     * Handles validation, repository call, and state transitions.
     */
    fun sendOtp(email: String) {
        // Validate email format before hitting API
        val validationResult = validator.validateEmail(email)
        if (!validationResult.isSuccess) {
            val errorMessage = resolveErrorMessage(validationResult.errorMessageResId)
            logger.w("Invalid email input for forgot password: $email | error=$errorMessage")
            _uiState.update { ForgotPasswordUiState.Error(errorMessage) }
            return
        }

        // Prevent duplicate API calls
        if (_uiState.value is ForgotPasswordUiState.Loading) return

        viewModelScope.launch {
            logger.i("Starting forgot password flow for email=$email")
            _uiState.update { ForgotPasswordUiState.Loading }

            repository.sendOtp(email)
                .onSuccess {
                    logger.i("OTP sent successfully for email=$email")
                    _uiState.update { ForgotPasswordUiState.Success(email) }
                }
                .onFailure { exception ->
                    val errorMsg = exception.message ?: context.getString(R.string.error_unknown)
                    logger.e("OTP request failed for email=$email | error=$errorMsg", exception)
                    _uiState.update { ForgotPasswordUiState.Error(errorMsg) }
                }
        }
    }

    /**
     * Resets the state to Idle if the current state is not Idle.
     */
    fun resetState() {
        if (_uiState.value !is ForgotPasswordUiState.Idle) {
            logger.d("Resetting forgot password state to Idle")
            _uiState.update { ForgotPasswordUiState.Idle }
        }
    }

    /**
     * Resolves a localized error message based on resource ID or defaults.
     */
    private fun resolveErrorMessage(resId: Int?): String =
        resId?.let { context.getString(it) } ?: context.getString(R.string.error_unknown)
}
