package com.example.pillcountingnewmodels.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive

@Composable
fun DashboardScreen(navController: NavController) {
    SplitResponsive(
        topOrLeft = { FixedCountSection() },
        bottomOrRight = { RegularCountSection() },
        cornerRadius = 40.dp,
        innerPadding = 24.dp

    )
}

@Composable
fun FixedCountSection() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon inside circle
        Box(
            modifier = Modifier
                .size(350.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.fixed_count), // replace with your image
                contentDescription = "Pill Counting Logo",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Fixed Count",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )

        // Subtitle
        Text(
            text = "For counting fixed quantities",
            fontSize = 22.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // 👈 Spreads items across full width
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(
                text = "45 completed",
                color = Color.Red,
                imageRes = R.drawable.tick_pink
            )

            StatusChip(
                text = "8 partial",
                color = Color.Red,
                imageRes = R.drawable.partial_pink
            )
        }
    }
}

@Composable
fun RegularCountSection() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon inside circle
        Box(
            modifier = Modifier
                .size(350.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.regular_count), // replace with your image
                contentDescription = "Pill Counting Logo",
//                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Regular Count",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Subtitle
        Text(
            text = "For day to day regular counts",
            fontSize = 22.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(
                text = "45 completed",
                color = Color.White,
                imageRes = R.drawable.tick_green
            )

            StatusChip(
                text = "8 partial",
                color = Color.White.copy(alpha = 0.7f),
                imageRes = R.drawable.partial_pink
            )
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    imageRes: Int // pass drawable resource here
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
//            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(26.dp) // adjust size as needed
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = text,
                color = color,
                fontSize = 20.sp
            )
        }
    }
}


