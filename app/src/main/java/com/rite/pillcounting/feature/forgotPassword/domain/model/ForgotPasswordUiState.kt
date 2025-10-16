package com.rite.pillcounting.feature.forgotPassword.domain.model

/**
 * Represents the UI state of the Forgot Password screen.
 *
 * This sealed interfaceDetail models the different states in the password recovery flow,
 * enabling the UI to react accordingly (e.g., show a loading indicator, display an error,
 * or navigate forward on success).
 */
sealed interface ForgotPasswordUiState {

    /** The default state before any user interaction occurs. */
    data object Idle : ForgotPasswordUiState

    /** State indicating that the OTP request is currently being processed. */
    data object Loading : ForgotPasswordUiState

    /**
     * State representing a successful OTP request.
     *
     * @property email The email address to which the OTP was sent.
     *                 Passed forward for pre-filling in the verification screen.
     */
    data class Success(val email: String) : ForgotPasswordUiState

    /**
     * State representing a failure during the OTP request.
     *
     * @property message A human-readable error message for display in the UI.
     */
    data class Error(val message: String) : ForgotPasswordUiState
}
