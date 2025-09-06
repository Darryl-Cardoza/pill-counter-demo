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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.AppConstants.FIXED_COUNT
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import androidx.compose.ui.res.stringResource


@Composable
fun FixedCountSection(completedFixedCount: String, partialFixedCount: String, navController: NavController) {
    val context = LocalContext.current


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon inside circle
        Image(
            painter = painterResource(id = R.drawable.fixed_count),
            contentDescription = "Pill Counting Logo",
            modifier = Modifier.clickable {
                Screen.ScanBarcode.createRoute(FIXED_COUNT)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.fixed_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickable{
                Screen.ScanBarcode.createRoute(FIXED_COUNT)
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.fixed_count_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier.clickable{
                Screen.ScanBarcode.createRoute(FIXED_COUNT)
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
                text = "$completedFixedCount ${stringResource(R.string.completed)}",
                backgroundColor = Color.Transparent,
                textColor = MaterialTheme.colorScheme.secondary,
                iconRes = R.drawable.tick,
                iconTint = MaterialTheme.colorScheme.secondary
            )

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
