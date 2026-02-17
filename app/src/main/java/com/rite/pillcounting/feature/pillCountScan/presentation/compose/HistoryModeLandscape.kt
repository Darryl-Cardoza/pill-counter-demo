package com.rite.pillcounting.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rite.pillcounting.R
import com.rite.pillcounting.feature.pillCountScan.domain.model.TxnDetail
import com.rite.pillcounting.ui.theme.AppTheme

@Composable
fun HistoryModeLandscape(
    scanType: String,
    targetCount: Int,
    totalCount: Int,
    txnHistory: List<TxnDetail>,
    onDeleteTxn: (Long) -> Unit,
    drugName : String
) {
    val latestTxnId = txnHistory.maxByOrNull { it.createdAt }?.txnDetailId
    val activeHistory = txnHistory
        .filter { true }
        .sortedBy { it.createdAt }

    val listState = rememberLazyListState()

    LaunchedEffect(activeHistory.size) {
        if (activeHistory.isNotEmpty()) {
            listState.animateScrollToItem(activeHistory.lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxHeight(),
    ) {
        Spacer(modifier = Modifier.weight(0.8f))
        Text(
            text = drugName,
            color = AppTheme.extendedColors.textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.weight(0.8f))
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(
                items = activeHistory,
                key = { _, item -> item.txnDetailId }
            ) { index, item ->
                Chip(
                    txnDetail = item,
                    index = index + 1,
                    onDelete = onDeleteTxn,
                    count = item.count.toString(),
                    highlight = item.txnDetailId == latestTxnId
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.8f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = totalCount.toString(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            if (scanType == "FIXED") {
                Spacer(Modifier.height(4.dp))

                Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = targetCount.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.pill_scanning_total_count),
                color = AppTheme.extendedColors.textColor,
                fontSize = 12.sp
            )
        }
    }
}
