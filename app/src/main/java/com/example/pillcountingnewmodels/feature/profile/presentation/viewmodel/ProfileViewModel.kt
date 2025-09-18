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
 * ViewModel responsible for handling Profile screen state and business logic.
 *
 * This class:
 * - Prefills the profile form with user data from [UserDao].
 * - Handles profile update and delete operations via [ProfileRepository].
 * - Emits UI states ([ProfileUpdateUiState], [ProfileDeleteUiState]) to drive Compose UI.
 *
 * @property repository Repository for performing remote profile operations.
 * @property preferenceHelper Helper for accessing persisted preferences (e.g., userId).
 * @property userDao Local Room DAO for persisting and retrieving user data.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val preferenceHelper: PreferenceHelper,
    private val userDao: UserDao
) : ViewModel() {

    private val logger = AppLogger.create<ProfileViewModel>()

    // ─────────────────────────── UI States ───────────────────────────

    /** State flow for profile update requests. */
    private val _updateUiState = MutableStateFlow<ProfileUpdateUiState>(ProfileUpdateUiState.Idle)
    val updateUiState = _updateUiState.asStateFlow()

    /** State flow for profile delete requests. */
    private val _deleteUiState = MutableStateFlow<ProfileDeleteUiState>(ProfileDeleteUiState.Idle)
    val deleteUiState = _deleteUiState.asStateFlow()

    // ─────────────────────────── Profile Fields ───────────────────────────

    /** User's first name (editable). */
    var firstName by mutableStateOf("")

    /** User's last name (editable). */
    var lastName by mutableStateOf("")

    /** Pharmacy name associated with the user (editable). */
    var pharmacyName by mutableStateOf("")

    /** Contact phone number (editable). */
    var phoneNumber by mutableStateOf("")

    /** Email address (editable but typically non-changeable). */
    var email by mutableStateOf("")

    /** NPI (National Provider Identifier) or equivalent ID (editable). */
    var npi by mutableStateOf("")

    /** Whether the "do not ask again" flag is set (affects notifications). */
    var doNotAskAgain by mutableStateOf(false)

    init {
        // Load user data from local persistence and prefill fields
        val userId = preferenceHelper.getUserId()
        if (!userId.isNullOrBlank()) {
            observeUser(userId)
        } else {
            logger.w("No userId found in preferences — skipping prefill.")
        }
    }

    /**
     * Observes the [UserEntity] stored in Room for the given [userId].
     * Updates the form fields with values when available.
     *
     * @param userId Unique identifier of the user.
     */
    private fun observeUser(userId: String) {
        viewModelScope.launch {
            userDao.observeById(userId).collect { user ->
                user?.let {
                    logger.i("Prefilling profile UI with user: ${it.email}")

                    // Split full name into first + last
                    val parts = it.name?.trim()?.split(" ") ?: emptyList()
                    firstName = parts.firstOrNull() ?: ""
                    lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""

                    pharmacyName = it.pharmacyName.orEmpty()
                    phoneNumber = it.phoneNumber.orEmpty()
                    email = it.email.orEmpty()
                    npi = it.npiId.orEmpty()
                    doNotAskAgain = !(it.notifications ?: true)
                }
            }
        }
    }

    /**
     * Sends a profile update request to the backend and persists changes to Room on success.
     */
    fun updateProfile() {
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

                    // Persist changes locally
                    val userId = preferenceHelper.getUserId()
                    if (!userId.isNullOrBlank()) {
                        val entity = UserEntity(
                            userId = userId,
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
                        userDao.upsertPreservingLocalId(entity)
                        logger.i("User entity updated in Room.")
                    }

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
     * Sends a delete profile request to the backend.
     * Does not immediately delete from Room to avoid accidental local data loss
     * (can be extended if required).
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

    /** Resets the profile update UI state to idle. */
    fun resetUpdateState() {
        _updateUiState.value = ProfileUpdateUiState.Idle
    }

    /** Resets the profile delete UI state to idle. */
    fun resetDeleteState() {
        _deleteUiState.value = ProfileDeleteUiState.Idle
    }
}
