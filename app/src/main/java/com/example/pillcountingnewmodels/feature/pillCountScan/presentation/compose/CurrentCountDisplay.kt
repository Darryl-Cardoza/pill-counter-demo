package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.FilledButton
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.FixedCountPillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.FixedCountPillScanningUiState

/**
 * Displays the current scanned pill count along with an "ADD" button.
 *
 * Layout:
 * - Circular indicator for the current count.
 * - Button to increment/add the count.
 *
 * @param uiState The current state of the scanning screen.
 * @param onAddClicked Lambda invoked when the "ADD" button is pressed.
 */
@Composable
fun CurrentCountDisplay(
    uiState: FixedCountPillScanningUiState,
    onAddClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Circular count indicator
        CircularCountIndicator(count = uiState.currentScanCount)

        FilledButton(
            text = stringResource(R.string.add).uppercase(),
            onClick = onAddClicked,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}
