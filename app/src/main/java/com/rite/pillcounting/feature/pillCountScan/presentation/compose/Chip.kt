package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.feature.pillCountScan.domain.model.TxnDetail

/**
 * A custom Chip composable to display batch counts.
 *
 * Features:
 * - Shows the main count inside a rounded rectangle.
 * - Displays a circular badge above the chip with the batch number.
 *
 * @param count The main count text to display.
 * @param modifier Optional [Modifier] to apply to the parent container.
.
 */
@Composable
fun Chip(
    shouldHighlight: Boolean,
    txnDetail: TxnDetail,
    modifier: Modifier = Modifier,
    index: Int,
    onDelete: (txnDetailId: Long) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .clickable { showDialog = true },
        contentAlignment = Alignment.TopCenter
    ) {
        // Main chip container
        Box(
            modifier = Modifier
                .size(
                    width = 50.dp,
                    height = 50.dp
                )
                .border(
                    width = if (shouldHighlight)3.dp else 1.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(7.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = txnDetail.count.toString(),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = if (shouldHighlight) FontWeight.Bold else FontWeight.Normal
            )
        }

        // Transaction Detail number badge
        Box(
            modifier = Modifier
                .offset(y = (if (shouldHighlight) -20 else -12).dp)
                .size(if (shouldHighlight) 33.dp else 25.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = Color.White,
                fontSize = if (shouldHighlight) 15.sp else 12.sp,
                fontWeight = if (shouldHighlight) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    if (showDialog) {
        TxnDetailDialog(
            batchNumber = index,
            details = txnDetail,
            onDelete = {
                onDelete(txnDetail.txnDetailId)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}
