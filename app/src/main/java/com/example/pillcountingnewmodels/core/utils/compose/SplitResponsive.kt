package com.example.pillcountingnewmodels.core.utils.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun SplitResponsive(
    topOrLeft: @Composable () -> Unit,
    bottomOrRight: @Composable () -> Unit,
    cornerRadius: Dp = 16.dp,
    innerPadding: Dp = 16.dp
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(Modifier.fillMaxSize()) {
            // Left Box (flat)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) { topOrLeft() }

            // Right Box with inner corners rounded
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius))
                    .background(AppTheme.extendedColors.primaryBackground)
                    .padding(innerPadding)
            ) { bottomOrRight() }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            // Top Box (flat)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { topOrLeft() }

            // Bottom Box with inner corners rounded
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        color = AppTheme.extendedColors.primaryBackground,
                        shape = RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = cornerRadius
                        )
                    )
                    .padding(innerPadding)
            ) { bottomOrRight() }
        }
    }
}