package com.example.pillcountingnewmodels.feature.profile.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.UserDao
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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val preferenceHelper: PreferenceHelper,
    private val userDao: UserDao
) : ViewModel() {

    private val logger = AppLogger.create<ProfileViewModel>()

    // Profile update UI state
    private val _updateUiState = MutableStateFlow<ProfileUpdateUiState>(ProfileUpdateUiState.Idle)
    val updateUiState = _updateUiState.asStateFlow()

    // Profile delete UI state
    private val _deleteUiState = MutableStateFlow<ProfileDeleteUiState>(ProfileDeleteUiState.Idle)
    val deleteUiState = _deleteUiState.asStateFlow()

    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var pharmacyName by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var email by mutableStateOf("")
    var npi by mutableStateOf("")
    var doNotAskAgain by mutableStateOf(false)

    /**
     * Update profile API call.
     */
    fun updateProfile() {
        viewModelScope.launch {
            _updateUiState.value = ProfileUpdateUiState.Loading

            val request = ProfileUpdateRequest(
                fullName = "$firstName $lastName",
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
     * Delete profile API call.
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

    fun resetUpdateState() {
        _updateUiState.value = ProfileUpdateUiState.Idle
    }

    fun resetDeleteState() {
        _deleteUiState.value = ProfileDeleteUiState.Idle
    }
}

