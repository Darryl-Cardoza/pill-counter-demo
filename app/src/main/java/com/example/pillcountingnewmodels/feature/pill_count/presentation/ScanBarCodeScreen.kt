package com.example.pillcountingnewmodels.feature.pill_count.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.AppInfo
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.pagePadding
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
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        BackButton(navController)

        SplitResponsive(
            topOrLeft = {
                AppInfo(context)
            },
            bottomOrRight = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = pagePadding),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = context.getString(R.string.barcode_scan),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.extendedColors.textColor,
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
    drugName: String = "-",
    ndc: String = "-",
    expiry: String = "-",
    lotNo: String = "-"
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DetailRow(label = "Drug name", value = drugName)
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
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
