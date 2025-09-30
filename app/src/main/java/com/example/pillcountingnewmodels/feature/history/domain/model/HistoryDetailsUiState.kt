package com.example.pillcountingnewmodels.feature.history.domain.model

data class HistoryDetailsUiState(
    val pillCount: Int = 0,
    val ndc: String? = null,
    val expiry: String? = null,
    val lotNo: String? = null,
    val dateTime: Long = 0,
    val note: String? = null,
)
