package com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.dashboard.domain.data.IUserDetailRepository
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state of the Dashboard screen,
 * including fetching user details and dashboard metrics.
 *
 * @property userDetailRepository Repository to fetch user details.
 * @property preferenceHelper Helper to retrieve stored access tokens.
 */
class DashboardViewModel @Inject constructor(
    private val userDetailRepository: IUserDetailRepository,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
        fetchUserDetail()
    }

    /**
     * Loads mock dashboard data into the UI state.
     *
     * TODO: Replace with real data fetch from repository or use case.
     */
    private fun loadDashboardData() {
        _uiState.update {
            it.copy(
                completedFixedCount = "0",
                partialFixedCount = "0",
                completedRegularCount = "0",
                partialRegularCount = "0"
            )
        }
    }

    /**
     * Retrieves the access token from preferences and requests
     * the user details from the repository. Updates the UI state
     * with the loading status, success result, or error message.
     */
    fun fetchUserDetail() {
        viewModelScope.launch {
            val token = preferenceHelper.getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoadingUserDetail = false,
                        userDetailError = "Access token not found"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoadingUserDetail = true, userDetailError = null) }

            val result = userDetailRepository.getUserDetail(token)

            result.fold(
                onSuccess = { userDetail ->
                    _uiState.update {
                        it.copy(
                            userDetail = userDetail,
                            isLoadingUserDetail = false,
                            userDetailError = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            userDetail = null,
                            isLoadingUserDetail = false,
                            userDetailError = error.message ?: "Unknown error occurred"
                        )
                    }
                }
            )
        }
    }
}
