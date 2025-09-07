package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import com.example.pillcountingnewmodels.ui.theme.AppTheme.extendedColors
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/**
 * History Screen
 *
 * - Displays a vertical calendar (scrollable only in the past).
 * - Highlights today with [MaterialTheme.colorScheme.primary].
 * - Shows medicine counts on the right/bottom side.
 * - Supports back navigation and common actions (export, delete, filter, search).
 */
@Composable
fun HistoryScreen(
    navController: NavController,
    onBackClick: () -> Unit = {}
) {
    val appTheme = AppTheme
    val today = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.from(today) }

    // Calendar state → starts 12 months in the past, ends at the current month.
    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    // Keep track of the selected date (default = today)
    var selectedDate by remember { mutableStateOf(today) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(appTheme.extendedColors.secondaryBackground)
    ) {
        SplitResponsive(
            topOrLeft = {
                CalendarSection(
                    appTheme = appTheme,
                    calendarState = calendarState,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    onBackClick = onBackClick
                )
            },
            bottomOrRight = {
                CountsSection(
                    appTheme = appTheme,
                    selectedDate = selectedDate
                )
            },
        )
    }
}

/**
 * Calendar section → Handles rendering of:
 * - Back button
 * - Calendar with month headers & day cells
 */
@Composable
private fun CalendarSection(
    appTheme: AppTheme,
    calendarState: com.kizitonwose.calendar.compose.CalendarState,
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
                    .clickable {  }
            )

            Spacer(Modifier.width(24.dp))

            Text(
                text = stringResource(R.string.history_title),
                fontSize = 16.sp,
                color = extendedColors.textColor
            )
        }

        Spacer(Modifier.height(16.dp))

        // Calendar → Past-only, today preselected
        VerticalCalendar(
            state = calendarState,
            monthHeader = { calendarMonth ->
                val month = calendarMonth.yearMonth
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${
                            month.month.name.lowercase()
                                .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                        } ${month.year}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
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

/**
 * Single day cell in the calendar.
 * - Highlights selected date with primary.
 * - Highlights today with faded primary.
 * - Disables future dates.
 */
@Composable
private fun DayCell(
    day: CalendarDay,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
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
                .clickable(enabled = !day.date.isAfter(LocalDate.now())) {
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

/**
 * Right/Bottom section → Displays counts list with actions.
 */
@Composable
private fun CountsSection(
    appTheme: AppTheme,
    selectedDate: LocalDate
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Header row (label + action icons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(
                    R.string.counts_label,
                    24
                ), // Replace 24 with dynamic data count
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = appTheme.extendedColors.textColor,
                textAlign = TextAlign.Center
            )
            Row {
                ActionIcon(
                    R.drawable.export,
                    stringResource(R.string.export_content_description)
                ) {}
                ActionIcon(
                    R.drawable.delete,
                    stringResource(R.string.delete_content_description)
                ) {}
                ActionIcon(
                    R.drawable.filter,
                    stringResource(R.string.filter_content_description)
                ) {}
                ActionIcon(
                    R.drawable.search,
                    stringResource(R.string.search_content_description)
                ) {}
            }
        }

        Spacer(Modifier.height(16.dp))

        // Mocked sample data → replace with real DB/API
        val sampleData = List(15) { index ->
            if (index % 2 == 0)
                Triple("Allopurinol 5MG", 200 + index, R.drawable.partial)
            else
                Triple("Bevacizumab 5MG", 100 + index, R.drawable.notes)
        }

        // Counts list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(sampleData) { (name, count, icon) ->
                CountRow(
                    name = name,
                    count = count,
                    iconRes = icon,
                    appTheme = appTheme
                )
            }
        }
    }
}

/**
 * Row item representing a single medicine count entry.
 */
@Composable
fun CountRow(
    name: String,
    count: Int,
    iconRes: Int,
    appTheme: AppTheme
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine thumbnail/logo
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            // Medicine name + date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = appTheme.extendedColors.textColor
                )
                Text(
                    text = "${LocalDate.now()} 11:23 am", // Replace with actual timestamp
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            // Action icon
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Count value
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = appTheme.extendedColors.textColor
            )
        }

        // Divider
        HorizontalDivider(color = extendedColors.textColor.copy(alpha = 0.2f))
    }
}

/**
 * Reusable action icon for header (export/delete/filter/search).
 */
@Composable
private fun ActionIcon(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .size(36.dp)
            .padding(horizontal = 6.dp)
            .clickable { onClick() }
    )
}
