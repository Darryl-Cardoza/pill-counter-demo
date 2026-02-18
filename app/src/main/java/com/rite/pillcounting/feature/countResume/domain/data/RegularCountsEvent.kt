package com.rite.pillcounting.feature.countResume.domain.data

import com.rite.pillcounting.feature.countResume.domain.model.CountItem

/**
 * Represents all user interactions and actions that can occur on the [RegularCountsEvent].
 *
 * Each event corresponds to a specific UI action, such as selecting an item,
 * toggling multi-select mode, or deleting an item.
 */
sealed interface RegularCountsEvent : ResumeEvent {

    /** Triggered when the user toggles the multi-select mode on/off. */
    object ToggleMultiSelectMode : RegularCountsEvent

    /** Triggered when the user closes the multi-select mode. */
    object CloseMultiSelectMode : RegularCountsEvent

    /** Triggered when the user clicks the delete button to remove selected items. */
    object DeleteClicked : RegularCountsEvent

    /** when user select all txn **/
    object SelectAllClicked: RegularCountsEvent

    /**
     * Triggered when the user swipes a single item to delete it.
     *
     * @param item The [CountItem] that was swiped.
     */
    data class ItemSwipedToDelete(val item: CountItem) : RegularCountsEvent

    data class ForceCompleteTransaction(val item: CountItem) : RegularCountsEvent

    /**
     * Triggered when the user selects or deselects an item in multi-select mode.
     *
     * @param item The [CountItem] that was selected/deselected.
     */
    data class SelectItem(val item: CountItem) : RegularCountsEvent

    data class resumeTransaction(val item: CountItem) : RegularCountsEvent
}
