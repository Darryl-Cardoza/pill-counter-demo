package com.example.pillcountingnewmodels.feature.pill_count.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastCbrt
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.pagePadding
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.xxLarge
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton
import com.example.pillcountingnewmodels.core.utils.compose.MenuButton
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.ui.theme.AppTheme

@Composable
fun ScanBarCodeScreen(
    navController: NavController,
    scanType: String,
) {
    val context = LocalContext.current


    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(Color.Gray) //temp
    ) {
        BackButton(navController)

        SplitResponsive(
            portraitRatio = 0.5f to 0.5f,
            landscapeRatio = 0.65f to 0.35f,
            topOrLeft = {
                Box(modifier = Modifier.background(Color.Black)) //temp
            },
            bottomOrRight = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = context.getString(R.string.barcode_scan).uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.extendedColors.textColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(top = medium)
                    )

                    DrugDetails()

                    BottomButtons(
                        onRedo = {
                            // Handle Redo click
                        },
                        onSkip = {
                            // Handle Skip click
                        },
                        onScan = {
                            // Handle Scan click
                        }
                    )
                }
            }
        )

        MenuButton(
            navController,
            modifier = Modifier
                .align(Alignment.TopEnd)
        )
    }
}


@Composable
fun DrugDetails(
    drugName: String = "",
    ndc: String = "",
    expiry: String = "",
    lotNo: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DetailRow(label = "Drug name", value = drugName) //not add strings for now, will add after anurag,s resources setup merge
        HorizontalDivider()
        DetailRow(label = "NDC", value = ndc)
        HorizontalDivider()
        DetailRow(label = "Expiry", value = expiry)
        HorizontalDivider()
        DetailRow(label = "Lot No.", value = lotNo)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(medium),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor.copy(alpha = 0.7f),
            modifier = Modifier
                .weight(0.35f)
                .wrapContentWidth(Alignment.Start) // align text start inside weight
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.extendedColors.textColor,
            modifier = Modifier
                .weight(0.65f)
                .wrapContentWidth(Alignment.Start) // align text start inside weight
        )
    }
}


@Composable
fun BottomButtons(
    onRedo: () -> Unit,
    onSkip: () -> Unit,
    onScan: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = xxLarge),
        horizontalArrangement = Arrangement.Center, // space between buttons
    ) {
        HollowButton(
            text = "Redo".uppercase(),
            onClick = onRedo,
            color = MaterialTheme.colorScheme.primary

        )
        Spacer(modifier = Modifier.width(medium))
        HollowButton(
            text = "Skip".uppercase(),
            onClick = onSkip,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(medium))
        ActionButtonPrimary(
            text = "Scan".uppercase(),
            onClick = onScan,
            color = MaterialTheme.colorScheme.secondary,
            useContentPadding = false
        )
    }
}
