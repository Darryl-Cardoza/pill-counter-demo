package com.example.pillcountingnewmodels.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.AppConstants.FIXED_COUNT
import com.example.pillcountingnewmodels.core.utils.AppConstants.REGULAR_COUNT
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.MenuButton
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.navigation.Routes.SCAN_BARCODE
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun DashboardScreen(navController: NavController) {

    var completedFixedCount by remember { mutableStateOf("0") }
    var partialFixedCount by remember { mutableStateOf("0") }
    var completedRegularCount by remember { mutableStateOf("0") }
    var partialRegularCount by remember { mutableStateOf("0") }

    Box(modifier = Modifier.background(AppTheme.extendedColors.secondaryBackground)) {
        SplitResponsive(
            topOrLeft = {
                FixedCountSection(
                    completedFixedCount = completedFixedCount,
                    partialFixedCount = partialFixedCount,
                    navController = navController
                )
            },
            bottomOrRight = {
                RegularCountSection(
                    completedRegularCount = completedRegularCount,
                    partialRegularCount = partialRegularCount,
                    navController = navController
                )
            },
        )

        MenuButton(
            navController,
            modifier = Modifier
                .align(Alignment.TopEnd)
        )
    }
}

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
                startScan(navController, FIXED_COUNT)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = context.getString(R.string.fixed_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickable{
                startScan(navController, FIXED_COUNT)
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = context.getString(R.string.fixed_count_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier.clickable{
                startScan(navController, FIXED_COUNT)
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
                text = "$completedFixedCount ${context.getString(R.string.completed)}",
                backgroundColor = Color.Transparent,
                textColor = MaterialTheme.colorScheme.secondary,
                iconRes = R.drawable.tick,
                iconTint = MaterialTheme.colorScheme.secondary
            )

            StatusChip(
                text = "$partialFixedCount ${context.getString(R.string.partial)}",
                backgroundColor = AppTheme.extendedColors.statusChipBackgroundOnSecondary,
                textColor = MaterialTheme.colorScheme.secondary,
                iconRes = R.drawable.partial,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

fun startScan(navController: NavController, countType: String) {
    navController.navigate("${SCAN_BARCODE}/${countType}")
}

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
                startScan(navController, REGULAR_COUNT)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = context.getString(R.string.regular_count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                startScan(navController, REGULAR_COUNT)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = context.getString(R.string.regular_count_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier.clickable {
                startScan(navController, REGULAR_COUNT)
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
                text = "$completedRegularCount ${context.getString(R.string.completed)}",
                backgroundColor = Color.Transparent,
                textColor = AppTheme.extendedColors.textColor,
                iconRes = R.drawable.tick,
                iconTint = MaterialTheme.colorScheme.primary
            )

            StatusChip(
                text = "$partialRegularCount ${context.getString(R.string.partial)}",
                backgroundColor = AppTheme.extendedColors.statusChipBackgroundOnPrimary,
                textColor = AppTheme.extendedColors.textColor,
                iconRes = R.drawable.partial,
                iconTint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    iconRes: Int,
    iconTint: Color?
) {
    Row(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,

    ) {
        Spacer(modifier = Modifier.width(3.dp))
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(25.dp),
            colorFilter = iconTint?.let { ColorFilter.tint(it) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(3.dp))
    }
}



