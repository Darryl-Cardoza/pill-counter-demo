package com.example.pillcountingnewmodels.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.feature.history.domain.model.CountRowData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for History screen.
 * Prepares all data needed for UI, including formatted timestamps.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor() : ViewModel() {

    // Currently selected date
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    // Sample medicine count data
    private val _counts = MutableStateFlow(
        List(15) { index ->
            val name = if (index % 2 == 0) "Allopurinol 5MG" else "Bevacizumab 5MG"
            val count = if (index % 2 == 0) 200 + index else 100 + index
            val timestamp = LocalDateTime.now().minusDays(index.toLong())
            val formattedTime = timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a", Locale.getDefault()))
            CountRowData(name, count, R.drawable.partial, formattedTime)
        }
    )
    val counts: StateFlow<List<CountRowData>> = _counts

    /** Updates the selected date */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
}

