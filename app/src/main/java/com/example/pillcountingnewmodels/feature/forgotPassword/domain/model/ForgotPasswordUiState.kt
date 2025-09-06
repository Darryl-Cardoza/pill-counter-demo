package com.example.pillcountingnewmodels.feature.forgotPassword.domain.model

/**
 * Defines the possible states for the Forgot Password screen UI.
 * This sealed interface acts as a state machine for the screen.
 */
sealed interface ForgotPasswordUiState {
    /** The initial state before any action is taken. */
    data object Idle : ForgotPasswordUiState

    /** Represents the in-progress state of the network call to send an OTP. */
    data object Loading : ForgotPasswordUiState

    /**
     * Indicates that the OTP was successfully sent.
     * @param email The email address to which the OTP was sent, to be passed to the next screen.
     */
    data class Success(val email: String) : ForgotPasswordUiState

    /** Represents an error state, containing a message for the user. */
    data class Error(val message: String) : ForgotPasswordUiState
}
