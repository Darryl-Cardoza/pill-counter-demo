package com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rite.pillcounting.R
import com.rite.pillcounting.core.room.dao.PillCountTxnDao
import com.rite.pillcounting.core.room.dao.PillCountTxnDetailsDao
import com.rite.pillcounting.core.room.dao.UserDao
import com.rite.pillcounting.core.room.models.PillCountTxnDetailsEntity
import com.rite.pillcounting.core.room.models.enums.CountStatus
import com.rite.pillcounting.core.room.models.enums.CountType
import com.rite.pillcounting.core.security.ModelDecryptor
import com.rite.pillcounting.core.utils.common.HelperFunctions.saveBitmapToFile
import com.rite.pillcounting.core.utils.common.LocationProvider
import com.rite.pillcounting.core.utils.common.OverlayUtils
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.showToast
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import com.rite.pillcounting.feature.pillCountScan.domain.data.NavigationEvent
import com.rite.pillcounting.feature.pillCountScan.domain.data.PillScanningEvent
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import com.rite.pillcounting.feature.pillCountScan.domain.model.PillScanningUiState
import com.rite.pillcounting.feature.pillCountScan.domain.model.TxnDetail
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.CameraHelper
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.PillAnalyzer
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.Postprocessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.nio.ByteBuffer
import java.util.ArrayDeque
import javax.inject.Inject

/**
 * ViewModel responsible for:
 *  - Managing camera frame analysis and TensorFlow inference.
 *  - Updating UI state for pill counting workflow.
 *  - Managing database transactions.
 *  - Preventing duplicate "Add" operations without a new scan.
 */
