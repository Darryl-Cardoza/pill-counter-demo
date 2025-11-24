package com.rite.pillcounting.feature.barcodeScan.domain.data

/**
 * Defines the user interactions (events) that can occur on the ScanBarCodeScreen.
 */
sealed interface ScanBarcodeEvent {
    data object RedoScan : ScanBarcodeEvent
    data object ManualPillInfo : ScanBarcodeEvent
    data object StartCount : ScanBarcodeEvent
    data class BarcodeScanned(val gtin14: String, val imagePath: String, val expiry: String, val lotNo: String) : ScanBarcodeEvent
    data class ScannerError(val exception: Exception) : ScanBarcodeEvent
}