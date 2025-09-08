package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonCornerRadius
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonHeight
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonInnerHorizontalPadding
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.small

@Composable
fun FilledButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(buttonHeight)
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(buttonCornerRadius)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
        ),
    ) {
        Text(text)
    }
}


