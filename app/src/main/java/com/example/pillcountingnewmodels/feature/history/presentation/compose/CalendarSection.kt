package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import com.example.pillcountingnewmodels.ui.theme.AppTheme.extendedColors
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.CalendarState
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
    appTheme: AppTheme,
    calendarState: CalendarState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appTheme.extendedColors.secondaryBackground)
            .padding(16.dp)
    ) {
        // Top bar: back button + screen title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = stringResource(R.string.back_content_description),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
            )

            Spacer(Modifier.width(24.dp))

            Text(
                text = stringResource(R.string.history_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = appTheme.extendedColors.textColor,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        // Vertical calendar → only handles UI rendering
        VerticalCalendar(
            state = calendarState,
            modifier = Modifier.fillMaxSize(),
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
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
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
