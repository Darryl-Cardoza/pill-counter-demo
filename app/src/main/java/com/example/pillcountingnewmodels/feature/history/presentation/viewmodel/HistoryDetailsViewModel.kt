package com.example.pillcountingnewmodels.feature.history.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.dashboard.domain.model.DashboardUiState
import com.example.pillcountingnewmodels.feature.history.data.HistoryRepository
import com.example.pillcountingnewmodels.feature.history.domain.model.HistoryDetailsUiState
import com.example.pillcountingnewmodels.feature.history.domain.model.TxnWithDrugDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class HistoryDetailsViewModel @Inject constructor(
    preferenceHelper: PreferenceHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d("HistoryDetailsViewModel", "HistoryDetailsViewModel initialized ${preferenceHelper.getTxnId()}")
    }
}





