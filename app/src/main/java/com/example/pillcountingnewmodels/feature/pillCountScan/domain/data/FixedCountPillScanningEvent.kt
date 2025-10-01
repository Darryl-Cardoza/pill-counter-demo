package com.example.pillcountingnewmodels.feature.pillCountScan.domain.data

sealed interface FixedCountPillScanningEvent {
    data object AddTransactionDetailClicked : FixedCountPillScanningEvent

    data class TransactionDetailDeleted(val txnDetailId: Long) : FixedCountPillScanningEvent
    data object RescanClicked : FixedCountPillScanningEvent
    data object PauseClicked : FixedCountPillScanningEvent
    data object DoneClicked : FixedCountPillScanningEvent
    data object ConfirmDone : FixedCountPillScanningEvent
    data object CancelDone : FixedCountPillScanningEvent
}