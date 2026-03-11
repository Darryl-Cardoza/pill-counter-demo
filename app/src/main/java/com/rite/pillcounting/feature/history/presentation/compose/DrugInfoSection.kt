package com.rite.pillcounting.feature.history.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.rite.pillcounting.core.room.models.dtos.TxnDetailInfo
import com.rite.pillcounting.core.room.models.enums.CountType

@Composable
fun DrugInfoSection(
    ndc: String,
    expiry: String,
    lotNo: Any,
    date: String,
    time: String,
    note: String?,
    totalPillCount: String,
    targetCount: Int?,
    barcodeImage: String?,
    transactionDetails: List<TxnDetailInfo>,
    onDelete: () -> Unit,
    onOk: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(0.85f)) {
                Row(
                    modifier = Modifier.weight(0.8f),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .padding(start = 20.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ImageWithCount(totalPillCount, targetCount,barcodeImage)
                        Spacer(Modifier.height(15.dp))
                        HistoryNote(note)
                    }

                    Box(
                        modifier = Modifier.weight(0.4f),
                        contentAlignment = Alignment.Center
                    ) {
                        HistoryDrugDetails(ndc, expiry, lotNo.toString(), date, time)
                    }
                }

                ButtonsRow(onDelete = onDelete, onOk = onOk)
            }

            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxHeight()
            ) {
                TransactionDetailsList(transactionDetails)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ImageWithCount(totalPillCount, targetCount ,barcodeImage)
            HistoryDrugDetails(ndc, expiry, lotNo.toString(), date, time)
            HistoryNote(note)
            TransactionDetailsList(transactionDetails)
            ButtonsRow(onDelete = onDelete, onOk = onOk)
        }
    }
}
