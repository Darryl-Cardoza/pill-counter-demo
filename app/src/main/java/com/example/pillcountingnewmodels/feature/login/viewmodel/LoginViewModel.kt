package com.example.pillcountingnewmodels.feature.login.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.login.data.LoginRepository
import com.example.pillcountingnewmodels.feature.login.domain.CredentialsValidator
import com.example.pillcountingnewmodels.feature.login.domain.model.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the UI state and business logic for the Login screen.
 *
 * This ViewModel is responsible for:
 * - Exposing a [State] of [LoginUiState] to the UI.
 * - Handling user actions, primarily the login attempt.
 * - Validating user input by delegating to a [CredentialsValidator].
 * - Communicating with the data layer ([LoginRepository]) to perform the login operation.
 * - Logging important events for debugging and monitoring.
 *
 * @property repository The data source for login operations.
 * @property validator The business logic for validating user credentials.
 * @property context The application context, used for resolving string resources.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val validator: CredentialsValidator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Initialize the logger for this specific class.
    private val logger = AppLogger.create<LoginViewModel>()

    // A private, mutable StateFlow that holds the current UI state.
    // This is the single source of truth for the Login screen's state.
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)

    // A public, read-only version of the StateFlow that the UI can collect to observe state changes.
    val uiState = _uiState.asStateFlow()

    /**
     * Orchestrates the login process.
     *
     * This function first performs client-side validation on the provided credentials.
     * If validation passes, it proceeds to call the repository to perform the login.
     * The [uiState] is updated accordingly to reflect loading, success, or error states.
     *
     * @param email The email address entered by the user.
     */
    fun login(email: String) {
        // --- Pre-computation Validation ---
        val validationResult = validator.validateEmail(email)

        if (!validationResult.isSuccess) {
            val errorMessage = validationResult.errorMessageResId?.let { context.getString(it) }
                ?: context.getString(R.string.error_unknown)
            _uiState.value = LoginUiState.Error(errorMessage)
            return
        }

        if (_uiState.value is LoginUiState.Loading) {
            return
        }

        // --- Asynchronous Operation ---
        viewModelScope.launch {
            logger.i("Login attempt for user: $email")
            _uiState.value = LoginUiState.Loading

            // Delegate the login call to the repository and handle the Result wrapper.
            repository.login(email)
                .onSuccess { loginResponse ->
                    // The API call was successful.
                    logger.i("Login successful for user: $email. Token received.")
                    _uiState.value = LoginUiState.Success
                }
                .onFailure { exception ->
                    // The API call failed due to a network error, server error, or other exception.
                    logger.e("Login failed for user: $email", exception)
                    _uiState.value = LoginUiState.Error(
                        exception.message ?: context.getString(R.string.error_unknown)
                    )
                }
        }
    }

    /**
     * Resets the UI state back to [LoginUiState.Idle].
     *
     * This is typically called when the user starts interacting with the input fields again
     * after an error has been displayed, allowing the error message to be cleared.
     */
    fun resetState() {
        if (_uiState.value !is LoginUiState.Idle) {
            _uiState.value = LoginUiState.Idle
        }
    }

    fun clearAll() {
        _uiState.value = LoginUiState.Idle
    }
}
