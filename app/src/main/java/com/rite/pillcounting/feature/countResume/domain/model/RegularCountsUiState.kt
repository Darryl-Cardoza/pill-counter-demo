package com.rite.pillcounting.feature.countResume.domain.model

import com.rite.pillcounting.feature.countResume.domain.data.ResumeUiState

/**
 * Represents the UI state for the Regular Count Resume screen.
 *
 * This class encapsulates all information needed to render the regular count list,
 * handle multi-selection, and track user interactions.
 *
 * @property regularCounts The complete list of regular count items displayed on the screen.
 * @property isMultiSelectMode Flag indicating whether multi-select mode is currently active.
 * @property selectedItems List of items currently selected by the user.
 * @property partialCounts All partial count items, inherited from [ResumeUiState].
 */
data class RegularCountsUiState(
    override val isMultiSelectMode: Boolean = false,
    override val selectedItems: List<CountItem> = emptyList(),
    override val partialCounts: List<CountItem> = emptyList(),
    val regularCounts: List<CountItem> = emptyList(),
) : ResumeUiState
