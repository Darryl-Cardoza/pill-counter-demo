package com.example.pillcountingnewmodels.feature.otp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.otp.data.VerifyPinRepository
import com.example.pillcountingnewmodels.feature.register.domain.model.VerifyPinUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the UI state and business logic for the OTP (One-Time Password) verification screen.
 *
 * This ViewModel is responsible for:
 * - Exposing a [StateFlow] of [VerifyPinUiState] to the UI.
 * - Handling the user's action to verify the OTP.
 * - Performing basic validation on the OTP format.
 * - Communicating with the data layer ([VerifyPinRepository]) to perform the verification.
 * - Logging important events for debugging and monitoring.
 *
 * @property repository The data source for OTP verification operations.
 * @property context The application context, used for resolving string resources.
 */
@HiltViewModel
class VerifyPinViewModel @Inject constructor(
    private val repository: VerifyPinRepository,
    @ApplicationContext private val context: Context,
    private val prefs: PreferenceHelper
) : ViewModel() {

    // Initialize the logger for this specific class.
    private val logger = AppLogger.create<VerifyPinViewModel>()

    // A private, mutable StateFlow that holds the current UI state.
    private val _uiState = MutableStateFlow<VerifyPinUiState>(VerifyPinUiState.Idle)

    // A public, read-only version of the StateFlow that the UI can collect to observe state changes.
    val uiState = _uiState.asStateFlow()

    /**
     * Orchestrates the OTP verification process.
     *
     * This function first performs client-side validation on the OTP length.
     * If validation passes, it proceeds to call the repository to perform the verification.
     * The [uiState] is updated accordingly to reflect loading, success, or error states.
     *
     * @param email The email address associated with the OTP.
     * @param otp The OTP entered by the user.
     */
    fun verifyPin(email: String, otp: String) {
        // Basic client-side validation to provide instant feedback for an invalid OTP format.
        if (otp.length != 4) { // Assuming a 4-digit OTP, adjust if necessary.
            _uiState.value = VerifyPinUiState.Error(context.getString(R.string.error_invalid_otp))
            return
        }

        // Prevent multiple verification requests from being sent if one is already in progress.
        if (_uiState.value is VerifyPinUiState.Loading) {
            return
        }

        // Launch a coroutine in the viewModelScope to handle the verification process.
        viewModelScope.launch {
            logger.i("OTP verification attempt for user: $email")
            _uiState.value = VerifyPinUiState.Loading

            // Delegate the verification call to the repository and handle the Result wrapper.
            repository.verifyPin(email, otp)
                .onSuccess { response ->
                    logger.i("OTP verification successful for user: $email.")

                    response.accessToken?.let { access ->
                        response.refreshToken?.let { refresh ->
                            prefs.saveTokens(accessToken = access, refreshToken = refresh)
                            logger.i("Tokens saved in SharedPreferences")
                        }
                    }

                    _uiState.value = VerifyPinUiState.Success
                }
                .onFailure { exception ->
                    logger.e("OTP verification failed for user: $email", exception)
                    _uiState.value = VerifyPinUiState.Error(
                        exception.message ?: context.getString(R.string.error_unknown)
                    )
                }
        }
    }

    /**
     * Resets the UI state back to [VerifyPinUiState.Idle].
     *
     * This is typically called when the user starts typing again after an error has been displayed.
     */
    fun resetState() {
        if (_uiState.value !is VerifyPinUiState.Idle) {
            _uiState.value = VerifyPinUiState.Idle
        }
    }

    fun clearAfterSuccess() {
        _uiState.value = VerifyPinUiState.Idle
    }

}

