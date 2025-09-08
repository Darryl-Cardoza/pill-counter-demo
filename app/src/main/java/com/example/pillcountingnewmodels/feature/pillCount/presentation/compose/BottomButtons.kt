package com.example.pillcountingnewmodels.feature.pillCount.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.xxLarge
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
            .padding(bottom = xxLarge, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(medium - 7.dp) // automatic spacing
    ) {
        HollowButton(
            text = "Redo".uppercase(),
            onClick = onRedo,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f) // takes equal width
        )
        HollowButton(
            text = "Skip".uppercase(),
            onClick = onSkip,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        HollowButton(
            text = "Scan".uppercase(),
            onClick = onScan,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}
