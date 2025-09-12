package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * Top App Bar for the Fixed Pill Count Scanning screen.
 *
 * Layout:
 * - Back button on the left
 * - Title centered
 * - Menu button on the right
 *
 * @param navController Navigation controller for handling back/menu actions.
 */
@Composable
fun TopAppBar(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.weight(1f))

            // Title text
            Text(
                text = stringResource(R.string.pill_count_title).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                color = AppTheme.extendedColors.textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
