package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.small
import com.example.pillcountingnewmodels.core.utils.compose.FilledButton
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
            .padding(top = medium),
        horizontalArrangement = Arrangement.spacedBy(small) // automatic spacing
    ) {
        HollowButton(
            text = stringResource(R.string.rescane).uppercase(),
            onClick = { onEvent(FixedCountPillScanningEvent.RescanClicked) },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f) // takes equal width
        )
        HollowButton(
            text = stringResource(R.string.pause).uppercase(),
            onClick = { onEvent(FixedCountPillScanningEvent.PauseClicked) },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        FilledButton(
            text = stringResource(R.string.done).uppercase(),
            onClick = { onEvent(FixedCountPillScanningEvent.DoneClicked) },
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}
