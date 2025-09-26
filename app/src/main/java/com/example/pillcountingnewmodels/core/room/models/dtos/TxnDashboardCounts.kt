package com.example.pillcountingnewmodels.core.room.models.dtos

/**
 * Aggregated counters for the dashboard.
 */
data class TxnDashboardCounts(
    val completedFixed: Int,
    val partialFixed: Int,
    val completedRegular: Int,
    val partialRegular: Int
)