package com.rite.pillcounting.feature.barcodeScan.domain.data

/**
 * Defines the user interactions (events) that can occur on the ScanBarCodeScreen.
 */
sealed interface ScanBarcodeEvent {
    data object RedoScan : ScanBarcodeEvent
    data object manualPillInfo : ScanBarcodeEvent
    data object StartCount : ScanBarcodeEvent
    data class BarcodeScanned(val barcodeValue: String, val imagePath: String) : ScanBarcodeEvent
    data class ScannerError(val exception: Exception) : ScanBarcodeEvent
}