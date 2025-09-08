package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.FixedCountPillScanningEvent

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
            .fillMaxWidth()
            .padding(bottom = medium, start = 8.dp, end = 8.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(medium - 7.dp) // automatic spacing
    ) {
        HollowButton(
            text = "Rescan".uppercase(),
            onClick = { onEvent(FixedCountPillScanningEvent.RescanClicked) },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f) // takes equal width
        )
        HollowButton(
            text = "Pause".uppercase(),
            onClick = { onEvent(FixedCountPillScanningEvent.RescanClicked) },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        HollowButton(
            text = "Done".uppercase(),
            onClick = { onEvent(FixedCountPillScanningEvent.PauseClicked) },
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}
