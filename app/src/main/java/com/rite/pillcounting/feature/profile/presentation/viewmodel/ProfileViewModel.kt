package com.rite.pillcounting.feature.profile.presentation.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rite.pillcounting.R
import com.rite.pillcounting.core.models.ErrorResponse
import com.rite.pillcounting.core.room.dao.UserDao
import com.rite.pillcounting.core.room.models.UserEntity
import com.rite.pillcounting.core.utils.common.HelperFunctions.plain
import com.rite.pillcounting.core.utils.common.HelperFunctions.secure
import com.rite.pillcounting.core.utils.common.NetworkUtils
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import com.rite.pillcounting.core.utils.validator.CredentialsValidator
import com.rite.pillcounting.feature.profile.data.ProfileRepository
import com.rite.pillcounting.feature.profile.domain.model.ProfileDeleteUiState
import com.rite.pillcounting.feature.profile.domain.model.ProfileUpdateRequest
import com.rite.pillcounting.feature.profile.domain.model.ProfileUpdateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * ViewModel for managing Profile UI state, validation, and business logic.
 *
 * Responsibilities:
 * - Prefill the profile form from [UserDao].
 * - Validate fields using [CredentialsValidator].
 * - Handle profile update and delete operations via [ProfileRepository].
 * - Manage state flows ([ProfileUpdateUiState], [ProfileDeleteUiState]) for Compose UI.
 * - Provide user-friendly network and API error messages.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val preferenceHelper: PreferenceHelper,
    private val userDao: UserDao,
    private val validator: CredentialsValidator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val logger = AppLogger.create<ProfileViewModel>()

    // ─────────────────────────── UI States ───────────────────────────
    private val _updateUiState = MutableStateFlow<ProfileUpdateUiState>(ProfileUpdateUiState.Idle)
    val updateUiState = _updateUiState.asStateFlow()

    private val _deleteUiState = MutableStateFlow<ProfileDeleteUiState>(ProfileDeleteUiState.Idle)
    val deleteUiState = _deleteUiState.asStateFlow()

    // ─────────────────────────── Profile Fields ───────────────────────────
    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var pharmacyName by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var email by mutableStateOf("")
    var npi by mutableStateOf("")
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

        doNotAskAgain = preferenceHelper.isDoNotAskAgain()
        logger.i("Initialized doNotAskAgain = $doNotAskAgain")
    }

    fun toggleDoNotAskAgain(value: Boolean) {
        doNotAskAgain = value
        preferenceHelper.saveDoNotAskAgain(value)
        logger.i("DoNotAskAgain updated → $value")
    }

    private fun observeUser(localId: Long) {
        viewModelScope.launch {
            userDao.observeByLocalId(localId).collect { user ->
                user?.let {
                    logger.i("Prefilling profile UI with user (localId=$localId, email=${it.email})")

                    val parts = it.name?.trim()?.split(" ") ?: emptyList()
                    firstName = parts.firstOrNull() ?: ""
                    lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
                    pharmacyName = it.pharmacyName.orEmpty()
                    phoneNumber = it.phoneNumber.plain().orEmpty()
                    email = it.email.plain().orEmpty()
                    npi = it.npiId.orEmpty()
                    doNotAskAgain = preferenceHelper.isDoNotAskAgain()
                }
            }
        }
    }

    fun onPhoneChanged(input: String) {
        val digits = input.filter { it.isDigit() }

        val limited = digits.take(10)

        phoneNumber = limited
    }

    private val allowedNameChars = Regex("[\\p{L} '-]")

    fun onFirstNameChanged(input: String) {
        firstName = input
            .filter { it.toString().matches(allowedNameChars) }
            .take(50)
    }

    fun onLastNameChanged(input: String) {
        lastName = input
            .filter { it.toString().matches(allowedNameChars) }
            .take(50)
    }
    // ─────────────────────────── Validation ───────────────────────────
    private fun validateInputs(): Boolean {
        firstNameError = validator.validateName(firstName).errorMessageResId
        lastNameError = validator.validateName(lastName).errorMessageResId
        pharmacyNameError = validator.validatePharmacyName(pharmacyName).errorMessageResId
        phoneError = validator.validatePhone(phoneNumber).errorMessageResId
        emailError = validator.validateEmail(email).errorMessageResId
        npiError = validator.validateNpi(npi).errorMessageResId

        return listOf(
            firstNameError, lastNameError, pharmacyNameError,
            phoneError, emailError, npiError
        ).all { it == null }
    }

    // ─────────────────────────── API Actions ───────────────────────────
        fun updateProfile() {
            if (!validateInputs()) {
                logger.w("Validation failed. Aborting update.")
                return
            }

            // Network check using NetworkUtils
            if (!NetworkUtils.isNetworkAvailable(context)) {
                _updateUiState.value =
                    ProfileUpdateUiState.Error(context.getString(R.string.error_no_internet))
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
                                email = email.secure(),
                                name = "$firstName $lastName".trim(),
                                phoneNumber = phoneNumber.secure(),
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
                        _updateUiState.value =
                            ProfileUpdateUiState.Error(getFriendlyErrorMessage(e))
                    }
            }
        }

        fun deleteProfile() {
            // Check internet before delete
            if (!NetworkUtils.isNetworkAvailable(context)) {
                _deleteUiState.value =
                    ProfileDeleteUiState.Error(context.getString(R.string.error_no_internet))
                return
            }

            viewModelScope.launch {
                _deleteUiState.value = ProfileDeleteUiState.Loading

                repository.deleteProfile()
                    .onSuccess {
                        logger.i("Profile delete success")
                        _deleteUiState.value = ProfileDeleteUiState.Success
                    }
                    .onFailure { e ->
                        logger.e("Profile delete failed", e)
                        _deleteUiState.value =
                            ProfileDeleteUiState.Error(getFriendlyErrorMessage(e))
                    }
            }
        }

    // ─────────────────────────── Friendly Error Mapping ───────────────────────────
    private fun getFriendlyErrorMessage(exception: Throwable): String {
        return when (exception) {
            is HttpException -> {
                val errorBody = exception.response()?.errorBody()?.string()
                val parsedMessage = errorBody?.let {
                    try {
                        val errorResponse = Gson().fromJson(it, ErrorResponse::class.java)
                        errorResponse.message
                    } catch (e: Exception) {
                        logger.e("Failed to parse error response", e)
                        null
                    }
                }
                when (exception.code()) {
                    400 -> parsedMessage ?: context.getString(R.string.error_invalid_input)
                    401 -> context.getString(R.string.error_unauthorized)
                    404 -> context.getString(R.string.error_not_found)
                    500 -> context.getString(R.string.error_server_unavailable)
                    else -> parsedMessage ?: context.getString(R.string.error_generic)
                }
            }

            is UnknownHostException -> context.getString(R.string.error_no_internet)
            is SocketTimeoutException -> context.getString(R.string.error_timeout)
            else -> exception.message?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.error_generic)
        }
    }

    // ─────────────────────────── State Reset ───────────────────────────
    fun resetUpdateState() {
        _updateUiState.value = ProfileUpdateUiState.Idle
    }

    fun resetDeleteState() {
        _deleteUiState.value = ProfileDeleteUiState.Idle
    }
}
