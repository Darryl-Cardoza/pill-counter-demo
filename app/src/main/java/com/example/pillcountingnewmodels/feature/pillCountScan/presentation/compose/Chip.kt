package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.TxnDetail

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
    txnDetail: TxnDetail,
    modifier: Modifier = Modifier,
    index: Int
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
                    shape = RoundedCornerShape(7.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = txnDetail.count.toString(),
                color = MaterialTheme.colorScheme.secondary
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
                text = index.toString(),
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}
