package com.example.pillcountingnewmodels.feature.login.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.models.ErrorResponse
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.login.data.LoginRepository
import com.example.pillcountingnewmodels.feature.login.domain.CredentialsValidator
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginUiState
import com.example.pillcountingnewmodels.feature.login.domain.model.LogoutUiState
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for handling the authentication logic for the login and logout process.
 *
 * This ViewModel:
 * - Validates user credentials.
 * - Manages authentication API interactions via [LoginRepository].
 * - Updates the UI using [LoginUiState] and [LogoutUiState] flows.
 * - Handles persistent login flags through [PreferenceHelper].
 *
 * @param repository Provides access to login/logout backend operations.
 * @param validator Performs email format validation before login.
 * @param context Required to resolve error strings.
 * @param preferenceHelper Utility to persist token and login state.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val validator: CredentialsValidator,
    @ApplicationContext private val context: Context,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    private val logger = AppLogger.create<LoginViewModel>()

    // Mutable state backing for login UI state
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)

    /** Exposed immutable state flow representing the login UI state. */
    val uiState = _uiState.asStateFlow()

    // Mutable state backing for logout UI state
    private val _logoutUiState = MutableStateFlow<LogoutUiState>(LogoutUiState.Idle)

    /** Exposed immutable state flow representing the logout UI state. */
    val logoutUiState = _logoutUiState.asStateFlow()

    /**
     * Initiates a login attempt using the given email address.
     *
     * - Validates the email locally.
     * - If valid, sends the login request to the backend.
     * - Emits [LoginUiState.Loading], then [Success] or [Error] depending on outcome.
     *
     * @param email The user's email address to authenticate.
     */
    fun login(email: String) {
        val validationResult = validator.validateEmail(email)

        if (!validationResult.isSuccess) {
            val errorMessage = validationResult.errorMessageResId?.let { context.getString(it) }
                ?: context.getString(R.string.error_unknown)
            _uiState.value = LoginUiState.Error(errorMessage)
            return
        }

        if (_uiState.value is LoginUiState.Loading) {
            // Prevent multiple simultaneous login attempts
            return
        }

        viewModelScope.launch {
            logger.i("Login attempt for user: $email")
            _uiState.value = LoginUiState.Loading

            repository.login(email)
                .onSuccess {
                    logger.i("Login successful for user: $email. Token received.")
                    _uiState.value = LoginUiState.Success
                }
                .onFailure { exception ->
                    logger.e("Login failed for user: $email", exception)
                    val errorMessage = if (exception is retrofit2.HttpException) {
                        val errorBody = exception.response()?.errorBody()?.string()
                        errorBody?.let {
                            try {
                                val errorResponse = Gson().fromJson(it, ErrorResponse::class.java)
                                errorResponse.message
                            } catch (e: Exception) {
                                context.getString(R.string.error_unknown)
                            }
                        } ?: context.getString(R.string.error_unknown)
                    } else {
                        context.getString(R.string.error_unknown)
                    }

                    _uiState.value = LoginUiState.Error(
                        errorMessage
                    )
                }
        }
    }

    /**
     * Initiates a logout operation using the given refresh token.
     *
     * - Sends a logout request to the backend.
     * - Updates [logoutUiState] to reflect the progress and result.
     *
     * @param refreshToken The refresh token to invalidate on logout.
     */
    fun logout(refreshToken: String) {
        if (_logoutUiState.value is LogoutUiState.Loading) {
            return // Avoid duplicate logout calls
        }

        viewModelScope.launch {
            logger.i("Logout attempt with refresh token: $refreshToken")
            _logoutUiState.value = LogoutUiState.Loading

            repository.logout(refreshToken)
                .onSuccess {
                    logger.i("Logout successful")
                    _logoutUiState.value = LogoutUiState.Success
                }
                .onFailure { exception ->
                    logger.e("Logout failed", exception)
                    val errorMessage = if (exception is retrofit2.HttpException) {
                        val errorBody = exception.response()?.errorBody()?.string()
                        errorBody?.let {
                            try {
                                val errorResponse = Gson().fromJson(it, ErrorResponse::class.java)
                                errorResponse.message
                            } catch (e: Exception) {
                                context.getString(R.string.error_unknown)
                            }
                        } ?: context.getString(R.string.error_unknown)
                    } else {
                        exception.message ?: context.getString(R.string.error_unknown)
                    }

                    _logoutUiState.value = LogoutUiState.Error(errorMessage)

                }
        }
    }

    /**
     * Resets the login UI state back to [LoginUiState.Idle].
     * This is useful after a login failure or success to clean up the UI.
     */
    fun resetLoginState() {
        if (_uiState.value !is LoginUiState.Idle) {
            _uiState.value = LoginUiState.Idle
        }
    }

    /**
     * Resets the logout UI state back to [LogoutUiState.Idle].
     * Should be called after logout error/success messages are no longer needed.
     */
    fun resetLogoutState() {
        if (_logoutUiState.value !is LogoutUiState.Idle) {
            _logoutUiState.value = LogoutUiState.Idle
        }
    }

    /**
     * Clears both login and logout UI states to idle.
     * Useful when navigating away from the login/logout flow.
     */
    fun clearAllStates() {
        _uiState.value = LoginUiState.Idle
        _logoutUiState.value = LogoutUiState.Idle
    }

}
