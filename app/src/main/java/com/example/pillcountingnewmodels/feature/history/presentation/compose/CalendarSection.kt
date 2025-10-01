package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.VerticalCalendar
import java.time.LocalDate
import java.util.Locale

/**
 * Calendar section of the History screen.
 * Purely UI; all business logic (selected date, calendar state) is handled by the ViewModel.
 *
 * @param appTheme Theme wrapper for extended colors.
 * @param calendarState State object for the [VerticalCalendar].
 * @param selectedDate Currently selected date from ViewModel.
 * @param onDateSelected Callback to notify ViewModel when a date is selected.
 * @param onBackClick Callback when the back button is pressed.
 */
@Composable
fun CalendarSection(
    calendarState: CalendarState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onBackClick: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        // Top bar: back button + screen title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BackButton(navController)

            Text(
                text = stringResource(R.string.history_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.extendedColors.textColor,
                textAlign = TextAlign.Center
            )
        }

        // Vertical calendar → only handles UI rendering
        VerticalCalendar(
            state = calendarState,
            modifier = Modifier.fillMaxSize().padding(medium),
            monthHeader = { calendarMonth ->
                val month = calendarMonth.yearMonth
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${month.month.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }} ${month.year}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            dayContent = { day ->
                DayCell(
                    day = day,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )
            }
        )
    }
}
