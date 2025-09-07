package com.example.pillcountingnewmodels.feature.dashboard.domain.model

/**
 * Represents the aggregated UI state for the Dashboard screen.
 *
 * This state exposes the counts for both fixed and regular counting flows,
 * including completed and partial progress values.
 *
 * Each property is represented as a [String] since the values
 * are expected to be displayed directly in the UI layer.
 */
data class DashboardUiState(

    /** Number of fixed counts that have been fully completed. */
    val completedFixedCount: String = "0",

    /** Number of fixed counts that are in progress or partially completed. */
    val partialFixedCount: String = "0",

    /** Number of regular counts that have been fully completed. */
    val completedRegularCount: String = "0",

    /** Number of regular counts that are in progress or partially completed. */
    val partialRegularCount: String = "0"
)
