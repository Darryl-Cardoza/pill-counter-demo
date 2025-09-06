package com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow() // Expose as an immutable StateFlow

    init {
        // In a real application, you would fetch data from a repository here.
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Simulate fetching data from a source
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