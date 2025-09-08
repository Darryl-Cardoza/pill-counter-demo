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
import com.example.pillcountingnewmodels.core.utils.AppConstants.REGULAR_COUNT
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * Dashboard section for displaying the "Regular Count" workflow.
 *
 * This composable renders:
 * - A regular count icon (clickable → navigates to ScanBarcode screen).
 * - Title and description labels (clickable for the same navigation).
 * - Status chips showing completed and partial regular counts.
 *
 * @param completedRegularCount Number of completed regular counts (as string for direct rendering).
 * @param partialRegularCount Number of partially completed regular counts.
 * @param navController Used for navigation between screens.
 */
@Composable
fun RegularCountSection(
    completedRegularCount: String,
    partialRegularCount: String,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Regular count icon (click → ScanBarcode)
        Image(
            painter = painterResource(id = R.drawable.regular_count),
            contentDescription = stringResource(R.string.regular_count),
            modifier = Modifier
                .size(120.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    navigateToBarcodeScanRegularCount(navController)
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Regular Count title (click → ScanBarcode)
        Text(
            text = stringResource(R.string.regular_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                navigateToBarcodeScanRegularCount(navController)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Description label (click → ScanBarcode)
        Text(
            text = stringResource(R.string.regular_count_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                navigateToBarcodeScanRegularCount(navController)
            }
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Status row (Completed + Partial counts)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completed status (static, non-clickable)
            StatusChip(
                text = "$completedRegularCount ${stringResource(R.string.completed)}",
                backgroundColor = Color.Transparent,
                textColor = AppTheme.extendedColors.textColor,
                iconRes = R.drawable.tick,
                iconTint = MaterialTheme.colorScheme.primary
            )

            // Partial status (click → ResumeRegularCounts screen)
            Box(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    navController.navigate(Screen.ResumeRegularCounts.createRoute(REGULAR_COUNT))
                }
            ) {
                StatusChip(
                    text = "$partialRegularCount ${stringResource(R.string.partial)}",
                    backgroundColor = AppTheme.extendedColors.statusChipBackgroundOnPrimary,
                    textColor = AppTheme.extendedColors.textColor,
                    iconRes = R.drawable.partial,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun navigateToBarcodeScanRegularCount(navController: NavController) {
    navController.navigate(Screen.ScanBarcode.createRoute(REGULAR_COUNT))
}