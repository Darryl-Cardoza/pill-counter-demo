package com.example.pillcountingnewmodels.feature.register.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.logger.AppLogger
import com.example.pillcountingnewmodels.core.utils.validator.CredentialsValidator
import com.example.pillcountingnewmodels.feature.register.domain.data.IRegisterRepository
import com.example.pillcountingnewmodels.feature.register.domain.model.RegisterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the UI state and business logic for the Register screen.
 *
 * This ViewModel is responsible for:
 * - Exposing a [StateFlow] of [RegisterUiState] to the UI.
 * - Handling the user's action to register a new account.
 * - Validating user input by delegating to a [CredentialsValidator].
 * - Communicating with the data layer ([IRegisterRepository]) to perform the registration.
 * - Logging important events for debugging and monitoring.
 *
 * @property repository The data source for registration operations.
 * @property validator The business logic for validating user credentials.
 * @property context The application context, used for resolving string resources.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: IRegisterRepository,
    private val validator: CredentialsValidator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Initialize the logger for this specific class.
    private val logger = AppLogger.create<RegisterViewModel>()

    // A private, mutable StateFlow that holds the current UI state.
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)

    // A public, read-only version of the StateFlow that the UI can collect to observe state changes.
    val uiState = _uiState.asStateFlow()

    /**
     * Orchestrates the user registration process.
     *
     * This function first performs client-side validation on all provided credentials.
     * If validation passes, it proceeds to call the repository to perform the registration.
     * The [uiState] is updated accordingly to reflect loading, success, or error states.
     *
     * @param email The email address entered by the user.
     * @param password The password entered by the user.
     * @param confirmPassword The password confirmation entered by the user.
     */
    fun register(email: String, password: String, confirmPassword: String) {
        // --- Pre-computation Validation ---
        // Validate credentials before making a network request for instant feedback.
        val validationResult = validator.validateEmail(email).takeIf { it.isSuccess }
            ?: validator.validatePassword(password)

        if (!validationResult.isSuccess) {
            val errorMessage = validationResult.errorMessageResId?.let { context.getString(it) }
                ?: context.getString(R.string.error_unknown)
            _uiState.value = RegisterUiState.Error(errorMessage)
            return
        }

        // Specifically check if the passwords match.
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error(context.getString(R.string.error_passwords_do_not_match))
            return
        }

        // Prevent multiple registration requests from being sent if one is already in progress.
        if (_uiState.value is RegisterUiState.Loading) {
            return
        }

        // --- Asynchronous Operation ---
        // Launch a coroutine in the viewModelScope to handle the registration process.
        viewModelScope.launch {
            logger.i("Registration attempt for user: $email")
            _uiState.value = RegisterUiState.Loading

            repository.register(email, password)
                .onSuccess {
                    logger.i("Registration successful for user: $email.")
                    _uiState.value = RegisterUiState.Success
                }
                .onFailure { exception ->
                    logger.e("Registration failed for user: $email", exception)
                    _uiState.value = RegisterUiState.Error(
                        exception.message ?: context.getString(R.string.error_unknown)
                    )
                }
        }
    }

    /**
     * Resets the UI state back to [RegisterUiState.Idle].
     *
     * This is typically called when the user starts typing again after an error has been displayed.
     */
    fun resetState() {
        if (_uiState.value !is RegisterUiState.Idle) {
            _uiState.value = RegisterUiState.Idle
        }
    }
}

