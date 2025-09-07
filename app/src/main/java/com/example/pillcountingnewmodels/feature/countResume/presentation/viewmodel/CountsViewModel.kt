package com.example.pillcountingnewmodels.feature.counts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.feature.countResume.domain.data.FixedCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.data.RegularCountsEvent
import com.example.pillcountingnewmodels.feature.countResume.domain.model.CountItem
import com.example.pillcountingnewmodels.feature.countResume.domain.model.FixedCountsUiState
import com.example.pillcountingnewmodels.feature.countResume.domain.model.RegularCountsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to manage both Fixed and Regular counts.
 *
 * Handles:
 * - Loading counts
 * - Multi-select mode
 * - Selection and deletion of items
 */
@HiltViewModel
class CountsViewModel @Inject constructor(
    // private val repository: ICountsRepository
) : ViewModel() {

    // ---------------- State Flows ----------------
    private val _fixedUiState = MutableStateFlow(FixedCountsUiState())
    val fixedUiState: StateFlow<FixedCountsUiState> = _fixedUiState.asStateFlow()

    private val _regularUiState = MutableStateFlow(RegularCountsUiState())
    val regularUiState: StateFlow<RegularCountsUiState> = _regularUiState.asStateFlow()

    init {
        loadFixedCounts()
        loadRegularCounts()
    }

    // ---------------- Event Dispatchers ----------------
    /** Handles FixedCounts events from UI */
    fun onFixedEvent(event: FixedCountsEvent) {
        when (event) {
            is FixedCountsEvent.ItemSwipedToDelete -> deleteFixedItem(event.item)
            is FixedCountsEvent.SelectItem -> selectFixedItem(event.item)
            FixedCountsEvent.DeleteClicked -> handleFixedDeleteClick()
            FixedCountsEvent.ToggleMultiSelectMode -> toggleFixedMultiSelectMode()
            FixedCountsEvent.CloseMultiSelectMode -> closeFixedMultiSelectMode()
        }
    }

    /** Handles RegularCounts events from UI */
    fun onRegularEvent(event: RegularCountsEvent) {
        when (event) {
            is RegularCountsEvent.ItemSwipedToDelete -> deleteRegularItem(event.item)
            is RegularCountsEvent.SelectItem -> selectRegularItem(event.item)
            RegularCountsEvent.DeleteClicked -> handleRegularDeleteClick()
            RegularCountsEvent.ToggleMultiSelectMode -> toggleRegularMultiSelectMode()
            RegularCountsEvent.CloseMultiSelectMode -> closeRegularMultiSelectMode()
        }
    }

    // ---------------- Fixed Counts Logic ----------------
    private fun loadFixedCounts() = viewModelScope.launch {
        _fixedUiState.update { it.copy(fixedCounts = sampleFixedCounts()) }
    }

    private fun selectFixedItem(item: CountItem) {
        val selected = _fixedUiState.value.selectedItems.toMutableList()
        if (selected.contains(item)) selected.remove(item) else selected.add(item)
        _fixedUiState.update { it.copy(selectedItems = selected) }
    }

    private fun handleFixedDeleteClick() {
        if (_fixedUiState.value.isMultiSelectMode) {
            val remainingItems = _fixedUiState.value.fixedCounts.toMutableList()
            remainingItems.removeAll(_fixedUiState.value.selectedItems)
            _fixedUiState.update {
                it.copy(
                    fixedCounts = remainingItems,
                    selectedItems = emptyList(),
                    isMultiSelectMode = false
                )
            }
        } else {
            toggleFixedMultiSelectMode()
        }
    }

    private fun deleteFixedItem(item: CountItem) {
        _fixedUiState.update {
            it.copy(fixedCounts = it.fixedCounts.filterNot { i -> i.id == item.id })
        }
    }

    private fun toggleFixedMultiSelectMode() {
        _fixedUiState.update { it.copy(isMultiSelectMode = !it.isMultiSelectMode) }
    }

    private fun closeFixedMultiSelectMode() {
        _fixedUiState.update { it.copy(isMultiSelectMode = false, selectedItems = emptyList()) }
    }

    // ---------------- Regular Counts Logic ----------------
    private fun loadRegularCounts() = viewModelScope.launch {
        _regularUiState.update { it.copy(regularCounts = sampleRegularCounts()) }
    }

    private fun selectRegularItem(item: CountItem) {
        val selected = _regularUiState.value.selectedItems.toMutableList()
        if (selected.contains(item)) selected.remove(item) else selected.add(item)
        _regularUiState.update { it.copy(selectedItems = selected) }
    }

    private fun handleRegularDeleteClick() {
        if (_regularUiState.value.isMultiSelectMode) {
            val remainingItems = _regularUiState.value.regularCounts.toMutableList()
            remainingItems.removeAll(_regularUiState.value.selectedItems)
            _regularUiState.update {
                it.copy(
                    regularCounts = remainingItems,
                    selectedItems = emptyList(),
                    isMultiSelectMode = false
                )
            }
        } else {
            toggleRegularMultiSelectMode()
        }
    }

    private fun deleteRegularItem(item: CountItem) {
        _regularUiState.update {
            it.copy(regularCounts = it.regularCounts.filterNot { i -> i.id == item.id })
        }
    }

    private fun toggleRegularMultiSelectMode() {
        _regularUiState.update { it.copy(isMultiSelectMode = !it.isMultiSelectMode) }
    }

    private fun closeRegularMultiSelectMode() {
        _regularUiState.update { it.copy(isMultiSelectMode = false, selectedItems = emptyList()) }
    }

    // ---------------- Sample Data ----------------
    private fun sampleFixedCounts() = listOf(
        CountItem(name = "Ibuprofen 200mg", quantity = 60, date = "29-05-2025 10:00 am"),
        CountItem(name = "Paracetamol 500mg", quantity = 100, date = "28-05-2025 11:45 am"),
        CountItem(name = "Omeprazole 20mg", quantity = 80, date = "28-05-2025 07:20 am")
    )

    private fun sampleRegularCounts() = listOf(
        CountItem(name = "Bevacizumab 5MG", quantity = 150, date = "29-05-2025 11:23 am"),
        CountItem(name = "Metformin 500mg", quantity = 90, date = "28-05-2025 09:15 am"),
        CountItem(name = "Lisinopril 10mg", quantity = 120, date = "28-05-2025 08:30 am")
    )
}
