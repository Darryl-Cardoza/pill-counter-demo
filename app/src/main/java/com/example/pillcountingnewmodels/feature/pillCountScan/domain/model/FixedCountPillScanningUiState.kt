package com.example.pillcountingnewmodels.feature.pillCountScan.domain.model

/**
 * Represents the UI state for the fixed pill counting process.
 *
 * This state is used to track the progress of scanning and counting pills,
 * including batch information, detected pills, and system flags such as
 * pause or loading status.
 *
 * @param drugName Name of the drug being scanned (e.g., "Crocin 50mg").
 * @param batchNumber Current batch number being processed.
 * @param totalCount Total number of pills counted across all batches so far.
 * @param expectedCount Expected total number of pills to be scanned.
 * @param currentScanCount Number of pills detected in the current scan session.
 * @param batchHistory List of completed batches with their counts and thumbnails.
 * @param detectedPills List of pills detected in the current camera frame.
 * @param isPaused Whether the scanning process is currently paused.
 * @param isLoading Whether a loading state is active (e.g., processing results).
 * @param error Error message if an error occurred during scanning or counting.
 */
data class FixedCountPillScanningUiState(
    val drugName: String = "Crocin 50mg",
    val batchNumber: Int = 8,
    val totalCount: Int = 485,
    val expectedCount: Int = 1000,
    val currentScanCount: Int = 45,
    val batchHistory: List<Batch> = emptyList(),
    val detectedPills: List<DetectedPill> = emptyList(),
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)


