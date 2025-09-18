package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.BottomButtons
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.DrugDetails
import com.example.pillcountingnewmodels.ui.theme.AppTheme


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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(modifier = Modifier.weight(1f))

                // Title text in the center
                Text(
                    text = stringResource(R.string.barcode_scan).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    color = AppTheme.extendedColors.textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        top = 8.dp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

            }
        }

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
