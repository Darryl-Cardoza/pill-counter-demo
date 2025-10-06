package com.example.pillcountingnewmodels.feature.menu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.core.utils.compose.HelperFunctions.mapCounts
import com.example.pillcountingnewmodels.feature.menu.domain.model.MenuUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Menu screen.
 *
 * ### Responsibilities
 * - Listen for dashboard count changes via [PillCountTxnDao].
 * - Map grouped database rows into [MenuUiState] counts.
 * - Expose reactive [StateFlow] for UI consumption.
 *
 * Threading:
 * - Collects database flows on the ViewModel's coroutine scope.
 * - Errors are caught and replaced with a default empty state.
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val pillCountTxnDao: PillCountTxnDao,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    /** Backing state flow for the Menu UI. */
    private val _uiState = MutableStateFlow(MenuUiState())

    /** Public immutable UI state exposed to the UI layer. */
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        observeDashboardCounts()
    }

    /**
     * Observe pill count dashboard rows from DB and map into [MenuUiState].
     *
     * Uses [mapCounts] to ensure consistent logic across dashboard and menu screens.
     * Provides:
     * - Fixed Completed / Partial
     * - Regular Completed / Partial
     */
    private fun observeDashboardCounts() {
        viewModelScope.launch {
            pillCountTxnDao.observeDashboardCountsGrouped()
                .map { rows ->
                    val counts = mapCounts(rows)
                    MenuUiState(
                        fixedCompleted = counts.fixedCompleted,
                        fixedPartial = counts.fixedPartial,
                        regularCompleted = counts.regularCompleted,
                        regularPartial = counts.regularPartial
                    )
                }
                .catch { e ->
                    e.printStackTrace()
                    emit(MenuUiState()) // fallback to default
                }
                .collect { state -> _uiState.value = state }
        }
    }

    fun getSavedHistoryOption(): Int {
        return preferenceHelper.getHistoryRetention()
    }
}

