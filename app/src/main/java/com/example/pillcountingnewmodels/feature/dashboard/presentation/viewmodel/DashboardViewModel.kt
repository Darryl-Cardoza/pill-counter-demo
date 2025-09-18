package com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.core.room.dao.UserDao
import com.example.pillcountingnewmodels.core.room.models.CountStatus
import com.example.pillcountingnewmodels.core.room.models.CountType
import com.example.pillcountingnewmodels.core.room.models.UserEntity
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.dashboard.domain.data.IUserDetailRepository
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.DashboardUiState
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.UserDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userDetailRepository: IUserDetailRepository,
    private val preferenceHelper: PreferenceHelper,
    private val userDao: UserDao,
    private val pillCountTxnDao: PillCountTxnDao
) : ViewModel() {

    private val logger = AppLogger.create<DashboardViewModel>()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        logger.i("DashboardViewModel initialized.")
        loadDashboardData()
        fetchUserDetail()
    }

    /**
     * Load dashboard counters from the database.
     */
    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            pillCountTxnDao.observeDashboardCountsGrouped().collect { rows ->
                val completedFixed =
                    rows.firstOrNull { it.status == CountStatus.COMPLETED && it.countType == CountType.FIXED }?.cnt
                        ?: 0
                val partialFixed =
                    rows.firstOrNull { it.status == CountStatus.PARTIAL && it.countType == CountType.FIXED }?.cnt
                        ?: 0
                val completedRegular =
                    rows.firstOrNull { it.status == CountStatus.COMPLETED && it.countType == CountType.REGULAR }?.cnt
                        ?: 0
                val partialRegular =
                    rows.firstOrNull { it.status == CountStatus.PARTIAL && it.countType == CountType.REGULAR }?.cnt
                        ?: 0

                _uiState.update {
                    it.copy(
                        completedFixedCount = completedFixed.toString(),
                        partialFixedCount = partialFixed.toString(),
                        completedRegularCount = completedRegular.toString(),
                        partialRegularCount = partialRegular.toString()
                    )
                }
            }
        }
    }


    fun fetchUserDetail() {
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
                        // Coerce payload to a single UI type (UserDetail?)
                        val uiUser: UserDetail? = when (payload) {
                            else -> payload.data
                        }

                        // Persist only if we actually have user data
                        uiUser?.let { detail ->
                            val entity = detail.toUserEntity(jwtUserId = uiUser.profile?.userId)
                            preferenceHelper.saveUserId(entity.userId)
                            userDao.upsertPreservingLocalId(user = entity)
                            logger.i(message = "User detail persisted locally.")
                        }

                        _uiState.update {
                            it.copy(
                                userDetail = uiUser,
                                isLoadingUserDetail = false,
                                userDetailError = null
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
                    _uiState.update {
                        it.copy(
                            userDetail = null,
                            isLoadingUserDetail = false,
                            userDetailError = error.message ?: "An unknown error occurred"
                        )
                    }
                }
            )
        }
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
        email = this.profile?.email,
        name = this.profile?.fullName,
        phoneNumber = this.profile?.phoneNumber,
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

