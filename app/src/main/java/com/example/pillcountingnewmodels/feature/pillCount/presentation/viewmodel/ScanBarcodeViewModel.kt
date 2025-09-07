package com.example.pillcountingnewmodels.feature.pillCount.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.pillCount.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.pillCount.domain.data.ScanBarcodeEvent
import com.example.pillcountingnewmodels.feature.pillCount.domain.model.ScanBarcodeUiState
import com.example.pillcountingnewmodels.feature.pillCount.domain.repository.IDrugRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel for the ScanBarCodeScreen.
 * It handles the business logic, manages the screen's state, and responds to UI events.
 *
 * @property savedStateHandle Handle to access navigation arguments.
 * @property drugRepository The repository for fetching drug information.
 */
@HiltViewModel
class ScanBarcodeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val drugRepository: IDrugRepository
) : ViewModel() {

    private val logger = AppLogger.create<ScanBarcodeViewModel>()

    private val _uiState = MutableStateFlow(ScanBarcodeUiState())
    val uiState: StateFlow<ScanBarcodeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        val scanType = savedStateHandle.get<String>(ARG_TYPE) ?: "regular"
        _uiState.update { it.copy(scanType = scanType) }
        logger.i("ViewModel initialized with scanType: '$scanType'")
    }

    /**
     * Central handler for all incoming UI events.
     * It logs the event and delegates it to the appropriate private function for processing.
     * @param event The [ScanBarcodeEvent] triggered by the user or the system.
     */
    fun onEvent(event: ScanBarcodeEvent) {
        logger.d("Received event: ${event::class.java.simpleName}")
        when (event) {
            is ScanBarcodeEvent.BarcodeScanned -> processBarcode(event.barcodeValue)
            is ScanBarcodeEvent.ScannerError -> handleScannerError(event.exception)
            ScanBarcodeEvent.ConfirmScan -> handleConfirmScan()
            ScanBarcodeEvent.RedoScan -> handleRedoScan()
            ScanBarcodeEvent.SkipScan -> handleSkipScan()
        }
    }

    /**
     * Processes the scanned barcode value.
     * It sets the UI to a loading state, fetches drug information from the repository
     * with a simulated 2-second delay, and updates the state with the result.
     * On success, it pauses the scanner to prevent immediate re-scans.
     * @param barcodeValue The raw string value from the scanned barcode.
     */
    private fun processBarcode(barcodeValue: String) {
        if (uiState.value.isLoading) return // Prevent processing if already in progress

        logger.i("Processing barcode: $barcodeValue")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {

                val drugInfo = drugRepository.getDrugInfoByNdc(barcodeValue)

                if (drugInfo != null) {
                    val displayName = when {
                        !drugInfo.brandName.isNullOrBlank() -> drugInfo.brandName
                        !drugInfo.genericName.isNullOrBlank() -> drugInfo.genericName
                        else -> "Unknown Drug"
                    }
                    logger.i("Successfully fetched drug info for barcode $barcodeValue")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            drugName = displayName,
                            ndc = drugInfo.ndc,
                            isScannerActive = false // Pause scanner on successful scan
                        )
                    }
                } else {
                    throw Exception("No drug information found for this NDC.")
                }
            } catch (e: Exception) {
                logger.e("Failed to fetch drug info for barcode $barcodeValue", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to find drug information."
                    )
                }
            }
        }
    }

    /**
     * Resets the UI state to allow for a new scan.
     * It clears any previously scanned drug name and NDC, removes error messages,
     * and importantly, sets the scanner to active.
     */
    private fun handleRedoScan() {
        logger.d("Redo scan clicked. Clearing state and resuming scanner.")
        _uiState.update {
            it.copy(
                drugName = "",
                ndc = "",
                error = null,
                isScannerActive = true // Resume scanner
            )
        }
    }

    /**
     * Handles the confirmation of a scanned item.
     * It checks if an NDC is present and, if so, sends a navigation event
     * to proceed to the next screen. If no NDC is present, it shows an error.
     */
    private fun handleConfirmScan() {
        val currentNdc = uiState.value.ndc
        if (currentNdc.isBlank()) {
            logger.w("ConfirmScan ignored: NDC is blank.")
            _uiState.update { it.copy(error = "Please scan an item first.") }
            return
        }
        logger.d("Confirm scan clicked. Navigating to pill count.")
        viewModelScope.launch {
            _navigationEvent.send(NavigationEvent.NavigateToPillCount(currentNdc))
        }
    }

    /**
     * Handles the user's choice to skip the scanning process.
     * It sends a one-time navigation event to go back to the previous screen.
     */
    private fun handleSkipScan() {
        logger.d("Skip scan clicked. Navigating back.")
        viewModelScope.launch {
            _navigationEvent.send(NavigationEvent.NavigateBack)
        }
    }

    /**
     * Handles errors reported by the barcode analyzer.
     * It logs the exception and updates the UI state with a generic, user-friendly
     * error message.
     * @param exception The exception caught by the scanner component.
     */
    private fun handleScannerError(exception: Exception) {
        logger.e("Received scanner error.", exception)
        _uiState.update { it.copy(error = "Scanner failed. Please try again.") }
    }

    companion object {
        const val ARG_TYPE = "type"
    }
}

