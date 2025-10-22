package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.medium
import com.rite.pillcounting.feature.pillCountScan.domain.data.PillScanningEvent
import com.rite.pillcounting.feature.pillCountScan.domain.model.PillScanningUiState
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import com.rite.pillcounting.ui.theme.AppTheme

/**
 * The right-hand panel of the scanning screen, containing all drug information,
 * batch counts, and user action buttons.
 *
 * @param uiState The current state of the UI to display.
 * @param onEvent The callback to send events to the ViewModel.
 */
@Composable
fun InformationPanelSection(
    uiState: PillScanningUiState,
    viewModel: PillScanningViewModel,
    onEvent: (PillScanningEvent) -> Unit,
    filteredPillCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(medium)
    ) {
        Text(
            text = stringResource(R.string.pill_count_title).uppercase(),
            fontSize = 16.sp,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
            color = AppTheme.extendedColors.textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        // Drug Info: Name, Batch, Total
        DrugInformation(uiState)

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Main Count Display and Add Button
            CurrentCountDisplay( viewModel = viewModel, uiState= uiState,filteredCount = filteredPillCount) { onEvent(PillScanningEvent.AddTransactionDetailClicked(filteredPillCount)) }
            // Horizontal list of previous Transaction Detail counts
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(medium),
                reverseLayout = true
            ) {
                itemsIndexed(uiState.txnDetailHistory) { index, txnDetail ->
                    //Highlight first element of list
                    val highlight = index == 0
                    // Pass the entire Transaction Detail object to the Chip
                    Chip(
                        shouldHighlight = highlight,
                        txnDetail = txnDetail,
                        index = uiState.txnDetailHistory.size - index,
                        onDelete = { txnDetailId ->
                            onEvent(PillScanningEvent.TransactionDetailDeleted(txnDetailId))
                        })
                }
            }
        }

        // Action Buttons: Rescan, Pause, Done
        ActionButtons(onEvent)

    }
}


