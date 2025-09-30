package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.ui.theme.AppTheme


@Composable

fun DrugHistoryDetailsBox(appTheme: AppTheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(100.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4)
            )
            .background(appTheme.extendedColors.secondaryBackground)
            .verticalScroll(rememberScrollState()) // Make it scrollable
    ) {
        Text(
            text = "Nunc id justo vitae lectus interdum pretium at nec urna. Aenean ipsum purus, semper eu eros vel, semper vehicula leo. Integer congue, tortor non posuere elementum, est nunc aliquam metus, dictum eleifend velit sem sed sem. ",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AppTheme.extendedColors.textColor.copy(alpha = 0.7f),
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(start= medium, top = medium)
        )

    }
}