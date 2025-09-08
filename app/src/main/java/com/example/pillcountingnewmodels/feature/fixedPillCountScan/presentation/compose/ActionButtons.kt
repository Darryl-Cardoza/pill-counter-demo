package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data.FixedCountPillScanningEvent

/**
 * A row of action buttons for the Fixed Pill Count Scanning screen.
 *
 * Buttons:
 * - RESCAN: Triggers a rescan action.
 * - PAUSE: Pauses the scanning process.
 * - DONE: Completes the scanning session.
 *
 * @param onEvent Callback to propagate button click events.
 */
@Composable
fun ActionButtons(
    onEvent: (FixedCountPillScanningEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Rescan button → outlined style
        OutlinedButton(
            onClick = { onEvent(FixedCountPillScanningEvent.RescanClicked) },

        ) {
            Text(text = "RESCAN")
        }

        // Pause button → outlined style
        OutlinedButton(
            onClick = { onEvent(FixedCountPillScanningEvent.PauseClicked) },

        ) {
            Text(text = "PAUSE")
        }

        // Done button → filled primary button
        Button(
            onClick = { onEvent(FixedCountPillScanningEvent.DoneClicked) },

        ) {
            Text(text = "DONE")
        }
    }
}
