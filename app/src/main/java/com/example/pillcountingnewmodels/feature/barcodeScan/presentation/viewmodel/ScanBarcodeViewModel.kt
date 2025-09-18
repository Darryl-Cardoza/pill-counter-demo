package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.DrugMasterDao
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.core.room.models.CountStatus
import com.example.pillcountingnewmodels.core.room.models.CountType
import com.example.pillcountingnewmodels.core.room.models.DrugMasterEntity
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnEntity
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.IDrugRepository
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.model.ScanBarcodeUiState
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
    private val drugRepository: IDrugRepository,
    private val drugMasterDao: DrugMasterDao,
    private val preferenceHelper: PreferenceHelper,
    private val pillCountTxnDao: PillCountTxnDao
) : ViewModel() {

    private val logger = AppLogger.create<ScanBarcodeViewModel>()

    private val _uiState = MutableStateFlow(ScanBarcodeUiState())
    val uiState: StateFlow<ScanBarcodeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        val scanType = savedStateHandle.get<String>(ARG_TYPE) ?: ""
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
            ScanBarcodeEvent.StartCount -> handleStartCount()
            ScanBarcodeEvent.RedoScan -> handleRedoScan()
            ScanBarcodeEvent.manualPillInfo -> showManualEntryDialog()
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
        _uiState.update { it.copy(isLoading = true, error = null) }
        logger.i("Processing barcode: $barcodeValue")

        viewModelScope.launch {
            val drug = drugMasterDao.getDrugByNdc(barcodeValue)
            if (drug != null) {
                logger.i("Drug exists: ${drug.drugName}")
                // use the record directly
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        drugName = drug.drugName ?: "",
                        ndc = drug.ndc,
                        isScannerActive = false // Pause scanner on successful scan
                    )
                }
            } else {
                logger.i("Drug not found, fetching from API...")
                try {
                    //val drugInfo = drugRepository.getDrugInfoByNdc("59779-311")
                    val drugInfo = drugRepository.getDrugInfoByNdc(barcodeValue)
                    if (drugInfo != null) {
                        val displayName = when {
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
                            error = e.message ?: "Failed to find drug information.",
                            showManualEntry = true
                        )
                    }
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
    private fun handleStartCount() {
        val currentNdc = uiState.value.ndc
        val currentDrugName = uiState.value.drugName
        if (currentNdc.isBlank()) {
            logger.w("ConfirmScan ignored: NDC is blank.")
            _uiState.update { it.copy(error = "Please scan an item first.") }
            return
        }
        logger.d("Confirm scan clicked. Navigating to pill count.")
        viewModelScope.launch {
            val drugId = drugMasterDao.upsertAndReturnId(currentNdc, currentDrugName)
            val localId =  preferenceHelper.getLocalId()
            logger.i(message = "local id in scan--->   $localId")
            val txn = PillCountTxnEntity(
                localId = localId,
                drugId = drugId,
                countType = when (uiState.value.scanType){
                    "FIXED" -> CountType.FIXED
                    "REGULAR" -> CountType.REGULAR
                    else -> CountType.REGULAR
                },
                status = CountStatus.PARTIAL,
                expiry = uiState.value.expiry,
                lotNo = uiState.value.lotNo
            )

            val txnId = pillCountTxnDao.insert(txn)
            preferenceHelper.saveTxnId(txnId)
            logger.d("active transaction id -->  $txnId")
            _navigationEvent.send(NavigationEvent.NavigateToPillCount(currentNdc))
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

    fun addManualDrug(drugName: String, ndc: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                drugName = drugName,
                ndc = ndc,
                isScannerActive = false, // Pause scanner on successful scan
                showManualEntry = false, //hiding dialog
                error = null
            )
        }
    }

    fun showManualEntryDialog(){
        _uiState.update {
            it.copy(
                showManualEntry = true,
                error = null
            )
        }
    }

    fun hideManualEntryDialog(){
        _uiState.update {
            it.copy(
                showManualEntry = false,
                error = null
            )
        }
    }

    companion object {
        const val ARG_TYPE = "type"
    }
}

