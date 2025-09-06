package com.example.pillcountingnewmodels.feature.forgotPassword.viewmodel

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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the UI state and business logic for the Forgot Password screen.
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
     * Orchestrates the password recovery process.
     */
    fun sendOtp(email: String) {
        val validationResult = validator.validateEmail(email)
        if (!validationResult.isSuccess) {
            val errorMessage = validationResult.errorMessageResId?.let { context.getString(it) }
                ?: context.getString(R.string.error_unknown)
            _uiState.value = ForgotPasswordUiState.Error(errorMessage)
            return
        }

        if (_uiState.value is ForgotPasswordUiState.Loading) {
            return
        }

        viewModelScope.launch {
            logger.i("Forgot password attempt for user: $email")
            _uiState.value = ForgotPasswordUiState.Loading

            repository.sendOtp(email)
                .onSuccess {
                    logger.i("Forgot password OTP sent successfully for user: $email.")
                    _uiState.value = ForgotPasswordUiState.Success(email)
                }
                .onFailure { exception ->
                    logger.e("Forgot password failed for user: $email", exception)
                    _uiState.value = ForgotPasswordUiState.Error(
                        exception.message ?: context.getString(R.string.error_unknown)
                    )
                }
        }
    }

    /**
     * Resets the UI state back to Idle.
     */
    fun resetState() {
        if (_uiState.value !is ForgotPasswordUiState.Idle) {
            _uiState.value = ForgotPasswordUiState.Idle
        }
    }
}
