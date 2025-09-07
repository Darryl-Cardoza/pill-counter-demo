package com.example.pillcountingnewmodels.feature.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.history.presentation.compose.CalendarSection
import com.example.pillcountingnewmodels.feature.history.presentation.compose.CountsSection
import com.example.pillcountingnewmodels.feature.history.presentation.viewmodel.HistoryViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * HistoryScreen
 *
 * Displays user's medicine history in a responsive split layout:
 * - **Calendar Section:** Select a date to filter counts.
 * - **Counts Section:** Shows medicines counted for selected date.
 *
 * State is fully managed by [HistoryViewModel].
 *
 * @param viewModel ViewModel providing state and counts data.
 * @param onBackClick Callback invoked when the back button is pressed.
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val appTheme = AppTheme

    // Observe selected date and counts from ViewModel
    val selectedDate by viewModel.selectedDate.collectAsState()
    val counts by viewModel.counts.collectAsState()

    // Calendar state must be initialized in Composable context
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(appTheme.extendedColors.secondaryBackground)
    ) {
        SplitResponsive(
            topOrLeft = {
                // Calendar section
                CalendarSection(
                    appTheme = appTheme,
                    calendarState = calendarState,
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.selectDate(it) },
                    onBackClick = onBackClick
                )
            },
            bottomOrRight = {
                // Counts section
                CountsSection(
                    appTheme = appTheme,
                    counts = counts,
                    onExportClick = { /* Handle export */ },
                    onDeleteClick = { /* Handle delete */ },
                    onFilterClick = { /* Handle filter */ },
                    onSearchClick = { /* Handle search */ }
                )
            }
        )
    }
}
