package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.feature.history.domain.model.TxnWithDrugDto
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * Section displaying a list of medicine counts for a selected date.
 * Fully MVVM-compliant:
 * - Receives preformatted row data from the ViewModel.
 * - UI only handles rendering.
 *
 * @param appTheme Theme wrapper for colors and typography.
 * @param counts List of row data from ViewModel.
 * @param onExportClick Callback when export icon is clicked.
 * @param onDeleteClick Callback when delete icon is clicked.
 * @param onFilterClick Callback when filter icon is clicked.
 * @param onSearchClick Callback when search icon is clicked.
 */
@Composable
fun CountsSection(
    appTheme: AppTheme,
    counts: List<TxnWithDrugDto>,
    onExportClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onTxnClick: (Long) -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Header row with count label and action icons
        if (counts.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.counts_label, counts.size),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = appTheme.extendedColors.textColor,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionIcon(
                        iconRes = R.drawable.pdf,
                        contentDescription = stringResource(R.string.export_content_description),
                        onClick = onExportClick
                    )
                    ActionIcon(
                        iconRes = R.drawable.delete,
                        contentDescription = stringResource(R.string.delete_content_description),
                        onClick = onDeleteClick
                    )
                    /*ActionIcon(
                    iconRes = R.drawable.filter,
                    contentDescription = stringResource(R.string.filter_content_description),
                    onClick = onFilterClick
                    )
                    ActionIcon(
                        iconRes = R.drawable.search,
                        contentDescription = stringResource(R.string.search_content_description),
                        onClick = onSearchClick
                    )*/
                }
            }

            Spacer(Modifier.height(16.dp))
        }


        if (counts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_data_found),
                    fontSize = 18.sp,
                    color = appTheme.extendedColors.textColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(counts) { rowData ->
                    CountRow(
                        rowData = rowData,
                        appTheme = appTheme,
                        onTxnClick = { onTxnClick(rowData.txnId) })
                }
            }
        }

    }
}
