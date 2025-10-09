package com.example.pillcountingnewmodels.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.utils.preference.PreferenceHelper
import com.example.pillcountingnewmodels.feature.history.data.HistoryRepository
import com.example.pillcountingnewmodels.feature.history.domain.model.TxnWithDrugDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for History screen.
 * Prepares all data needed for UI, including formatted timestamps.
 */

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    @OptIn(ExperimentalCoroutinesApi::class)
    val counts: StateFlow<List<TxnWithDrugDto>> =
        _selectedDate
            .flatMapLatest { date ->
                repository.getTransactionsForDate(date)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun deleteCountsForSelectedDate() {
        viewModelScope.launch {
            repository.deleteTransactionsForDate(_selectedDate.value)
        }
    }

    fun selectCurrentTransaction(txnId: Long) {
        preferenceHelper.saveTxnId(txnId)
    }
}





