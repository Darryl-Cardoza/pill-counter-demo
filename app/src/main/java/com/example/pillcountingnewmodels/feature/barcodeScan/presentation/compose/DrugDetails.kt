package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.constants.Dimens.medium

@Composable
fun DrugDetails(
    drugName: String = "",
    ndc: String = "",
    expiry: String = "",
    lotNo: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = medium)) {
        DetailRow(label = stringResource(R.string.drugname), value = drugName) //not add strings for now, will add after anurag,s resources setup merge
        HorizontalDivider()
        DetailRow(label = stringResource(R.string.ndc).uppercase(), value = ndc)
        HorizontalDivider()
        DetailRow(label = stringResource(R.string.expiry), value = expiry)
        HorizontalDivider()
        DetailRow(label = stringResource(R.string.lotNo), value = lotNo)
    }
}