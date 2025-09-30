package com.example.pillcountingnewmodels.feature.history.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.DetailRow

@Composable
fun HistoryDrugDetails(
    ndc: String = "",
    expiry: String = "",
    batch: String = "",
    date: String = "",
    time: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = medium)) {
        DetailRow(label = "NDC", value = ndc)
        HorizontalDivider()
        DetailRow(label = "Expiry", value = expiry)
        HorizontalDivider()
        DetailRow(label = "Batch", value = batch)
        HorizontalDivider()
        DetailRow(label = "Date", value = date)
        HorizontalDivider()
        DetailRow(label = "Time", value = time)

    }
}