package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A circular indicator showing a pill count in the center, surrounded by a decorative ring.
 *
 * The design mimics a progress-style circular indicator but is static.
 *
 * @param count The numeric value to display in the center.
 * @param modifier Modifier applied to the root Box.
 */
@Composable
fun CircularCountIndicator(
    count: Int,
    modifier: Modifier = Modifier
) {
    // Colors for outer ring and inner circle
    val indicatorColor = Color(0xFFEC407A)
    val centerColor = Color(0xFF26C6DA)

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer decorative ring
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp, // Thinner border
                    color = indicatorColor.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        )

        // Inner circle containing the count
        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .clip(CircleShape)
                .background(centerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
