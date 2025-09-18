package com.example.pillcountingnewmodels.feature.barcodeScan.domain.model

/**
 * Represents the current state of the ScanBarCodeScreen.
 *
 * @property scanType The type of count being performed (e.g., "fixed", "regular").
 * @property drugName The name of the scanned drug, if any.
 * @property ndc The National Drug Code (NDC) of the scanned drug.
 * @property isLoading Indicates if a background operation is in progress.
 * @property error An error message to display to the user, if any.
 * @property isScannerActive Controls the active state of the barcode analyzer. True to scan, false to pause.
 */
data class ScanBarcodeUiState(
    val scanType: String = "",
    val drugName: String = "",
    val ndc: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isScannerActive: Boolean = true,
    val showManualEntry: Boolean = false
)