package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.model.Batch

/**
 * Horizontal scrollable row displaying a history of counted batches.
 *
 * Each batch is represented by a [Chip] that shows:
 * - The count of pills
 * - The batch number as a badge
 *
 * @param batches List of batches to display.
 */
@Composable
fun BatchHistory(
    batches: List<Batch>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp), // spacing between chips
        contentPadding = PaddingValues(horizontal = 16.dp)  // horizontal padding at start/end
    ) {
        items(batches) { batch ->
            // Each batch is rendered as a Chip with count and batch number
            Chip(
                count = batch.count.toString(),
                batchNumber = batch.batchNumber
            )
        }
    }
}
