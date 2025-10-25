package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.FilledButton
import com.rite.pillcounting.feature.pillCountScan.domain.model.PillScanningUiState
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import kotlinx.coroutines.delay

/**
 * Displays the current detected pill count along with an **ADD** button.
 *
 * This composable shows the live counter section of the pill scanning screen.
 * When the user presses the ADD button, it triggers the event callback and
 * temporarily disables the button for 5 seconds to prevent duplicate taps.
 *
 * Layout:
 * - Circular indicator for the live pill count.
 * - "ADD" button that visually dims and becomes inactive for 5 seconds.
 *
 * @param uiState The current state of the pill scanning UI.
 * @param filteredCount The currently detected and filtered pill count.
 * @param viewModel The [PillScanningViewModel] managing count updates.
 * @param onAddClicked Callback invoked when the "ADD" button is pressed.
 */
@Composable
fun CurrentCountDisplay(
    uiState: PillScanningUiState,
    filteredCount: Int,
    viewModel: PillScanningViewModel,
    onAddClicked: () -> Unit
) {
    var isButtonEnabled by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        CircularCountIndicator(
            count = filteredCount,
            viewModel = viewModel
        )

        // Apply dimming when disabled using alpha
        FilledButton(
            text = stringResource(R.string.add).uppercase(),
            onClick = {
                if (isButtonEnabled) {
                    isButtonEnabled = false
                    onAddClicked()
                }
            },
            color = if (isButtonEnabled)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            modifier = Modifier.graphicsLayer {
                alpha = if (isButtonEnabled) 1f else 0.5f
            }
        )
    }

    // Re-enable button after 5 seconds
    LaunchedEffect(isButtonEnabled) {
        if (!isButtonEnabled) {
            delay(5000)
            isButtonEnabled = true
        }
    }
}

