package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A custom Chip composable to display batch counts.
 *
 * Features:
 * - Shows the main count inside a rounded rectangle.
 * - Displays a circular badge above the chip with the batch number.
 *
 * @param count The main count text to display.
 * @param batchNumber The batch number displayed in the circular badge.
 * @param modifier Optional [Modifier] to apply to the parent container.
 * @param chipColor Background color of the chip (default: MaterialTheme color).
 * @param badgeColor Background color of the batch badge (default: pink).
 * @param textColor Color of the text inside the chip (default: MaterialTheme onSurface).
 */
@Composable
fun Chip(
    count: String,
    batchNumber: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Main chip container
        Box(
            modifier = Modifier
                .size(width = 50.dp, height = 50.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color.Transparent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Batch number badge
        Box(
            modifier = Modifier
                .offset(y = (-12).dp)
                .size(25.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = batchNumber.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
