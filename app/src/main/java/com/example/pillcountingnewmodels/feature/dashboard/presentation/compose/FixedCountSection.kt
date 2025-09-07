package com.example.pillcountingnewmodels.feature.dashboard.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.AppConstants.FIXED_COUNT
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun FixedCountSection(
    completedFixedCount: String, partialFixedCount: String, navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(painter = painterResource(id = R.drawable.fixed_count),
            contentDescription = "Fixed Count",
            modifier = Modifier.clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                navController.navigate(Screen.ScanBarcode.createRoute(FIXED_COUNT))
            })

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(R.string.fixed_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                navController.navigate(Screen.ScanBarcode.createRoute(FIXED_COUNT))
            })
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = stringResource(R.string.fixed_count_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier.clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                navController.navigate(Screen.ScanBarcode.createRoute(FIXED_COUNT))
            })

        Spacer(modifier = Modifier.height(60.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(
                text = "$completedFixedCount ${stringResource(R.string.completed)}",
                backgroundColor = Color.Transparent,
                textColor = MaterialTheme.colorScheme.secondary,
                iconRes = R.drawable.tick,
                iconTint = MaterialTheme.colorScheme.secondary
            )

            // Updated to navigate to the correct partial counts screen
            Box(
                modifier = Modifier.clickable(indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    navController.navigate(Screen.ResumeFixedCounts.createRoute(FIXED_COUNT))
                }
            ) {
                StatusChip(
                    text = "$partialFixedCount ${stringResource(R.string.partial)}",
                    backgroundColor = AppTheme.extendedColors.statusChipBackgroundOnSecondary,
                    textColor = MaterialTheme.colorScheme.secondary,
                    iconRes = R.drawable.partial,
                    iconTint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
