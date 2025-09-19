package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.FixedCountPillScanningUiState
import com.example.pillcountingnewmodels.ui.theme.AppTheme

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
fun DrugInformation(uiState: FixedCountPillScanningUiState) {
    // Calculate the total count from the batch history list
    val totalBatchCount = uiState.batchHistory.sumOf { it.count }
    val nextBatchNumber = uiState.batchHistory.size + 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Drug Name
        Text(
            text = "Drug Name: ${uiState.drugName}",
            fontSize = 14.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            color = AppTheme.extendedColors.textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Batch number and Total/Expected counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Display the next batch number
            Text(
                text = "Batch $nextBatchNumber",
                fontSize = 16.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )

            val countDisplay = when (uiState.scanType) {
                "FIXED" -> "${totalBatchCount}/${uiState.expectedCount}"
                "REGULAR" -> totalBatchCount.toString()
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