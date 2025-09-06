package com.example.pillcountingnewmodels.feature.dashboard.domain.model

data class DashboardUiState(
    val completedFixedCount: String = "0",
    val partialFixedCount: String = "0",
    val completedRegularCount: String = "0",
    val partialRegularCount: String = "0"
)
