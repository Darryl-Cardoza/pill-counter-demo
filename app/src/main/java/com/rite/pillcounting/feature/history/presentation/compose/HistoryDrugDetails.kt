package com.rite.pillcounting.feature.history.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.constants.Dimens.medium

@Composable
fun HistoryDrugDetails(
    ndc: String = "",
    expiry: String = "",
    lotNo: String = "",
    date: String = "",
    time: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = medium)) {
        DetailRow(label = stringResource(R.string.ndc).uppercase(), value = ndc)
        HorizontalDivider()
        DetailRow(label = stringResource(R.string.expiry), value = expiry)
        HorizontalDivider()
        DetailRow(label = stringResource(R.string.lotNo), value = lotNo)
        HorizontalDivider()
        DetailRow(label = stringResource(R.string.date), value = date)
        HorizontalDivider()
        DetailRow(label = stringResource(R.string.time), value = time)

    }
}