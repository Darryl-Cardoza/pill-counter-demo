package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import android.widget.GridLayout
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.FixedCountPillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.FixedCountPillScanningUiState

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
            .padding(medium)
    ) {
        // Top Bar: Back Arrow, Title, Menu
        TopAppBar(navController)

        // Drug Info: Name, Batch, Total
        DrugInformation(uiState)
        Column(modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly){
            // Main Count Display and Add Button
            CurrentCountDisplay(uiState) { onEvent(FixedCountPillScanningEvent.AddBatchClicked) }
            // Horizontal list of previous batch counts
            BatchHistory(uiState.batchHistory)
        }

        // Action Buttons: Rescan, Pause, Done
        ActionButtons(onEvent)

    }
}


