package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.responsiveDp
import com.rite.pillcounting.core.utils.constants.Dimens.large
import com.rite.pillcounting.core.utils.constants.Dimens.xxxLarge
import com.rite.pillcounting.feature.pillCountScan.domain.data.PillScanningEvent
import kotlinx.coroutines.delay

@Composable
fun ActionButtons(
    onEvent: (PillScanningEvent) -> Unit,
    filteredPillCount: Int,
    onListClicked: () -> Unit
) {
    var isAddEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(isAddEnabled) {
        if (!isAddEnabled) {
            delay(5000)
            isAddEnabled = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = large, end = large),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            modifier = Modifier.size(responsiveDp(xxxLarge)),
            onClick = { onListClicked() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.history),
                contentDescription = "List",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(responsiveDp(xxxLarge))
            )
        }

        // Add (disabled for 5 seconds after click)
        IconButton(
            modifier = Modifier.size(responsiveDp(xxxLarge)),
            onClick = {
                if (isAddEnabled) {
                    isAddEnabled = false
                    onEvent(
                        PillScanningEvent.AddTransactionDetailClicked(filteredPillCount)
                    )
                }
            },
            enabled = isAddEnabled
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Add",
                tint = if (isAddEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(responsiveDp(xxxLarge))
            )
        }

        // Done/Complete
        IconButton(
            modifier = Modifier.size(responsiveDp(xxxLarge)),
            onClick = { onEvent(PillScanningEvent.DoneClicked) }) {
            Icon(
                painter = painterResource(id = R.drawable.complete),
                contentDescription = "Complete",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(responsiveDp(xxxLarge))
            )
        }
    }
}
