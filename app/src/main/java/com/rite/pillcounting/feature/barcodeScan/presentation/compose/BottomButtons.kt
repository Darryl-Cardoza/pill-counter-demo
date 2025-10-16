package com.rite.pillcounting.feature.barcodeScan.presentation.compose

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
import com.rite.pillcounting.core.utils.constants.Dimens.xxLarge
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.FilledButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.HollowButton

@Composable
fun BottomButtons(
    onRedo: () -> Unit,
    onManual: () -> Unit,
    onCount: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = xxLarge, start = medium, end = medium),
        horizontalArrangement = Arrangement.spacedBy(small) // automatic spacing
    ) {
        HollowButton(
            text = stringResource(R.string.redo).uppercase(),
            onClick = onRedo,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f) // takes equal width
        )
        HollowButton(
            text = stringResource(R.string.manual).uppercase(),
            onClick = onManual,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        FilledButton(
            text = stringResource(R.string.count).uppercase(),
            onClick = onCount,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}
