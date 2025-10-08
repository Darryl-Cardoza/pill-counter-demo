package com.example.pillcountingnewmodels.feature.register.domain.model

/**
 * Defines the possible states for the Login screen UI, serving as a state machine.
 * This sealed interfaceDetail ensures that the UI can only be in one of these well-defined states at a time.
 */
sealed interface RegisterUiState {
    /** The initial or default state. */
    data object Idle : RegisterUiState
    /** Indicates an ongoing operation, like a network request. */
    data object Loading : RegisterUiState
    /** Indicates that the login operation completed successfully. */
    data object Success : RegisterUiState
    /** Represents an error state, containing a message to be displayed to the user. */
    data class Error(val message: String) : RegisterUiState
}