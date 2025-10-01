package com.example.pillcountingnewmodels.feature.history.domain.model

import com.example.pillcountingnewmodels.core.room.models.dtos.TxnWithDetails

data class HistoryDetailsUiState(
    val txnInfo: TxnWithDetails? = null
)
