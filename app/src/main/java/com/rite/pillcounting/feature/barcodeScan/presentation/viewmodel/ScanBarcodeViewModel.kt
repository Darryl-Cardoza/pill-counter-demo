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
import org.rite.hl7.hl7.domain.model.CompleteHL7Message
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
        loadExpectedNdcFromTxn()
    }

    /**
     * Central event dispatcher for UI-triggered or system-triggered events.
     *
     * @param event The event from [ScanBarcodeEvent] that needs to be handled.
     */
    fun onEvent(event: ScanBarcodeEvent) {
        logger.d("Received event: ${event::class.java.simpleName}")
        when (event) {
            is ScanBarcodeEvent.BarcodeScanned -> processBarcode(
                gtin14 = event.gtin14,
                imagePath = event.imagePath,
                expiry = event.expiry,
                lotNo = event.lotNo
            )

            is ScanBarcodeEvent.ScannerError -> handleScannerError(event.exception)
            is ScanBarcodeEvent.StartCount -> handleStartCount(event.receivedFromHL7)
            ScanBarcodeEvent.RedoScan -> handleRedoScan()
        }
    }

    private fun loadExpectedNdcFromTxn() {
        viewModelScope.launch {
            val txnId = preferenceHelper.getTxnId()
            val txn = pillCountTxnDao.getById(txnId) ?: return@launch

            if (txn.isComingFromHL7 == true) {
                val drugId = txn.drugId ?: return@launch
                val drug = drugMasterDao.getDrugById(drugId) ?: return@launch

                logger.i("Loaded expected NDC from DrugMaster: ${drug.ndc}")

                println("Loaded expected NDC from DrugMaster: ${drug.ndc}")

                _uiState.update {
                    it.copy(
                        hl7ExpectedNdc = drug.ndc,
                        drugName = drug.drugName ?: it.drugName
                    )
                }
            }
        }
    }


    /**
     * Process a scanned barcode value.
     *
     * - First checks if the drug exists in local DB ([DrugMasterDao]).
     * - If not found, fetches from remote API via [IDrugRepository].
     * - Updates [uiState] with result (drug name, NDC, error, etc.).
     *
     * @param gtin14 Raw string value from the scanned barcode.
     */
    private fun processBarcode(gtin14: String, imagePath: String, expiry: String, lotNo: String) {
        if (uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            /* ---------- HL7 NDC VALIDATION ---------- */

            var result: Boolean = false

            val expectedHl7Ndc = uiState.value.hl7ExpectedNdc
            if (expectedHl7Ndc != null && expectedHl7Ndc != gtin14) {
                logger.w("NDC mismatch: scanned=$gtin14 expected=$expectedHl7Ndc")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showNdcNotMatchedDialog = true
                    )
                }
                return@launch
            } else if (expectedHl7Ndc == gtin14) {
                result = true
            }


            val drug = drugMasterDao.getDrugByNdc(gtin14)
            if (drug != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        drugName = drug.drugName.orEmpty(),
                        ndc = drug.ndc,
                        barcodeImagePath = imagePath,
                        isScannerActive = false,
                        expiry = expiry,
                        lotNo = lotNo
                    )
                }
                onEvent(ScanBarcodeEvent.StartCount(result))
            } else {
                try {
                    val drugInfo = drugRepository.getDrugInfoByNdc(gtin14)
                    if (drugInfo != null) {
                        val displayName =
                            drugInfo.genericName?.takeIf { it.isNotBlank() } ?: "Unknown Drug"
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                drugName = displayName,
                                ndc = drugInfo.ndc,
                                barcodeImagePath = imagePath,
                                isScannerActive = false,
                                expiry = expiry,
                                lotNo = lotNo
                            )
                        }
                        onEvent(ScanBarcodeEvent.StartCount(result))
                    } else {
                        throw Exception("No drug information found for this NDC.")
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to find drug information.",
//                            showManualEntry = true
                        )
                    }
                }
            }
        }
    }


    fun resumeScanning() {
        _uiState.update {
            it.copy(
                isLoading = false,
                showNdcNotMatchedDialog = false
            )
        }

        analyzer.resume()
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

    fun hideNdcNotMatchedDialog() {
        _uiState.update { it.copy(showNdcNotMatchedDialog = false) }
    }

    /**
     * Handle confirmation of a scanned drug and create a pill count transaction.
     *
     * - Validates that a scanned NDC exists.
     * - Uses [DrugMasterDao.upsertPreservingId] to ensure the drug is persisted.
     * - Creates a [PillCountTxnEntity] and persists it in [PillCountTxnDao].
     * - Emits a navigation event to proceed to pill count screen.
     */
    private fun handleStartCount(receivedFromHL7: Boolean) {

        val currentNdc = uiState.value.ndc
        if (currentNdc.isBlank()) return

        viewModelScope.launch {

            val txnId = preferenceHelper.getTxnId()

            if (receivedFromHL7) {
                val txn = pillCountTxnDao.getById(txnId) ?: return@launch
                pillCountTxnDao.update(
                    txn.copy(
                        countType = CountType.valueOf(uiState.value.scanType),
                        status = CountStatus.PARTIAL,
                        expiry = uiState.value.expiry,
                        barcodeImage = uiState.value.barcodeImagePath
                    )
                )
                logger.i("HL7 txn updated with scan data txnId=$txnId")
            } else {
                val drugId = drugMasterDao.upsertPreservingId(
                    DrugMasterEntity(
                        ndc = currentNdc,
                        drugName = uiState.value.drugName
                    )
                )

                val txn = PillCountTxnEntity(
                    localId = preferenceHelper.getLocalId(),
                    drugId = drugId,
                    countType = CountType.valueOf(uiState.value.scanType),
                    status = CountStatus.PARTIAL,
                    expiry = uiState.value.expiry,
                    lotNo = uiState.value.lotNo,
                    barcodeImage = uiState.value.barcodeImagePath
                )

                val newTxnId = pillCountTxnDao.upsertPreservingId(txn)
                preferenceHelper.saveTxnId(newTxnId)
            }

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
//                showManualEntry = false,
                error = null
            )
        }
        onEvent(ScanBarcodeEvent.StartCount())
    }

    companion object {
        /** Navigation argument key for scan type ("FIXED" or "REGULAR"). */
        const val ARG_TYPE = "type"
    }
}
