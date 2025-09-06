package com.example.pillcountingnewmodels.feature.login.domain

import android.util.Patterns
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.models.ValidationResult
import javax.inject.Inject

/**
 * Handles validation for user credentials like email and password.
 * This class is framework-agnostic and returns resource IDs for errors,
 * leaving string resolution to the UI layer (ViewModel).
 */
class CredentialsValidator @Inject constructor() {

    /**
     * Validates an email address format.
     *
     * @param email The email string to validate.
     * @return [ValidationResult] indicating success or failure with a corresponding string resource ID.
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, R.string.error_email_empty)
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, R.string.error_email_invalid)
        }
        return ValidationResult(true)
    }

    /**
     * Validates a password based on a set of security rules, returning the first error found.
     *
     * Rules enforced:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     *
     * @param password The password string to validate.
     * @return [ValidationResult] indicating success or failure with a corresponding string resource ID.
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, R.string.error_password_too_short)
        }
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(false, R.string.error_password_no_uppercase)
        }
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(false, R.string.error_password_no_lowercase)
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, R.string.error_password_no_digit)
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return ValidationResult(false, R.string.error_password_no_special)
        }
        return ValidationResult(true)
    }
}

