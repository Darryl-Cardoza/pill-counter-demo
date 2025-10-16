package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.medium
import com.rite.pillcounting.core.utils.constants.Dimens.small
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.FilledButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.HollowButton
import com.rite.pillcounting.feature.pillCountScan.domain.data.PillScanningEvent

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
    onEvent: (PillScanningEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = medium),
        horizontalArrangement = Arrangement.spacedBy(small) // automatic spacing
    ) {
        HollowButton(
            text = stringResource(R.string.rescane).uppercase(),
            onClick = { onEvent(PillScanningEvent.RescanClicked) },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f) // takes equal width
        )
        HollowButton(
            text = stringResource(R.string.pause).uppercase(),
            onClick = { onEvent(PillScanningEvent.PauseClicked) },
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        FilledButton(
            text = stringResource(R.string.done).uppercase(),
            onClick = { onEvent(PillScanningEvent.DoneClicked) },
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}
