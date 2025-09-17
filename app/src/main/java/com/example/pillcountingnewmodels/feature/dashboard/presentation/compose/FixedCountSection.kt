package com.example.pillcountingnewmodels.feature.dashboard.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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

/**
 * Dashboard section for displaying the "Fixed Count" workflow.
 *
 * This composable renders:
 * - A fixed count icon that navigates to the barcode scanning screen.
 * - Title and description labels (both clickable for navigation).
 * - Status chips showing completed and partial counts.
 *
 * @param completedFixedCount The number of completed fixed counts (as string for direct UI rendering).
 * @param partialFixedCount The number of partially completed fixed counts.
 * @param navController Navigation controller used for screen transitions.
 */
@Composable
fun FixedCountSection(
    completedFixedCount: String,
    partialFixedCount: String,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Fixed count icon leading to ScanBarcode screen
        Image(
            painter = painterResource(id = R.drawable.fixed_count),
            contentDescription = stringResource(R.string.fixed_count),
            modifier = Modifier
                .size(120.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    navigateToBarcodeScanFixedCount(navController)
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Fixed Count title (clickable -> ScanBarcode)
        Text(
            text = stringResource(R.string.fixed_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                navigateToBarcodeScanFixedCount(navController)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Description text (clickable -> ScanBarcode)
        Text(
            text = stringResource(R.string.fixed_count_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                navigateToBarcodeScanFixedCount(navController)
            }
        )

       Spacer(modifier = Modifier.weight(1f))

        // Row of status chips for Completed & Partial
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completed status (static, non-clickable)
            StatusChip(
                text = "$completedFixedCount ${stringResource(R.string.completed)}",
                backgroundColor = Color.Transparent,
                textColor = MaterialTheme.colorScheme.secondary,
                iconRes = R.drawable.tick,
                iconTint = MaterialTheme.colorScheme.secondary
            )

            // Partial status (clickable -> ResumeFixedCounts screen)
            Box(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
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

        Spacer(modifier = Modifier.height(10.dp))
    }
}

fun navigateToBarcodeScanFixedCount(navController: NavController) {
    navController.navigate(Screen.ScanBarcode.createRoute(FIXED_COUNT))
}
