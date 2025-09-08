package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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