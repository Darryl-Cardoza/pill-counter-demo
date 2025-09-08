package com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.model

import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data.DetectedPill

data class FixedCountPillScanningUiState(
    val drugName: String = "Crocin 50mg",
    val batchNumber: Int = 8,
    val totalCount: Int = 485,
    val expectedCount: Int = 1000,
    val currentScanCount: Int = 45,
    val batchHistory: List<Batch> = listOf(
        Batch(12, 45),Batch(11, 45), Batch(10, 45), Batch(9, 45), Batch(8, 45), Batch(7, 30), Batch(6, 50), Batch(5, 40), Batch(4, 45), Batch(3, 45)
    ),
    val detectedPills: List<DetectedPill> = emptyList(),
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Batch(
    val batchNumber: Int,
    val count: Int
)
