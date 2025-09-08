package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data.FixedCountPillScanningEvent
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.model.FixedCountPillScanningUiState

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
    uiState: FixedCountPillScanningUiState,
    onEvent: (FixedCountPillScanningEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        // Top Bar: Back Arrow, Title, Menu
        TopAppBar(navController)


        // Drug Info: Name, Batch, Total
        DrugInformation(uiState)

        // Main Count Display and Add Button
        CurrentCountDisplay(uiState) { onEvent(FixedCountPillScanningEvent.AddBatchClicked) }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal list of previous batch counts
        BatchHistory(uiState.batchHistory)

        // Fill remaining space to push buttons to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons: Rescan, Pause, Done
        ActionButtons(onEvent)

        Spacer(modifier = Modifier.height(10.dp))
    }
}


