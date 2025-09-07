package com.example.pillcountingnewmodels.feature.pillCount.domain.data

/**
 * Defines the user interactions (events) that can occur on the ScanBarCodeScreen.
 */
sealed interface ScanBarcodeEvent {
    data object RedoScan : ScanBarcodeEvent
    data object SkipScan : ScanBarcodeEvent
    data object ConfirmScan : ScanBarcodeEvent
    data class BarcodeScanned(val barcodeValue: String) : ScanBarcodeEvent
    data class ScannerError(val exception: Exception) : ScanBarcodeEvent
}