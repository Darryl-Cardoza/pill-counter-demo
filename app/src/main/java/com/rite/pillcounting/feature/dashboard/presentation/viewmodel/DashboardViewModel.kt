package com.rite.pillcounting.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rite.pillcounting.core.room.dao.PillCountTxnDao
import com.rite.pillcounting.core.room.dao.UserDao
import com.rite.pillcounting.core.room.models.UserEntity
import com.rite.pillcounting.core.utils.common.HelperFunctions.mapCounts
import com.rite.pillcounting.core.utils.common.HelperFunctions.secure
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import com.rite.pillcounting.feature.dashboard.domain.data.IUserDetailRepository
import com.rite.pillcounting.feature.dashboard.domain.model.DashboardUiState
import com.rite.pillcounting.feature.dashboard.domain.model.UserDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Dashboard screen.
 *
 * ### Responsibilities
 * - Observe pill count transaction statistics from [PillCountTxnDao].
 * - Map database counts into dashboard-friendly values (Fixed/Regular, Completed/Partial).
 * - Fetch user profile details from [IUserDetailRepository] using an access token.
 * - Persist user details into Room via [UserDao].
 * - Keep preferences ([PreferenceHelper]) up-to-date with userId and localId.
 * - Expose navigation flag when profile is incomplete (to redirect user to Profile screen).
 *
 * ### Threading
 * - Database operations are executed on `Dispatchers.IO`.
 * - Results are mapped and posted to UI state using [MutableStateFlow].
 *
 * ### Logging
 * - All lifecycle and error events are logged using [AppLogger].
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userDetailRepository: IUserDetailRepository,
    private val preferenceHelper: PreferenceHelper,
    private val userDao: UserDao,
    private val pillCountTxnDao: PillCountTxnDao
) : ViewModel() {

    /** Logger instance for this ViewModel. */
    private val logger = AppLogger.create<DashboardViewModel>()

    /** Backing state flow for the Dashboard UI. */
    private val _uiState = MutableStateFlow(DashboardUiState())

    /** Public immutable UI state exposed to the UI layer. */
    val uiState = _uiState.asStateFlow()

    init {
        logger.i("DashboardViewModel initialized.")
        observeDashboardCounts()
        fetchUserDetail()
    }

    /**
     * Observe aggregated transaction counts and update the dashboard UI state.
     *
     * Counts are grouped by [CountType] and [CountStatus] (Completed/Partial).
     * Uses [mapCounts] to transform database rows into strongly typed buckets.
     */
    private fun observeDashboardCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            pillCountTxnDao.observeDashboardCountsGrouped().collect { rows ->
                val counts = mapCounts(rows)
                _uiState.update {
                    it.copy(
                        completedFixedCount = counts.fixedCompleted.toString(),
                        partialFixedCount = counts.fixedPartial.toString(),
                        completedRegularCount = counts.regularCompleted.toString(),
                        partialRegularCount = counts.regularPartial.toString()
                    )
                }
            }
        }
    }

    /**
     * Fetch the latest user details from the remote repository.
     *
     * - Reads the access token from [PreferenceHelper].
     * - Requests user details via [IUserDetailRepository].
     * - Persists the user profile into [UserDao].
     * - Saves `userId` and `localId` into [PreferenceHelper] for later use.
     * - Updates [DashboardUiState] with either success or error state.
     * - Sets [DashboardUiState.navigateToProfile] to `true` if profile is incomplete.
     */
    private fun fetchUserDetail() {
        viewModelScope.launch(Dispatchers.IO) {
            logger.d("Starting fetchUserDetail()")

            val token = preferenceHelper.getAccessToken()
            if (token.isNullOrBlank()) {
                logger.e("Access token not found in preferences.")
                _uiState.update {
                    it.copy(
                        isLoadingUserDetail = false,
                        userDetailError = "Access token not found"
                    )
                }
                return@launch
            }

            logger.i("Access token retrieved. Requesting user detail from repository.")
            _uiState.update { it.copy(isLoadingUserDetail = true, userDetailError = null) }

            val result = userDetailRepository.getUserDetail(token)
            result.fold(
                onSuccess = { payload ->
                    logger.i("User detail fetch successful. Persisting to Room...")
                    try {
                        val uiUser: UserDetail? = payload.data
                        uiUser?.let { detail ->
                            val entity = detail.toUserEntity(jwtUserId = uiUser.profile?.userId)
                            val localId = userDao.upsertPreservingLocalId(user = entity)
                            preferenceHelper.saveUserId(entity.userId)
                            preferenceHelper.saveLocalId(localId)
                            logger.i("User persisted locally with localId=$localId")
                        }

                        // Check if profile is incomplete
                        val isProfileIncomplete = uiUser?.profile?.isProfileCompleted == false

                        _uiState.update {
                            it.copy(
                                userDetail = uiUser,
                                isLoadingUserDetail = false,
                                userDetailError = null,
                                navigateToProfile = isProfileIncomplete
                            )
                        }
                    } catch (dbErr: Throwable) {
                        logger.e("Persisting user detail failed.", dbErr)
                        _uiState.update {
                            it.copy(
                                isLoadingUserDetail = false,
                                userDetailError = dbErr.message ?: "Failed to persist user detail"
                            )
                        }
                    }
                },
                onFailure = { error ->
                    logger.e("Failed to fetch user details.", error)
                    if (error.message == "LOGOUT") {
                        _uiState.update {
                            it.copy(
                                logoutUser = true,
                                isLoadingUserDetail = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                userDetail = null,
                                isLoadingUserDetail = false,
                                userDetailError = error.message ?: "An unknown error occurred"
                            )
                        }
                    }
                }
            )
        }
    }

    /** Resets the navigateToProfile flag after navigation. */
    fun resetNavigateToProfile() {
        _uiState.update { it.copy(navigateToProfile = false) }
    }

    fun saveTxnId(){
        preferenceHelper.saveTxnId(0)
    }
}

/* ───────────────────────────── Mappers ───────────────────────────── */

/**
 * Map API payload [UserDetail] to persistence [UserEntity].
 * Uses [jwtUserId] (from JWT) as the Room primary key; falls back to email if missing.
 */
private fun UserDetail.toUserEntity(jwtUserId: String?): UserEntity {
    val pk = jwtUserId ?: this.profile?.email.orEmpty()
    return UserEntity(
        userId = pk,
        email = this.profile?.email?.secure(),
        name = this.profile?.fullName,
        phoneNumber = this.profile?.phoneNumber?.secure(),
        avatarUrl = this.profile?.avatarUrl,
        role = this.profile?.role?.name,
        isVerified = this.profile?.isVerified ?: false,
        isProfileCompleted = this.profile?.isProfileCompleted,
        pharmacyName = this.profile?.pharmacyName,
        npiId = this.profile?.npiId,
        language = this.settings?.language,
        timezone = this.settings?.timezone,
        notifications = this.settings?.notificationsEnabled,
        createdAt = System.currentTimeMillis()
    )
}
