package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.hilt.navigation.compose.hiltViewModel
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel

@Composable
fun CircularCountIndicator(
    count: Int,
    modifier: Modifier = Modifier,
    viewModel: PillScanningViewModel = hiltViewModel()
) {
    val centerColor = MaterialTheme.colorScheme.primary
    val indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
    val lastDetections by viewModel.lastTenDetections.collectAsState()

    // Check if last 4 are identical
    val lastFourSame = remember(lastDetections) {
        lastDetections.toList().takeLast(4).let { lastFour ->
            lastFour.size == 4 && lastFour.distinct().size == 1
        }
    }

    // Infinite animation for outer circle
    val infiniteTransition = rememberInfiniteTransition()
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.size(115.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()

            // If last 4 are same, show full circle (360°), else animate
            val sweepAngle = if (lastFourSame) 360f else 360 * sweepProgress

            drawArc(
                color = indicatorColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner circle with count
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
                fontSize = 32.sp
            )
        }
    }
}

