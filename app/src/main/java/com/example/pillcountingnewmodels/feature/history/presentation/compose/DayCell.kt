package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.LocalDate

/**
 * Composable representing a single day cell in the calendar.
 *
 * Features:
 * - Highlights the selected date with [MaterialTheme.colorScheme.secondary].
 * - Highlights today with a faded primary color.
 * - Disables clicks for future dates.
 *
 * @param day The [CalendarDay] to render.
 * @param selectedDate Currently selected date (nullable).
 * @param onDateSelected Callback invoked when the user selects a valid date.
 */
@Composable
fun DayCell(
    day: CalendarDay,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    // Only render dates that belong to the current month
    if (day.position == DayPosition.MonthDate) {
        val isSelected = day.date == selectedDate
        val isToday = day.date == LocalDate.now()

        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(2.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.secondary
                        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                        else -> Color.Transparent
                    }
                )
                .clickable(
                    enabled = !day.date.isAfter(LocalDate.now())
                ) {
                    onDateSelected(day.date)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
