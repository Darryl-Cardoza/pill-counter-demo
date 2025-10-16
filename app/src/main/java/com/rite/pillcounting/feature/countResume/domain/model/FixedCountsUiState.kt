package com.rite.pillcounting.feature.countResume.domain.model

import com.rite.pillcounting.feature.countResume.domain.data.ResumeUiState

/**
 * Represents the UI state for the Fixed Count Resume screen.
 *
 * This class holds all necessary information to render the fixed count list,
 * manage selection mode, and track user interactions.
 *
 * @property fixedCounts The complete list of fixed count items displayed on the screen.
 * @property isMultiSelectMode Flag indicating whether multi-select mode is currently active.
 * @property selectedItems List of items currently selected by the user.
 * @property partialCounts All partial count items, inherited from [ResumeUiState].
 */
data class FixedCountsUiState(
    override val isMultiSelectMode: Boolean = false,
    override val selectedItems: List<CountItem> = emptyList(),
    override val partialCounts: List<CountItem> = emptyList(),
    val fixedCounts: List<CountItem> = emptyList()
) : ResumeUiState
