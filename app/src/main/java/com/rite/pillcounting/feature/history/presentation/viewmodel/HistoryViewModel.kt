package com.rite.pillcounting.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import com.rite.pillcounting.feature.history.data.HistoryRepository
import com.rite.pillcounting.feature.history.domain.model.HistoryMode
import com.rite.pillcounting.feature.history.domain.model.TxnWithDrugDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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


    private val _counts = MutableStateFlow<List<TxnWithDrugDto>>(emptyList())
    val counts: StateFlow<List<TxnWithDrugDto>> = _counts

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private var currentMode: HistoryMode = HistoryMode.NORMAL



    fun start(mode: HistoryMode) {
        currentMode = mode
        loadCounts(_selectedDate.value)
    }


    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadCounts(date)
    }

    /** Load all completed Transaction **/
    private fun loadCounts(date: LocalDate) {
        viewModelScope.launch {

            val flow = when (currentMode) {
                HistoryMode.NORMAL ->
                    repository.getTransactionsForDate(date)

                HistoryMode.REGULAR ->
                    repository.getRegularTransactionsWithDrugByDate(date)

                HistoryMode.DISPENSE ->
                    repository.getDispenseTransactionsWithDrugByDate(date)
            }

            flow.collect {
                _counts.value = it
            }
        }
    }




    /** delete all transaction selected date **/
    fun deleteCountsForSelectedDate() {
        viewModelScope.launch {

            when (currentMode) {
                HistoryMode.NORMAL ->
                    repository.deleteTransactionsForDate(_selectedDate.value)

                HistoryMode.REGULAR ->
                    repository.deleteRegularTransactionsForDate(_selectedDate.value)

                HistoryMode.DISPENSE ->
                    repository.deleteDispenseTransactionsForDate(_selectedDate.value)
            }

            loadCounts(_selectedDate.value) // refresh immediately
        }
    }


    fun selectCurrentTransaction(txnId: Long) {
        preferenceHelper.saveTxnId(txnId)
    }
}




