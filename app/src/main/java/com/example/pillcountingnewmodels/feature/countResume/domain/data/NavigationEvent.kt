package com.example.pillcountingnewmodels.feature.countResume.domain.data

import com.example.pillcountingnewmodels.core.room.models.enums.CountType

/**
 * Defines one-time navigation events sent from the ViewModel to the UI.
 */
sealed interface NavigationEvent {
    data class NavigateToPillCount(val countType: CountType) : NavigationEvent
    data object NavigateBack : NavigationEvent
}