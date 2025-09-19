package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.TxnDetail

/**
 * Displays a horizontal list of batch history as chips.
 *
 * Each batch is represented by a [Chip], which shows its information
 * such as batch number, count, and thumbnail (if available).
 *
 * @param txnDetails List of [TxnDetail] items to be displayed in the history row.
 */
@Composable
fun TxnDetailHistory(
    txnDetails: List<TxnDetail>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(medium),
    ) {
        itemsIndexed(txnDetails) { index, txnDetail ->
            // Pass the entire batch object to the Chip
            Chip(txnDetail = txnDetail, index = txnDetails.size - index)
        }
    }
}