package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.core.utils.constants.Dimens.medium
import com.rite.pillcounting.feature.pillCountScan.domain.model.TxnDetail
import com.rite.pillcounting.ui.theme.AppTheme

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
            .background(
                color = AppTheme.extendedColors.secondaryBackground,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { showDialog = true }
            .padding(medium),
        contentAlignment = Alignment.TopCenter
    ) {
        val countText = txnDetail.count.toString()
        val displayText = if (countText.length == 1) " $countText " else countText // add space for 1-digit numbers

        Text(
            text = displayText,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = if (shouldHighlight) FontWeight.Black else FontWeight.Normal,
            fontSize = if (shouldHighlight) 22.sp else 20.sp
        )
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
