package com.example.pillcountingnewmodels.feature.countResume.domain.data

import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem

/**
 * Factory interface for creating [ResumeEvent] instances.
 *
 * This allows mapping UI interactions to specific events in a type-safe manner.
 *
 * @param E The type of [ResumeEvent] this factory produces.
 */
interface ResumeEventFactory<E : ResumeEvent> {

    /** Creates an event to toggle multi-select mode on/off. */
    fun toggleMultiSelectMode(): E

    /** Creates an event to close multi-select mode. */
    fun closeMultiSelectMode(): E

    /** Creates an event when the delete action is triggered. */
    fun deleteClicked(): E

    /**
     * Creates an event for deleting a specific item via swipe gesture.
     *
     * @param item The [CountItem] to be deleted.
     */
    fun itemSwipedToDelete(item: CountItem): E

    /**
     * Creates an event for selecting or deselecting a specific item in multi-select mode.
     *
     * @param item The [CountItem] being selected/deselected.
     */
    fun selectItem(item: CountItem): E

    fun resumeTransaction(item: CountItem): E
}
