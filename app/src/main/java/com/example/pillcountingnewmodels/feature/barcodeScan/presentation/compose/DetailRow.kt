package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(medium),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor.copy(alpha = 0.7f),
            modifier = Modifier
                .weight(0.35f)
                .wrapContentWidth(Alignment.Start) // align text start inside weight
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier
                .weight(0.65f)
                .wrapContentWidth(Alignment.Start) // align text start inside weight
        )
    }
}