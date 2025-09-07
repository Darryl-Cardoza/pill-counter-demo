package com.example.pillcountingnewmodels.feature.countResume.domain.data

import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem

/**
 * Represents the common state for any partial count resume screen.
 *
 * Encapsulates information about multi-select mode, selected items,
 * and the list of items being displayed.
 */
interface ResumeUiState {

    /** Indicates whether multi-select mode is currently active. */
    val isMultiSelectMode: Boolean

    /** The list of items currently selected in multi-select mode. */
    val selectedItems: List<CountItem>

    /** The list of all items displayed in the partial count list. */
    val partialCounts: List<CountItem>
}

/**
 * Marker interface for events that can occur on a Resume screen.
 *
 * All user interactions like selection, deletion, and swipe actions
 * should implement this interface.
 */
interface ResumeEvent