@HiltViewModel
class PillScanningViewModel @Inject constructor(
    app: Application,
    private val preferenceHelper: PreferenceHelper,
    private val pillCountTxnDao: PillCountTxnDao,
    private val userDao: UserDao,
    private val pillCountTxnDetailsDao: PillCountTxnDetailsDao,
    private val locationProvider: LocationProvider,
) : AndroidViewModel(app) {

    private val logger = AppLogger("PillScanningVM")

    /** TensorFlow interpreter instance */
    private var interpreter: Interpreter? = null

    private var currentFrameBitmap: Bitmap? = null
    private var lastTransformationMatrix: Matrix? = null
    private var isAnalyzingFrame = false
    private var isPaused = false
    private var idleJob: Job? = null
    private val idleTimeout = 30_000L

    private val _uiState = MutableStateFlow(PillScanningUiState())
    val uiState: StateFlow<PillScanningUiState> = _uiState.asStateFlow()

    private val _modelState = MutableStateFlow<ModelState>(ModelState.Idle)
    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _lastTenDetections = MutableStateFlow(ArrayDeque<Int>())
    private val _cameraPaused = MutableStateFlow(false)
    val cameraPaused = _cameraPaused.asStateFlow()

    private var cameraHelper: CameraHelper? = null

    // --- Duplicate prevention ---
    private var currentScanId: Long = 0L

    // --- Detection snapshot ---
    private var lastDetectedSnapshot: List<Int> = emptyList()
    private var lastChangeTimestamp: Long = System.currentTimeMillis()

    /** Public read-only flow for observing recent detection counts. */
    val lastTenDetections: StateFlow<ArrayDeque<Int>> = _lastTenDetections

    // --- Duplicate prevention ---
    private var lastAddedScanSignature: String? = null
    private var lastAddClickTime: Long = 0L
    private var gpuDelegate: GpuDelegate? = null


    companion object {
        private const val MODEL_FILENAME = "best_float32.tflite"
        private const val ZERO_DETECTIONS_THRESHOLD = 25
    }

    /** Model initialization states */
    sealed class ModelState {
        object Idle : ModelState()
        object Loading : ModelState()
        data class Ready(val analyzer: PillAnalyzer) : ModelState()
        data class Error(val message: String, val cause: Throwable? = null) : ModelState()
    }

    init {
        resetIdleTimer()
    }


    // ------------------------------------------------------------------------
    // Initialization and Observation
    // ------------------------------------------------------------------------

    /** Attach a CameraHelper instance for lifecycle control. */
    fun attachCameraHelper(helper: CameraHelper) {
        cameraHelper = helper
    }

    /** Observe all transaction details for the current transaction. */
    fun observeTxnDetailsForTxn() {
        viewModelScope.launch {
            pillCountTxnDetailsDao.observeAllForTxn(preferenceHelper.getTxnId())
                .collectLatest { entities ->
                    val history = entities.map {
                        TxnDetail(
                            txnDetailId = it.txnDetailsId,
                            count = it.pillCount ?: 0,
                            image = it.imagePath,
                            createdAt = it.createdAt
                        )
                    }
                    _uiState.update { state -> state.copy(txnDetailHistory = history) }
                }
        }
    }

    /** Initialize TensorFlow Lite interpreter for pill detection. */
    fun initializeInterpreter(
        retryCount: Int = 1,
        viewWidth: Int = 640,
        viewHeight: Int = 640
    ) {
        if (_modelState.value is ModelState.Ready) {
            logger.w("Interpreter already initialized, skipping reinitialization.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _modelState.value = ModelState.Loading
            var attempt = 0

            while (attempt <= retryCount) {
                var createdDelegate: GpuDelegate? = null
                try {
                    val buffer = loadModelFile()
                    val options = Interpreter.Options()

                    // Move GPU delegate creation to MAIN thread
                    withContext(Dispatchers.Main) {
                        val compatList = CompatibilityList()
                        if (compatList.isDelegateSupportedOnThisDevice) {
                            try {
                                val delegateOptions = compatList.bestOptionsForThisDevice
                                createdDelegate = GpuDelegate(delegateOptions)
                                options.addDelegate(createdDelegate)
                                logger.i("GPU delegate initialized on main thread.")
                            } catch (gpuInitEx: Exception) {
                                logger.w("GPU delegate creation failed: ${gpuInitEx.message}. Will use CPU fallback.")
                                createdDelegate?.close()
                                createdDelegate = null
                            }
                        } else {
                            logger.w("GPU delegate not supported by CompatibilityList. Using CPU.")
                        }
                    }

                    // CPU fallback if GPU not available
                    if (createdDelegate == null) {
                        options.setUseXNNPACK(true)
                        options.numThreads =
                            Runtime.getRuntime().availableProcessors().coerceAtMost(4)
                    }

                    // Now safely create interpreter (IO thread)
                    val tflite = Interpreter(buffer, options)
                    interpreter = tflite

                    // Keep delegate reference for cleanup
                    gpuDelegate?.close()
                    gpuDelegate = createdDelegate

                    val analyzer = PillAnalyzer(
                        interpreter = tflite,
                        viewWidth = viewWidth,
                        viewHeight = viewHeight
                    ) { count, detections, bitmap, matrix ->
                        processDetections(count, detections, bitmap, matrix, viewWidth, viewHeight)
                    }

                    _modelState.value = ModelState.Ready(analyzer)
                    logger.i("Interpreter initialized successfully.")
                    return@launch

                } catch (e: Exception) {
                    try { createdDelegate?.close() } catch (_: Exception) {}
                    attempt++
                    logger.e("Interpreter initialization failed (attempt $attempt)", e)
                    if (attempt > retryCount) {
                        _modelState.value = ModelState.Error("Initialization failed", e)
                    }
                }
            }
        }
    }



    // ------------------------------------------------------------------------
    // Frame Processing and Detection Logic
    // ------------------------------------------------------------------------

    /** Handle each analyzed frame and maintain rolling detection state. */
    private fun processDetections(
        count: Int,
        detections: List<Postprocessor.Detection>,
        bitmap: Bitmap,
        matrix: Matrix,
        viewWidth: Int,
        viewHeight: Int
    ) {
        if (isPaused) {
            bitmap.recycle()
            return
        }

        currentScanId++
        currentFrameBitmap?.recycle()
        currentFrameBitmap = bitmap
        lastTransformationMatrix = Matrix(matrix)
        logger.d("Frame analyzed. Count=$count, ScanId=$currentScanId")

        val buffer = ArrayDeque(_lastTenDetections.value)
        if (buffer.size >= ZERO_DETECTIONS_THRESHOLD) buffer.removeFirst()
        buffer.addLast(count)
        _lastTenDetections.value = buffer

        updateDetectedPills(
            detections.map {
                DetectedPill(
                    x = it.pixelX / viewWidth,
                    y = it.pixelY / viewHeight,
                    confidence = it.confidence
                )
            }
        )
    }

    private fun pauseAndClearBuffers() {
        _lastTenDetections.value.clear()
        lastDetectedSnapshot = emptyList()
        _uiState.update { it.copy(showIdleOverlay = true, detectedPills = emptyList()) }
        isPaused = true
        _cameraPaused.value = true
        logger.w("Camera paused due to stable detection pattern. Buffers cleared.")
    }

    fun resetIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(idleTimeout)
            pauseAndClearBuffers()
        }
    }


    /** Process an incoming frame from CameraX. */
    fun onFrameCaptured(image: ImageProxy) {
        if (isPaused) {
            image.close()
            return
        }
        val currentState = _modelState.value
        if (currentState !is ModelState.Ready) {
            image.close()
            return
        }
        if (isAnalyzingFrame) {
            image.close()
            return
        }

        isAnalyzingFrame = true
        viewModelScope.launch(Dispatchers.Default) {
            try {
                currentState.analyzer.analyze(image)
            } catch (e: Exception) {
                logger.e("Frame analysis failed.", e)
                image.close()
            } finally {
                isAnalyzingFrame = false
            }
        }
    }

    /** Reset idle overlay and resume camera analysis. */
    fun resetIdleOverlay() {
        _uiState.update { it.copy(showIdleOverlay = false) }

        // Clear all idle detection memory
        _lastTenDetections.value.clear()
        lastDetectedSnapshot = emptyList()
        lastChangeTimestamp = System.currentTimeMillis()
        lastAddedScanSignature = null

        // Resume camera
        isPaused = false
        _cameraPaused.value = false

        logger.i("Idle overlay reset → buffers, snapshots, and timers cleared. Analysis resumed cleanly.")
    }


    /** Update the list of detected pills in UI state. */
    private fun updateDetectedPills(pills: List<DetectedPill>) {
        _uiState.update { it.copy(detectedPills = pills) }
    }

    fun updateFilteredPills(filtered: List<DetectedPill>) {
        _uiState.update { it.copy(filteredPills = filtered) }
    }

    /** Load TensorFlow Lite model from assets. */
    private fun loadModelFile(fileName: String = MODEL_FILENAME): ByteBuffer {
        val context = getApplication<Application>()
        val encFile = File(context.filesDir, "$fileName.enc")

        if (!encFile.exists()) {
            context.assets.open("$fileName.enc").use { input ->
                encFile.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val decryptedBytes = ModelDecryptor.decryptToBytes(encFile)
        return ByteBuffer.allocateDirect(decryptedBytes.size).apply {
            put(decryptedBytes)
            rewind()
        }
    }


    override fun onCleared() {
        super.onCleared()

        try {
            gpuDelegate?.close()
        } catch (e: Exception) {
            logger.w("Error closing GPU delegate: ${e.message}")
        } finally {
            gpuDelegate = null
        }

        try {
            currentFrameBitmap?.recycle()
        } catch (e: Exception) {
            logger.w("Error recycling bitmap: ${e.message}")
        } finally {
            currentFrameBitmap = null
        }

        _modelState.value = ModelState.Idle
        logger.i("ViewModel cleared and all TensorFlow resources released.")
    }
    // ------------------------------------------------------------------------
    // Event Handling
    // ------------------------------------------------------------------------

    fun onEvent(event: PillScanningEvent) {
        when (event) {
            is PillScanningEvent.AddTransactionDetailClicked -> handleAddTransaction(event)
            is PillScanningEvent.RescanClicked -> handleRescan()
            is PillScanningEvent.PauseClicked -> logger.i("Pause clicked.")
            PillScanningEvent.DoneClicked -> handleDone()
            is PillScanningEvent.NoteSaved -> handleNoteSaved(event)
            PillScanningEvent.NoteSkip -> handleNoteSkip()
            is PillScanningEvent.ConfirmDone -> handleConfirmDone()
            is PillScanningEvent.CancelDone -> handleCancelDone()
            is PillScanningEvent.TransactionDetailDeleted -> handleDeleteTransaction(event)
        }
    }

    private fun handleAddTransaction(event: PillScanningEvent.AddTransactionDetailClicked) {
        val context: Context = getApplication<Application>().applicationContext
        val currentTime = System.currentTimeMillis()

        // --- Debounce: prevent taps within 2 seconds ---
        if (currentTime - lastAddClickTime < 2000) {
            showToast(context, context.getString(R.string.add_button_wait))
            logger.w("Add action ignored: tapped too quickly.")
            return
        }
        lastAddClickTime = currentTime

        val totalBatchCount = _uiState.value.txnDetailHistory.sumOf { it.count }
        val targetCount = _uiState.value.targetCount
        val currentCount = event.filteredCount
        val predictedTotal = totalBatchCount + currentCount

        if (_uiState.value.scanType == CountType.FIXED.toString() && predictedTotal > targetCount) {
            _uiState.update { it.copy(restrictAdd = true) }
            //showToast(context, context.getString(R.string.add_exceeds_target))
            logger.w("Add blocked: predicted total exceeds target count.")
            return
        }
        if (currentCount == 0) {
            showToast(context, context.getString(R.string.add_zero_detected))
            logger.w("Add blocked: detected count is 0.")
            return
        }

        // --- Generate unique signature for current detections ---
        val signature = _uiState.value.detectedPills.joinToString(separator = "|") {
            "${"%.3f".format(it.x)}-${"%.3f".format(it.y)}"
        } + "|count=$currentCount"

        // --- Prevent duplicate adds without new scan ---
        if (signature == lastAddedScanSignature) {
            showToast(context, context.getString(R.string.duplicate_scan_ignored))
            logger.w("Duplicate add prevented: no change in detection pattern.")
            return
        }

        lastAddedScanSignature = signature
        logger.i("Adding transaction detail with unique signature. Count=$currentCount")

        viewModelScope.launch {
            val userId = preferenceHelper.getUserId().orEmpty()
            val user = userDao.getByUserId(userId)
            val location = locationProvider.getCurrentLocationAsString()

            val overlayBitmap = currentFrameBitmap?.let { base ->
                val filteredPills = _uiState.value.filteredPills
                if (filteredPills.isNotEmpty()) {
                    try {
                        OverlayUtils.drawDetectionsOnBitmap(
                            bitmap = base,
                            detectedPills = filteredPills,
                            previewWidth = cameraHelper?.getPreviewWidth() ?: base.width,
                            previewHeight = cameraHelper?.getPreviewHeight() ?: base.height,
                            userName = user?.name,
                            userId = user?.userId,
                            location = location,
                            timestamp = System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        logger.e("Overlay drawing failed, returning base bitmap", e)
                        base
                    }
                } else base
            }

            val filePath = overlayBitmap?.let {
                saveBitmapToFile(
                    getApplication(),
                    it,
                    "txn_detail_${System.currentTimeMillis()}.jpg",
                    "transaction_details"
                )
            }

            pillCountTxnDetailsDao.insert(
                PillCountTxnDetailsEntity(
                    txnId = preferenceHelper.getTxnId(),
                    pillCount = currentCount,
                    imagePath = filePath,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )

            overlayBitmap?.recycle()
            currentFrameBitmap = null
            logger.i("Transaction detail saved. Count=$currentCount, File=$filePath")
        }
    }

    private fun handleRescan() {
        _uiState.update { it.copy(detectedPills = emptyList()) }
        lastAddedScanSignature = null
        logger.i("Rescan triggered.")
    }

    private fun handleDone() {
        viewModelScope.launch {
            val txnId = preferenceHelper.getTxnId()
            val total = pillCountTxnDetailsDao.getTotalPillCountForTxn(txnId)
            if (total == 0) {
                _uiState.update { it.copy(showNoTransaction = true) }
                return@launch
            }
            if (preferenceHelper.getShowNotesDialogSetting()) {
                _uiState.update { it.copy(showNotesDialog = true) }
            } else showConfirmDialogAfterDone()
        }
    }

    private fun handleNoteSaved(event: PillScanningEvent.NoteSaved) {
        setNoteDialogShown(false)
        viewModelScope.launch {
            pillCountTxnDao.updateNote(preferenceHelper.getTxnId(), event.note)
            showConfirmDialogAfterDone()
        }
    }

    private fun handleNoteSkip() {
        setNoteDialogShown(false)
        showConfirmDialogAfterDone()
    }

    private fun handleConfirmDone() {
        viewModelScope.launch {
            val txnId = preferenceHelper.getTxnId()
            val total = pillCountTxnDetailsDao.getTotalPillCountForTxn(txnId)
            val txn = pillCountTxnDao.getById(txnId) ?: return@launch
            if (total == 0) {
                _uiState.update { it.copy(showConfirmDialog = false) }
                return@launch
            }
            val status =
                if (txn.countType == CountType.FIXED && txn.targetCount != null && total < txn.targetCount)
                    CountStatus.PARTIAL else CountStatus.COMPLETED

            pillCountTxnDao.updateTxnStatus(txnId, status)
            _navigationEvent.send(NavigationEvent.NavigateToDashboard)
            logger.i("Transaction completed. Status=$status")
        }
    }

    private fun handleCancelDone() {
        _uiState.update { it.copy(showConfirmDialog = false) }
        logger.i("Confirm dialog cancelled.")
    }

    private fun handleDeleteTransaction(event: PillScanningEvent.TransactionDetailDeleted) {
        viewModelScope.launch {
            pillCountTxnDetailsDao.softDelete(event.txnDetailId)
            logger.i("Transaction detail deleted. Id=${event.txnDetailId}")
        }
    }

    // ------------------------------------------------------------------------
    // UI Utility Functions
    // ------------------------------------------------------------------------

    fun resetRestrictAdd() = _uiState.update { it.copy(restrictAdd = false) }

    fun resetNoTransaction() = _uiState.update { it.copy(showNoTransaction = false) }

    fun setTargetCountDialogShown(shown: Boolean) =
        _uiState.update { it.copy(showTargetCountDialog = shown) }

    fun setNoteDialogShown(shown: Boolean) =
        _uiState.update { it.copy(showNotesDialog = shown) }

    fun setScanType(type: String) {
        _uiState.update { it.copy(scanType = type) }
        logger.d("Scan type set to $type")
    }

    fun updateTargetCount(target: Int) {
        _uiState.update { it.copy(targetCount = target) }
        viewModelScope.launch {
            pillCountTxnDao.updateTargetCount(preferenceHelper.getTxnId(), target)
            logger.i("Target count updated to $target")
        }
    }

    fun showTxnInfo(countType: String) {
        viewModelScope.launch {
            val txnInfo = pillCountTxnDao.getTxnWithDetails(preferenceHelper.getTxnId())
            val shouldShowDialog = countType == CountType.FIXED.toString() &&
                    (txnInfo?.targetCount == null || txnInfo.targetCount == 0) &&
                    !_uiState.value.showTargetCountDialog

            _uiState.update {
                it.copy(
                    drugName = txnInfo?.drugName.orEmpty(),
                    targetCount = txnInfo?.targetCount ?: 0,
                    showTargetCountDialog = shouldShowDialog
                )
            }
            logger.d("Txn info loaded. Drug=${txnInfo?.drugName}, Target=${txnInfo?.targetCount}")
        }
    }

    private fun showConfirmDialogAfterDone() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }
}
