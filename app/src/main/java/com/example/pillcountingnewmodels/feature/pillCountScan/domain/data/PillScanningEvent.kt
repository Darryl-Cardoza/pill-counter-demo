package com.example.pillcountingnewmodels.feature.pillCountScan.domain.data

sealed interface PillScanningEvent {
    data class AddTransactionDetailClicked(val filteredCount: Int) : PillScanningEvent
    data class TransactionDetailDeleted(val txnDetailId: Long) : PillScanningEvent
    data object RescanClicked : PillScanningEvent
    data object PauseClicked : PillScanningEvent
    data object DoneClicked : PillScanningEvent
    data object ConfirmDone : PillScanningEvent
    data object CancelDone : PillScanningEvent
    data object NoteSkip : PillScanningEvent
    data class NoteSaved(val note: String) : PillScanningEvent
}