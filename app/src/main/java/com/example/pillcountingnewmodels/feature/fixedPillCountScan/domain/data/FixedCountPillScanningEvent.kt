package com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data

sealed interface FixedCountPillScanningEvent {
    data object AddBatchClicked : FixedCountPillScanningEvent
    data object RescanClicked : FixedCountPillScanningEvent
    data object PauseClicked : FixedCountPillScanningEvent
    data object DoneClicked : FixedCountPillScanningEvent
}