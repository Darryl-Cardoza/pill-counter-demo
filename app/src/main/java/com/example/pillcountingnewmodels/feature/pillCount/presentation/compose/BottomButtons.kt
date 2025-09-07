package com.example.pillcountingnewmodels.feature.pillCount.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
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
            .padding(bottom = xxLarge),
        horizontalArrangement = Arrangement.Center, // space between buttons
    ) {
        HollowButton(
            text = "Redo".uppercase(),
            onClick = onRedo,
            color = MaterialTheme.colorScheme.primary

        )
        Spacer(modifier = Modifier.width(medium))
        HollowButton(
            text = "Skip".uppercase(),
            onClick = onSkip,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(medium))
        ActionButtonPrimary(
            text = "Scan".uppercase(),
            onClick = onScan,
            color = MaterialTheme.colorScheme.secondary,
            useContentPadding = false
        )
    }
}