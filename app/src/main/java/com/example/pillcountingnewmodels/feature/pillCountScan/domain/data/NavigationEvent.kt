package com.example.pillcountingnewmodels.feature.pillCountScan.domain.data

sealed interface NavigationEvent {

    data object NavigateToDashboard : NavigationEvent
}