package com.rite.pillcounting.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rite.pillcounting.core.room.models.enums.CountStatus
import com.rite.pillcounting.core.room.models.enums.CountType
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
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _currentMode = MutableStateFlow(HistoryMode.NORMAL)
    val currentMode: StateFlow<HistoryMode> = _currentMode


    @OptIn(ExperimentalCoroutinesApi::class)
    val counts: StateFlow<List<TxnWithDrugDto>> =
        combine(_selectedDate, _currentMode) { date, mode ->
            date to mode
        }
            .flatMapLatest { (date, mode) ->

                val (type, status) = mode.toQueryParams()

                repository.getTransactionsForDate(
                    date = date,
                    type = type,
                    status = status
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )




    fun setHistoryMode(mode: HistoryMode) {
        _currentMode.value = mode
    }
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    private fun HistoryMode.toQueryParams(): Pair<CountType?, CountStatus?> =
        when (this) {
            HistoryMode.NORMAL ->
                null to null

            HistoryMode.REGULAR ->
                CountType.REGULAR to CountStatus.COMPLETED

            HistoryMode.DISPENSE ->
                CountType.FIXED to CountStatus.COMPLETED
        }




    /** delete all transaction selected date **/
    fun deleteCountsForSelectedDate() {
        viewModelScope.launch {

            val (type, status) = currentMode.value.toQueryParams()

            repository.deleteTransactionsForDate(
                date = _selectedDate.value,
                type = type,
                status = status
            )
        }
    }



    fun selectCurrentTransaction(txnId: Long) {
        preferenceHelper.saveTxnId(txnId)
    }
}




