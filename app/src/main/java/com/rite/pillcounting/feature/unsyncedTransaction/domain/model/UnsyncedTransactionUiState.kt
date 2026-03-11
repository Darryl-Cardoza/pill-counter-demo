package com.rite.pillcounting.feature.unsyncedTransaction.domain.model

import com.rite.pillcounting.feature.countResume.domain.model.CountItem

data class UnsyncedTransactionUiState(
    val unsyncedTransactionList: List<CountItem> = emptyList()
)