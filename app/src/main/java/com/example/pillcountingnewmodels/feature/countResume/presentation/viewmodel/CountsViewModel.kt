package com.example.pillcountingnewmodels.feature.countResume.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.core.room.models.CountType
import com.example.pillcountingnewmodels.feature.countResume.domain.data.FixedCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.RegularCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem
import com.example.pillcountingnewmodels.feature.countResume.domain.model.FixedCountsUiState
import com.example.pillcountingnewmodels.feature.countResume.domain.model.RegularCountsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel to manage both Fixed and Regular pill counts.
 *
 * Responsibilities:
 * - Observe [PillCountTxnDao] for real-time updates of transactions.
 * - Split transactions into Fixed and Regular categories.
 * - Handle multi-select operations (toggle, close, delete).
 * - Map database models into lightweight [CountItem]s for UI.
 */
@HiltViewModel
class CountsViewModel @Inject constructor(
    private val pillCountTxnDao: PillCountTxnDao
) : ViewModel() {

    /* ------------------------- State Flows ------------------------- */

    /** Backing state for fixed count transactions. */
    private val _fixedUiState = MutableStateFlow(FixedCountsUiState())
    /** Public immutable state for UI to observe. */
    val fixedUiState: StateFlow<FixedCountsUiState> = _fixedUiState.asStateFlow()

    /** Backing state for regular count transactions. */
    private val _regularUiState = MutableStateFlow(RegularCountsUiState())
    /** Public immutable state for UI to observe. */
    val regularUiState: StateFlow<RegularCountsUiState> = _regularUiState.asStateFlow()

    /** Formatter for displaying human-readable dates from epoch millis. */
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())

    init {
        observeFixedCounts()
        observeRegularCounts()
    }

    /* ------------------------- Event Dispatchers ------------------------- */

    /**
     * Handle UI events for Fixed Counts section.
     */
    fun onFixedEvent(event: FixedCountsEvent) {
        when (event) {
            is FixedCountsEvent.ItemSwipedToDelete -> deleteFixedItem(event.item)
            is FixedCountsEvent.SelectItem -> toggleFixedSelection(event.item)
            FixedCountsEvent.DeleteClicked -> handleFixedDeleteClick()
            FixedCountsEvent.ToggleMultiSelectMode -> toggleFixedMultiSelectMode()
            FixedCountsEvent.CloseMultiSelectMode -> closeFixedMultiSelectMode()
        }
    }

    /**
     * Handle UI events for Regular Counts section.
     */
    fun onRegularEvent(event: RegularCountsEvent) {
        when (event) {
            is RegularCountsEvent.ItemSwipedToDelete -> deleteRegularItem(event.item)
            is RegularCountsEvent.SelectItem -> toggleRegularSelection(event.item)
            RegularCountsEvent.DeleteClicked -> handleRegularDeleteClick()
            RegularCountsEvent.ToggleMultiSelectMode -> toggleRegularMultiSelectMode()
            RegularCountsEvent.CloseMultiSelectMode -> closeRegularMultiSelectMode()
        }
    }

    /* ------------------------- Fixed Counts Logic ------------------------- */

    /**
     * Observes active transactions in DB and maps those with [CountType.FIXED].
     */
    private fun observeFixedCounts() {
        viewModelScope.launch {
            pillCountTxnDao.observeAllActive()
                .map { txns ->
                    txns.filter { it.countType == CountType.FIXED }
                        .map {
                            CountItem(
                                id = it.txnId,
                                name = it.note ?: "Unnamed Fixed Count",
                                quantity = it.targetCount ?: 0,
                                date = formatDate(it.createdAt)
                            )
                        }
                }
                .catch { e -> e.printStackTrace() } // log & continue
                .collect { items ->
                    _fixedUiState.update { it.copy(fixedCounts = items) }
                }
        }
    }

    /**
     * Toggle selection of a [CountItem] in Fixed Counts.
     */
    private fun toggleFixedSelection(item: CountItem) {
        val selected = _fixedUiState.value.selectedItems.toMutableList()
        if (selected.contains(item)) selected.remove(item) else selected.add(item)
        _fixedUiState.update { it.copy(selectedItems = selected) }
    }

    /**
     * Handle delete button click:
     * - If multi-select active → delete selected items.
     * - Else → toggle multi-select mode.
     */
    private fun handleFixedDeleteClick() {
        if (_fixedUiState.value.isMultiSelectMode) {
            val toDelete = _fixedUiState.value.selectedItems
            viewModelScope.launch {
                toDelete.forEach { pillCountTxnDao.softDelete(it.id) }
            }
            _fixedUiState.update {
                it.copy(selectedItems = emptyList(), isMultiSelectMode = false)
            }
        } else {
            toggleFixedMultiSelectMode()
        }
    }

    /**
     * Soft delete a single Fixed Count item in DB.
     */
    private fun deleteFixedItem(item: CountItem) {
        viewModelScope.launch {
            try {
                pillCountTxnDao.softDelete(item.id)
            } catch (e: Exception) {
                if (e !is CancellationException) e.printStackTrace()
            }
        }
    }

    /** Toggle multi-select mode for Fixed Counts. */
    private fun toggleFixedMultiSelectMode() {
        _fixedUiState.update { it.copy(isMultiSelectMode = !it.isMultiSelectMode) }
    }

    /** Close multi-select mode and clear selection for Fixed Counts. */
    private fun closeFixedMultiSelectMode() {
        _fixedUiState.update { it.copy(isMultiSelectMode = false, selectedItems = emptyList()) }
    }

    /* ------------------------- Regular Counts Logic ------------------------- */

    /**
     * Observes active transactions in DB and maps those with [CountType.REGULAR].
     */
    private fun observeRegularCounts() {
        viewModelScope.launch {
            pillCountTxnDao.observeAllActive()
                .map { txns ->
                    txns.filter { it.countType == CountType.REGULAR }
                        .map {
                            CountItem(
                                id = it.txnId,
                                name = it.note ?: "Unnamed Regular Count",
                                quantity = it.targetCount ?: 0,
                                date = formatDate(it.createdAt)
                            )
                        }
                }
                .catch { e -> e.printStackTrace() }
                .collect { items ->
                    _regularUiState.update { it.copy(regularCounts = items) }
                }
        }
    }

    /**
     * Toggle selection of a [CountItem] in Regular Counts.
     */
    private fun toggleRegularSelection(item: CountItem) {
        val selected = _regularUiState.value.selectedItems.toMutableList()
        if (selected.contains(item)) selected.remove(item) else selected.add(item)
        _regularUiState.update { it.copy(selectedItems = selected) }
    }

    /**
     * Handle delete button click for Regular Counts.
     */
    private fun handleRegularDeleteClick() {
        if (_regularUiState.value.isMultiSelectMode) {
            val toDelete = _regularUiState.value.selectedItems
            viewModelScope.launch {
                toDelete.forEach { pillCountTxnDao.softDelete(it.id) }
            }
            _regularUiState.update {
                it.copy(selectedItems = emptyList(), isMultiSelectMode = false)
            }
        } else {
            toggleRegularMultiSelectMode()
        }
    }

    /**
     * Soft delete a single Regular Count item in DB.
     */
    private fun deleteRegularItem(item: CountItem) {
        viewModelScope.launch {
            try {
                pillCountTxnDao.softDelete(item.id)
            } catch (e: Exception) {
                if (e !is CancellationException) e.printStackTrace()
            }
        }
    }

    /** Toggle multi-select mode for Regular Counts. */
    private fun toggleRegularMultiSelectMode() {
        _regularUiState.update { it.copy(isMultiSelectMode = !_regularUiState.value.isMultiSelectMode) }
    }

    /** Close multi-select mode and clear selection for Regular Counts. */
    private fun closeRegularMultiSelectMode() {
        _regularUiState.update { it.copy(isMultiSelectMode = false, selectedItems = emptyList()) }
    }

    /**
     * Convert epoch millis into formatted date string.
     *
     * @param millis Timestamp to format.
     * @return Human-readable formatted date, or "-" if invalid.
     */
    private fun formatDate(millis: Long?): String {
        return if (millis != null && millis > 0) {
            try {
                dateFormatter.format(Date(millis))
            } catch (e: Exception) {
                "-"
            }
        } else "-"
    }
}
