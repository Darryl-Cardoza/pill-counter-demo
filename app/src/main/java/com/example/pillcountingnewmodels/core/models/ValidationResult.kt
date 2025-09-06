package com.example.pillcountingnewmodels.core.models

import androidx.annotation.StringRes

/**
 * Represents the outcome of a validation check.
 *
 * @property isSuccess True if validation passed, false otherwise.
 * @property errorMessageResId The string resource ID for the error message if validation failed.
 */
data class ValidationResult(
    val isSuccess: Boolean,
    @StringRes val errorMessageResId: Int? = null
)
