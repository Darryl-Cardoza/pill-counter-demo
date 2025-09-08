package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Added vertical padding for spacing
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
            Text(
                text = "Batch ${uiState.batchNumber}",
                fontSize = 16.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Total ${uiState.totalCount}/${uiState.expectedCount}",
                fontSize = 16.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
