package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.small
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.xxLarge
import com.example.pillcountingnewmodels.core.utils.compose.FilledButton
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton

@Composable
fun BottomButtons(
    onRedo: () -> Unit,
    onSkip: () -> Unit,
    onScan: () -> Unit
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
            text = stringResource(R.string.skip).uppercase(),
            onClick = onSkip,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        FilledButton(
            text = stringResource(R.string.scan).uppercase(),
            onClick = onScan,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}
