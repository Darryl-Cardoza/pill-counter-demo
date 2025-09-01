package com.example.pillcountingnewmodels.core.utils.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SplitResponsive(
    topLeftContent: @Composable RowScope.() -> Unit = {},
    topRightContent: @Composable (Color) -> Unit = {}, // 👈 Now takes a color param
    topOrLeft: @Composable () -> Unit,
    bottomOrRight: @Composable () -> Unit,
    cornerRadius: Dp = 16.dp,
    innerPadding: Dp = 16.dp,
    landscapeColor: Color = MaterialTheme.colorScheme.primary,
    portraitColor: Color = MaterialTheme.colorScheme.primary,
    hamburgerLandscapeColor: Color = Color.White,       // 👈 Hamburger colors
    hamburgerPortraitColor: Color = Color.Black
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hamburgerColor = if (isLandscape) hamburgerLandscapeColor else hamburgerPortraitColor

    if (isLandscape) {
        // -------- Landscape --------
        Row(Modifier.fillMaxSize()) {
            // Left side with top-left icon
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    content = topLeftContent
                )
                Box(modifier = Modifier.fillMaxSize()) { topOrLeft() }
            }

            // Right side with top-right icon
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius))
                    .background(landscapeColor)
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    topRightContent(hamburgerColor) // 👈 Pass dynamic color
                }
                Box(modifier = Modifier.fillMaxSize()) { bottomOrRight() }
            }
        }
    } else {
        // -------- Portrait --------
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                // Top section with top-left icon
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        content = topLeftContent
                    )
                    Box(modifier = Modifier.fillMaxSize()) { topOrLeft() }
                }

                // Bottom section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            color = portraitColor,
                            shape = RoundedCornerShape(
                                topStart = cornerRadius,
                                topEnd = cornerRadius
                            )
                        )
                        .padding(innerPadding)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) { bottomOrRight() }
                }
            }

            // Top-right hamburger floats over everything
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                topRightContent(hamburgerColor) // 👈 Pass dynamic color
            }
        }
    }
}

