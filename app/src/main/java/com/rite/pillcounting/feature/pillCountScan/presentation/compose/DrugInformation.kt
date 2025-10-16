package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.R
import com.rite.pillcounting.feature.pillCountScan.domain.model.PillScanningUiState
import com.rite.pillcounting.ui.theme.AppTheme

/**
 * Displays drug-related information in the Fixed Pill Count Scanning screen.
 *
 * Layout:
 * - Drug Name (top, left-aligned)
 * - Batch number and total count/expected count (row below)
 *
 * @param uiState Current state of the scanning screen containing drug info.
 */
@Composable
fun DrugInformation(uiState: PillScanningUiState) {
    val nextTxnDetailNumber = uiState.txnDetailHistory.size + 1
    val totalBatchCount = uiState.txnDetailHistory.sumOf { it.count }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Drug Name
        Text(
            text = "${stringResource(R.string.drugname)}: ${uiState.drugName}",
            fontSize = 14.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            color = AppTheme.extendedColors.textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Batch number and Total/Expected counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Display the next batch number
            Text(
                text = "${stringResource(R.string.txn_detail)} $nextTxnDetailNumber",
                fontSize = 16.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )

            val countDisplay = when (uiState.scanType) {
                "FIXED" -> "${stringResource(R.string.total)}  ${totalBatchCount}/${uiState.targetCount}"
                "REGULAR" -> "${stringResource(R.string.total)} $totalBatchCount"
                else -> totalBatchCount.toString()
            }

            // Display the calculated total batch count
            Text(
                text = countDisplay,
                fontSize = 16.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}