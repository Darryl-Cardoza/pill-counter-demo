package com.rite.pillcounting.feature.barcodeScan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rite.pillcounting.core.room.dao.DrugMasterDao
import com.rite.pillcounting.core.room.dao.PillCountTxnDao
import com.rite.pillcounting.core.room.models.DrugMasterEntity
import com.rite.pillcounting.core.room.models.PillCountTxnEntity
import com.rite.pillcounting.core.room.models.enums.CountStatus
import com.rite.pillcounting.core.room.models.enums.CountType
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import com.rite.pillcounting.feature.barcodeScan.domain.data.IDrugRepository
import com.rite.pillcounting.feature.barcodeScan.domain.data.NavigationEvent
import com.rite.pillcounting.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.rite.pillcounting.feature.barcodeScan.domain.model.ScanBarcodeUiState
import com.rite.pillcounting.feature.barcodeScan.presentation.analyzer.BarcodeAnalyzer
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
 * ViewModel responsible for handling all business logic of the Barcode Scanning screen.
 *
 * Responsibilities:
 * - Manage camera scanner state (start, stop, redo).
 * - Process scanned barcodes:
 *   - Lookup drug in local DB first.
 *   - If not found, fetch from API.
 * - Create pill count transactions and persist them in local Room DB.
 * - Expose navigation events to drive UI transitions.
 *
 * @property savedStateHandle Used to retrieve navigation arguments (e.g., scan type).
 * @property drugRepository Repository for fetching drug details from a remote source.
 * @property drugMasterDao DAO for managing drug master data.
 * @property preferenceHelper Wrapper for persisting local IDs and preferences.
 * @property pillCountTxnDao DAO for handling pill count transaction records.
 */
