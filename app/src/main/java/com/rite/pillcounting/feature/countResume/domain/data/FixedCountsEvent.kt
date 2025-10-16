package com.rite.pillcounting.feature.countResume.domain.data

import com.rite.pillcounting.feature.countResume.domain.model.CountItem

/**
 * Represents all user interactions and actions that can occur on the [FixedCountsEvent].
 *
 * Each event corresponds to a specific UI action, such as selecting an item,
 * toggling multi-select mode, or deleting an item.
 */
sealed interface FixedCountsEvent : ResumeEvent {

    /** Triggered when the user toggles the multi-select mode on/off. */
    object ToggleMultiSelectMode : FixedCountsEvent

    /** Triggered when the user closes the multi-select mode. */
    object CloseMultiSelectMode : FixedCountsEvent

    /** Triggered when the user clicks the delete button to remove selected items. */
    object DeleteClicked : FixedCountsEvent

    /**
     * Triggered when the user swipes a single item to delete it.
     *
     * @param item The [CountItem] that was swiped.
     */
    data class ItemSwipedToDelete(val item: CountItem) : FixedCountsEvent
    data class ForceCompleteTransaction(val item: CountItem) : FixedCountsEvent

    /**
     * Triggered when the user selects or deselects an item in multi-select mode.
     *
     * @param item The [CountItem] that was selected/deselected.
     */
    data class SelectItem(val item: CountItem) : FixedCountsEvent

    data class resumeTransaction(val item: CountItem) : FixedCountsEvent
}
