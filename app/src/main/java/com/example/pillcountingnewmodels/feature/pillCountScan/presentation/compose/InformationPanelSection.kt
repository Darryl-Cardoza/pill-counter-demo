package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.Dimens.medium
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.PillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.PillScanningUiState

/**
 * The right-hand panel of the scanning screen, containing all drug information,
 * batch counts, and user action buttons.
 *
 * @param navController The navigation controller.
 * @param uiState The current state of the UI to display.
 * @param onEvent The callback to send events to the ViewModel.
 */
@Composable
fun InformationPanelSection(
    navController: NavController,
    uiState: PillScanningUiState,
    onEvent: (PillScanningEvent) -> Unit,
    filteredPillCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(medium)
    ) {
        // Top Bar: Back Arrow, Title, Menu
        TopAppBar(navController)

        // Drug Info: Name, Batch, Total
        DrugInformation(uiState)

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Main Count Display and Add Button
            CurrentCountDisplay(uiState= uiState,filteredCount = filteredPillCount) { onEvent(PillScanningEvent.AddTransactionDetailClicked(filteredPillCount)) }
            // Horizontal list of previous batch counts
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(medium),
            ) {
                itemsIndexed(uiState.txnDetailHistory) { index, txnDetail ->
                    // Pass the entire batch object to the Chip
                    Chip(
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


