package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonCornerRadius
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonHeight
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonInnerHorizontalPadding
import com.example.pillcountingnewmodels.ui.theme.AppTheme


@Composable
fun ActionButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    useContentPadding: Boolean = true,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = AppTheme.extendedColors.secondaryBackground, // or use a theme color
            disabledContentColor = Color.LightGray // or use a theme color
        ),
        shape = RoundedCornerShape(buttonCornerRadius),
        contentPadding = if (useContentPadding) {
            PaddingValues(horizontal = buttonInnerHorizontalPadding)
        } else {
            ButtonDefaults.ContentPadding
        },
        enabled = enabled
    ) {
        Text(text)
    }
}

