package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.Batch

/**
 * Displays a horizontal list of batch history as chips.
 *
 * Each batch is represented by a [Chip], which shows its information
 * such as batch number, count, and thumbnail (if available).
 *
 * @param batches List of [Batch] items to be displayed in the history row.
 */
@Composable
fun BatchHistory(
    batches: List<Batch>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(medium),
    ) {
        items(batches) { batch ->
            // Pass the entire batch object to the Chip
            Chip(batch = batch)
        }
    }
}