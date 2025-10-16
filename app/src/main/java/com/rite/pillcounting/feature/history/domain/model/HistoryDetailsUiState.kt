package com.rite.pillcounting.feature.history.domain.model

import com.rite.pillcounting.core.room.models.dtos.TxnWithDetails

data class HistoryDetailsUiState(
    val txnInfo: TxnWithDetails? = null
)