@HiltViewModel
class ScanBarcodeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val drugRepository: IDrugRepository,
    private val drugMasterDao: DrugMasterDao,
    private val preferenceHelper: PreferenceHelper,
    private val pillCountTxnDao: PillCountTxnDao,
    val analyzer: BarcodeAnalyzer
) : ViewModel() {

    /** Logger instance scoped to this ViewModel for debugging and error tracking. */
    private val logger = AppLogger.create<ScanBarcodeViewModel>()

    /** Backing state for the UI layer (state hoisted for Compose). */
    private val _uiState = MutableStateFlow(ScanBarcodeUiState())

    /** Public immutable view of the UI state. */
    val uiState: StateFlow<ScanBarcodeUiState> = _uiState.asStateFlow()

    /** Backing channel for one-time navigation events (e.g., navigate to PillCount screen). */
    private val _navigationEvent = Channel<NavigationEvent>()

    /** Public Flow that UI can collect to observe navigation actions. */
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        val scanType = savedStateHandle.get<String>(ARG_TYPE) ?: ""
        _uiState.update { it.copy(scanType = scanType) }
        logger.i("ViewModel initialized with scanType: '$scanType'")
    }

    /**
     * Central event dispatcher for UI-triggered or system-triggered events.
     *
     * @param event The event from [ScanBarcodeEvent] that needs to be handled.
     */
    fun onEvent(event: ScanBarcodeEvent) {
        logger.d("Received event: ${event::class.java.simpleName}")
        when (event) {
            is ScanBarcodeEvent.BarcodeScanned -> processBarcode(barcodeValue = event.barcodeValue, imagePath = event.imagePath)
            is ScanBarcodeEvent.ScannerError -> handleScannerError(event.exception)
            ScanBarcodeEvent.StartCount -> handleStartCount()
            ScanBarcodeEvent.RedoScan -> handleRedoScan()
            ScanBarcodeEvent.manualPillInfo -> showManualEntryDialog()
        }
    }

    /**
     * Process a scanned barcode value.
     *
     * - First checks if the drug exists in local DB ([DrugMasterDao]).
     * - If not found, fetches from remote API via [IDrugRepository].
     * - Updates [uiState] with result (drug name, NDC, error, etc.).
     *
     * @param barcodeValue Raw string value from the scanned barcode.
     */
    private fun processBarcode(barcodeValue: String, imagePath: String) {
        if (uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val drug = drugMasterDao.getDrugByNdc(barcodeValue)
            if (drug != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        drugName = drug.drugName.orEmpty(),
                        ndc = drug.ndc,
                        barcodeImagePath = imagePath,
                        isScannerActive = false
                    )
                }
            } else {
                try {
                    val drugInfo = drugRepository.getDrugInfoByNdc(barcodeValue)
                    if (drugInfo != null) {
                        val displayName = drugInfo.genericName?.takeIf { it.isNotBlank() } ?: "Unknown Drug"
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                drugName = displayName,
                                ndc = drugInfo.ndc,
                                barcodeImagePath = imagePath,
                                isScannerActive = false
                            )
                        }
                    } else {
                        throw Exception("No drug information found for this NDC.")
                    }
                } catch (e: Exception) {
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
     * Reset the UI state for a redo scan.
     *
     * Clears drug info and error messages, and re-activates the scanner.
     */
    private fun handleRedoScan() {
        logger.d("Redo scan triggered → resuming scanner.")
        analyzer.resume()
        _uiState.update {
            it.copy(
                drugName = "",
                ndc = "",
                error = null,
                isScannerActive = true
            )
        }
    }

    /**
     * Handle confirmation of a scanned drug and create a pill count transaction.
     *
     * - Validates that a scanned NDC exists.
     * - Uses [DrugMasterDao.upsertPreservingId] to ensure the drug is persisted.
     * - Creates a [PillCountTxnEntity] and persists it in [PillCountTxnDao].
     * - Emits a navigation event to proceed to pill count screen.
     */
    private fun handleStartCount() {
        val currentNdc = uiState.value.ndc
        val currentDrugName = uiState.value.drugName

        if (currentNdc.isBlank()) {
            logger.w("StartCount ignored: NDC is blank.")
            _uiState.update { it.copy(error = "Please scan an item first.") }
            return
        }

        viewModelScope.launch {
            val drugId = drugMasterDao.upsertPreservingId(
                DrugMasterEntity(
                    ndc = currentNdc,
                    drugName = currentDrugName
                )
            )
            val localId = preferenceHelper.getLocalId()
            logger.i("LocalId retrieved from preferences: $localId")

            val txn = PillCountTxnEntity(
                localId = localId,
                drugId = drugId,
                countType = when (uiState.value.scanType) {
                    "FIXED" -> CountType.FIXED
                    "REGULAR" -> CountType.REGULAR
                    else -> CountType.REGULAR
                },
                status = CountStatus.PARTIAL,
                expiry = uiState.value.expiry,
                lotNo = uiState.value.lotNo,
                barcodeImage = uiState.value.barcodeImagePath
            )

            val txnId = pillCountTxnDao.upsertPreservingId(txn)
            preferenceHelper.saveTxnId(txnId)
            logger.d("Transaction created with txnId=$txnId")

            _navigationEvent.send(
                NavigationEvent.NavigateToPillCount(
                    ndc = currentNdc,
                    type = uiState.value.scanType
                )
            )
        }
    }

    /**
     * Handle scanner errors reported from MLKit or CameraX.
     *
     * @param exception The thrown exception.
     */
    private fun handleScannerError(exception: Exception) {
        logger.e("Scanner error received", exception)
        _uiState.update { it.copy(error = "Scanner failed. Please try again.") }
    }

    /**
     * Add a drug manually (fallback path).
     *
     * Used when the drug is not recognized or the barcode fails to resolve.
     *
     * @param drugName Human-readable drug name entered manually.
     * @param ndc National Drug Code string.
     */
    fun addManualDrug(drugName: String, ndc: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                drugName = drugName,
                ndc = ndc,
                isScannerActive = false,
                showManualEntry = false,
                error = null
            )
        }
    }

    /** Show the manual entry dialog. */
    fun showManualEntryDialog() {
        _uiState.update { it.copy(showManualEntry = true, error = null) }
    }

    /** Hide the manual entry dialog. */
    fun hideManualEntryDialog() {
        analyzer.resume()
        _uiState.update { it.copy(showManualEntry = false, error = null, isScannerActive = true ) }
    }

    companion object {
        /** Navigation argument key for scan type ("FIXED" or "REGULAR"). */
        const val ARG_TYPE = "type"
    }
}
