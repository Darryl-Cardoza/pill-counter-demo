package com.example.pillcountingnewmodels.feature.dashboard.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.AppConstants.REGULAR_COUNT
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun RegularCountSection(
    completedRegularCount: String,
    partialRegularCount: String,
    navController: NavController
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon inside circle
        Image(
            painter = painterResource(id = R.drawable.regular_count),
            contentDescription = "Pill Counting Logo",
            modifier = Modifier.clickable {
                Screen.ScanBarcode.createRoute(REGULAR_COUNT)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.regular_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                Screen.ScanBarcode.createRoute(REGULAR_COUNT)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.regular_count_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier.clickable {
                Screen.ScanBarcode.createRoute(REGULAR_COUNT)
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Status Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(
                text = "$completedRegularCount ${stringResource(R.string.completed)}",
                backgroundColor = Color.Transparent,
                textColor = AppTheme.extendedColors.textColor,
                iconRes = R.drawable.tick,
                iconTint = MaterialTheme.colorScheme.primary
            )

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