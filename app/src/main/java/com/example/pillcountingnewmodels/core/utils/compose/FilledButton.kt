package com.example.pillcountingnewmodels.core.utils.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonCornerRadius
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.buttonHeight

@Composable
fun FilledButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    buttonHeightDefault: Dp = buttonHeight,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(buttonHeightDefault)
            .widthIn(min = 100.dp)
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
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


