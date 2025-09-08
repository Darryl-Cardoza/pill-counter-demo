package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium

@Composable
fun DrugDetails(
    drugName: String = "",
    ndc: String = "",
    expiry: String = "",
    lotNo: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = medium)) {
        DetailRow(label = "Drug name", value = drugName) //not add strings for now, will add after anurag,s resources setup merge
        HorizontalDivider()
        DetailRow(label = "NDC", value = ndc)
        HorizontalDivider()
        DetailRow(label = "Expiry", value = expiry)
        HorizontalDivider()
        DetailRow(label = "Lot No.", value = lotNo)
    }
}