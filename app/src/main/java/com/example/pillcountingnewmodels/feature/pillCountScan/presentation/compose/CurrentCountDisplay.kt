package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.common.UserInterfaceUtils.FilledButton
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.PillScanningUiState

/**
 * Displays the current scanned pill count along with an "ADD" button.
 *
 * Layout:
 * - Circular indicator for the current count from the camera analysis.
 * - Button to add the current count to the batch history.
 *
 * @param uiState The current state of the scanning screen.
 * @param onAddClicked Lambda invoked when the "ADD" button is pressed.
 */
@Composable
fun CurrentCountDisplay(
    uiState: PillScanningUiState,
    filteredCount: Int,
    onAddClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Circular count indicator reflects the live detected pill count.
        CircularCountIndicator(count = /*uiState.detectedPills.size*/filteredCount)

        FilledButton(
            text = stringResource(R.string.add).uppercase(),
            onClick = onAddClicked,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}