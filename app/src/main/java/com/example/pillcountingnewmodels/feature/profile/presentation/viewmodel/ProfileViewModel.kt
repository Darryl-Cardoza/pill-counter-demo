package com.example.pillcountingnewmodels.feature.profile.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.room.models.UserEntity
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.login.domain.CredentialsValidator
import com.example.pillcountingnewmodels.feature.profile.data.ProfileRepository
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileDeleteUiState
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateRequest
import com.example.pillcountingnewmodels.feature.profile.domain.model.ProfileUpdateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing Profile UI state, validation, and business logic.
 *
 * Responsibilities:
 * - Prefill the profile form from [UserDao].
 * - Validate fields using [CredentialsValidator].
 * - Handle profile update and delete operations via [ProfileRepository].
 * - Manage state flows ([ProfileUpdateUiState], [ProfileDeleteUiState]) for Compose UI.
 *
 * @property repository Repository for profile API operations.
 * @property preferenceHelper Shared preferences helper for storing user session data.
 * @property userDao Room DAO for persisting and observing user data locally.
 * @property validator Validation utility for checking profile field inputs.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val preferenceHelper: PreferenceHelper,
    private val userDao: UserDao,
    private val validator: CredentialsValidator
) : ViewModel() {

    private val logger = AppLogger.create<ProfileViewModel>()

    // ─────────────────────────── UI States ───────────────────────────

    /** State flow representing the status of profile update operations. */
    private val _updateUiState = MutableStateFlow<ProfileUpdateUiState>(ProfileUpdateUiState.Idle)
    val updateUiState = _updateUiState.asStateFlow()

    /** State flow representing the status of profile delete operations. */
    private val _deleteUiState = MutableStateFlow<ProfileDeleteUiState>(ProfileDeleteUiState.Idle)
    val deleteUiState = _deleteUiState.asStateFlow()

    // ─────────────────────────── Profile Fields ───────────────────────────

    /** User's first name input. */
    var firstName by mutableStateOf("")

    /** User's last name input. */
    var lastName by mutableStateOf("")

    /** Pharmacy name input. */
    var pharmacyName by mutableStateOf("")

    /** Phone number input. */
    var phoneNumber by mutableStateOf("")

    /** Email input. */
    var email by mutableStateOf("")

    /** NPI (National Provider Identifier) input. */
    var npi by mutableStateOf("")

    /** Checkbox state for "Do not ask again". */
    var doNotAskAgain by mutableStateOf(false)

    // ─────────────────────────── Validation Errors ───────────────────────────

    var firstNameError by mutableStateOf<Int?>(null)
    var lastNameError by mutableStateOf<Int?>(null)
    var pharmacyNameError by mutableStateOf<Int?>(null)
    var phoneError by mutableStateOf<Int?>(null)
    var emailError by mutableStateOf<Int?>(null)
    var npiError by mutableStateOf<Int?>(null)

    init {
        val localId = preferenceHelper.getLocalId()
        if (localId != null && localId != 0L) {
            observeUser(localId)
        } else {
            logger.w("No localId found in preferences — skipping prefill.")
        }

        // Load the saved "Do Not Ask Again" preference
        doNotAskAgain = preferenceHelper.isDoNotAskAgain()
        logger.i("Initialized doNotAskAgain = $doNotAskAgain")
    }

    fun toggleDoNotAskAgain(value: Boolean) {
        doNotAskAgain = value
        preferenceHelper.saveDoNotAskAgain(value)
        logger.i("DoNotAskAgain updated → $value")
    }

    /**
     * Observes the [UserEntity] in Room by [localId].
     * Automatically updates UI fields when the user record changes.
     *
     * @param localId The Room PK of the user.
     */
    private fun observeUser(localId: Long) {
        viewModelScope.launch {
            userDao.observeByLocalId(localId).collect { user ->
                user?.let {
                    logger.i("Prefilling profile UI with user (localId=$localId, email=${it.email})")

                    val parts = it.name?.trim()?.split(" ") ?: emptyList()
                    firstName = parts.firstOrNull() ?: ""
                    lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""

                    pharmacyName = it.pharmacyName.orEmpty()
                    phoneNumber = it.phoneNumber.orEmpty()
                    email = it.email.orEmpty()
                    npi = it.npiId.orEmpty()
                    doNotAskAgain = preferenceHelper.isDoNotAskAgain()
                }
            }
        }
    }

    // ─────────────────────────── Validation ───────────────────────────

    /**
     * Validates all profile fields. Fields are optional,
     * but if filled they must be valid.
     *
     * @return true if all inputs are valid, false otherwise.
     */
    private fun validateInputs(): Boolean {
        firstNameError = validator.validateName(firstName).errorMessageResId
        lastNameError = validator.validateName(lastName).errorMessageResId
        pharmacyNameError = validator.validatePharmacyName(pharmacyName).errorMessageResId
        phoneError = validator.validatePhone(phoneNumber).errorMessageResId
        emailError = validator.validateEmail(email).errorMessageResId
        npiError = validator.validateNpi(npi).errorMessageResId

        return listOf(
            firstNameError,
            lastNameError,
            pharmacyNameError,
            phoneError,
            emailError,
            npiError
        ).all { it == null }
    }

    // ─────────────────────────── API Actions ───────────────────────────

    /**
     * Updates the user's profile after validation.
     */
    fun updateProfile() {
        if (!validateInputs()) {
            logger.w("Validation failed. Aborting update.")
            return
        }

        viewModelScope.launch {
            _updateUiState.value = ProfileUpdateUiState.Loading

            val request = ProfileUpdateRequest(
                fullName = "$firstName $lastName".trim(),
                pharmacyName = pharmacyName,
                phoneNumber = phoneNumber,
                npiId = npi,
                isProfileComplete = true,
                avatarUrl = "",
                notificationsEnabled = !doNotAskAgain,
                language = "en",
                timezone = "Asia/Kolkata"
            )

            repository.updateProfile(request)
                .onSuccess {
                    logger.i("Profile update success")

                    val localId = preferenceHelper.getLocalId()
                    if (localId != null && localId != 0L) {
                        val entity = UserEntity(
                            localId = localId,
                            userId = preferenceHelper.getUserId().orEmpty(),
                            email = email,
                            name = "$firstName $lastName".trim(),
                            phoneNumber = phoneNumber,
                            pharmacyName = pharmacyName,
                            npiId = npi,
                            notifications = !doNotAskAgain,
                            isVerified = true,
                            isProfileCompleted = true,
                            createdAt = System.currentTimeMillis()
                        )
                        userDao.update(entity)
                        logger.i("User entity updated in Room via localId=$localId")
                    }

                    preferenceHelper.saveDoNotAskAgain(doNotAskAgain)

                    _updateUiState.value = ProfileUpdateUiState.Success
                }
                .onFailure { e ->
                    logger.e("Profile update failed", e)
                    _updateUiState.value = ProfileUpdateUiState.Error(
                        e.message ?: "Profile update failed"
                    )
                }
        }
    }

    /**
     * Deletes the user's profile.
     *
     * Note: Local Room record is not deleted immediately
     * to prevent accidental data loss.
     */
    fun deleteProfile() {
        viewModelScope.launch {
            _deleteUiState.value = ProfileDeleteUiState.Loading

            repository.deleteProfile()
                .onSuccess {
                    logger.i("Profile delete success")
                    _deleteUiState.value = ProfileDeleteUiState.Success
                }
                .onFailure { e ->
                    logger.e("Profile delete failed", e)
                    _deleteUiState.value = ProfileDeleteUiState.Error(
                        e.message ?: "Profile deletion failed"
                    )
                }
        }
    }

    /** Resets profile update UI state back to [ProfileUpdateUiState.Idle]. */
    fun resetUpdateState() {
        _updateUiState.value = ProfileUpdateUiState.Idle
    }

    /** Resets profile delete UI state back to [ProfileDeleteUiState.Idle]. */
    fun resetDeleteState() {
        _deleteUiState.value = ProfileDeleteUiState.Idle
    }
}
