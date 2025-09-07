package com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel responsible for managing and exposing the state of the Dashboard screen.
 *
 * Responsibilities:
 * - Holds the current [DashboardUiState].
 * - Fetches and updates dashboard metrics (completed/partial counts).
 * - Exposes immutable state to the UI layer via [uiState].
 *
 * Note:
 * In a production environment, data would typically be fetched from a repository
 * (network, database, or a combination). Currently, it uses mock values.
 */
class DashboardViewModel : ViewModel() {

    // Backing state for the Dashboard screen
    private val _uiState = MutableStateFlow(DashboardUiState())

    /**
     * Publicly exposed immutable state for UI consumption.
     * UI layers should collect this StateFlow to render updates reactively.
     */
    val uiState = _uiState.asStateFlow()

    init {
        // Initialize dashboard data when ViewModel is created
        loadDashboardData()
    }

    /**
     * Loads dashboard data.
     *
     * TODO:
     * Replace with repository calls or use cases when integrating with real data sources.
     */
    private fun loadDashboardData() {
        // Mocked data (for now). Replace with actual repository call.
        _uiState.update {
            it.copy(
                completedFixedCount = "12",
                partialFixedCount = "3",
                completedRegularCount = "45",
                partialRegularCount = "8"
            )
        }
    }
}
