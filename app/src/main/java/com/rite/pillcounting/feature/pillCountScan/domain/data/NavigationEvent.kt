package com.rite.pillcounting.feature.pillCountScan.domain.data

sealed interface NavigationEvent {

    data object NavigateToDashboard : NavigationEvent
}