package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.model.FixedCountPillScanningUiState

/**
 * Displays the current scanned pill count along with an "ADD" button.
 *
 * Layout:
 * - Circular indicator for the current count.
 * - Button to increment/add the count.
 *
 * @param uiState The current state of the scanning screen.
 * @param onAddClicked Lambda invoked when the "ADD" button is pressed.
 */
@Composable
fun CurrentCountDisplay(
    uiState: FixedCountPillScanningUiState,
    onAddClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Circular count indicator
        CircularCountIndicator(count = uiState.currentScanCount)

        // "ADD" button
        Button(
            onClick = onAddClicked,
            modifier = Modifier
                .size(width = 100.dp, height = 40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFD82B5),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "ADD",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
