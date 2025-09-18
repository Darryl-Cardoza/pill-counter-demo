package com.example.pillcountingnewmodels.feature.pillCountScan.domain.model

import android.graphics.Bitmap

/**
 * Represents a batch of scanned pills.
 *
 * Each batch stores its unique batch number, the total count of pills detected,
 * and an optional thumbnail image representing the batch.
 *
 * @param batchNumber Identifier for the batch.
 * @param count Total number of pills detected in this batch.
 * @param thumbnail A thumbnail image of the batch (nullable if not available).
 */
data class Batch(
    val batchNumber: Int,
    val count: Int,
    val thumbnail: Bitmap?
)