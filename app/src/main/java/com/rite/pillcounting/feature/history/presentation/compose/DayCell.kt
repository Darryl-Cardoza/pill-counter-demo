package com.rite.pillcounting.feature.history.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.rite.pillcounting.ui.theme.AppTheme

@Composable
fun DayCell(
    day: CalendarDay,
    isStart: Boolean,
    isEnd: Boolean,
    isInRange: Boolean,
    onClick: () -> Unit
) {

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = AppTheme.extendedColors.textColor

    val sliderShape = when {
        isStart && isEnd -> RoundedCornerShape(50) // single day range
        isStart -> RoundedCornerShape(
            topStart = 20.dp,
            bottomStart = 20.dp,
            topEnd = 0.dp,
            bottomEnd = 0.dp
        )

        isEnd -> RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 20.dp,
            bottomEnd = 20.dp
        )

        isInRange -> RoundedCornerShape(0.dp)
        else -> null
    }

    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .height(40.dp)
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }),
        contentAlignment = Alignment.Center
    ) {

        //  Slider background
        val isSingleDay = isStart && isEnd

        if ((isInRange || isEnd) && !isSingleDay) {

            val horizontalPaddingStart = if (isStart) 20.dp else 0.dp
            val horizontalPaddingEnd = if (isEnd) 20.dp else 0.dp

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
                    .padding(
                        start = horizontalPaddingStart,
                        end = horizontalPaddingEnd
                    )
                    .background(
                        color = primary.copy(alpha = 0.25f),
                        shape = sliderShape ?: RoundedCornerShape(0.dp)
                    )
            )
        }

        // Start & End circle on top
        if (isStart || isEnd) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = onPrimary
                )
            }
        }

        // Middle text
        else if (isInRange) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Normal
        else {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
