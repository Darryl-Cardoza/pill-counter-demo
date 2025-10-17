package com.rite.pillcounting.feature.barcodeScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.medium
import com.rite.pillcounting.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.rite.pillcounting.ui.theme.AppTheme


/**
 * The panel displaying drug information and action buttons.
 *
 * @param drugName The name of the scanned drug.
 * @param ndc The National Drug Code of the scanned drug.
 * @param onEvent The callback for user-initiated events.
 */
@Composable
fun InformationPanel(
    navController: NavController,
    drugName: String,
    ndc: String,
    onEvent: (ScanBarcodeEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.barcode_scan).uppercase(),
            fontSize = 16.sp,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
            color = AppTheme.extendedColors.textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = medium)
        )

        DrugDetails(
            drugName = drugName,
            ndc = ndc
        )

        BottomButtons(
            onRedo = { onEvent(ScanBarcodeEvent.RedoScan) },
            onManual = { onEvent(ScanBarcodeEvent.manualPillInfo) },
            onCount = { onEvent(ScanBarcodeEvent.StartCount) }
        )
    }
}
