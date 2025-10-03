package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDao
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDetailsDao
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnDetailsEntity
import com.example.pillcountingnewmodels.core.room.models.enums.CountStatus
import com.example.pillcountingnewmodels.core.room.models.enums.CountType
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.core.utils.saveBitmapToFile
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.PillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.DetectedPill
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.PillScanningUiState
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.TxnDetail
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic.PillAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject


/**
 * ViewModel for the Fixed Count Pill Scanning feature.
 *
 * Responsibilities:
 * 1. Loads and initializes the TensorFlow Lite interpreter.
 * 2. Manages the [PillAnalyzer] for frame-by-frame pill detection.
 * 3. Stores UI state: detected pills, batch history, drug info, etc.
 * 4. Handles user events like Add Batch, Rescan, Pause, Done.
 * 5. Manages memory for camera frames and thumbnails safely.
 *
 * Lifecycle:
 * - Initializes interpreter on-demand via [initializeInterpreter].
 * - Processes frames in [onFrameCaptured].
 * - Releases interpreter and bitmaps on [onCleared].
 */
@HiltViewModel
class PillScanningViewModel @Inject constructor(
    app: Application,
    private val preferenceHelper: PreferenceHelper,
    private val pillCountTxnDao: PillCountTxnDao,
    private val pillCountTxnDetailsDao: PillCountTxnDetailsDao
) : AndroidViewModel(app) {

    private val logger = AppLogger.create<PillScanningViewModel>()

    sealed class ModelState {
        object Idle : ModelState()
        object Loading : ModelState()
        data class Ready(val analyzer: PillAnalyzer) : ModelState()
        data class Error(val message: String, val cause: Throwable? = null) : ModelState()
    }

    private val _modelState = MutableStateFlow<ModelState>(ModelState.Idle)
    val modelState = _modelState.asStateFlow()

    private val _uiState = MutableStateFlow(PillScanningUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var interpreter: Interpreter? = null
    private var currentFrameBitmap: Bitmap? = null

    companion object {
        private const val MODEL_FILENAME = "best_float32_new.tflite"
        private const val TAG = "PillScanningVM"
    }

    private var lastDetectedSnapshot: List<Int> = emptyList()
    private var lastChangeTimestamp: Long = System.currentTimeMillis()


    fun checkIdleState(newDetected: List<Int>) {
        if (newDetected == lastDetectedSnapshot) {
            val elapsed = System.currentTimeMillis() - lastChangeTimestamp
            if (elapsed >= 15_000) { // 15 seconds
                _uiState.update { it.copy(showIdleOverlay = true) }
            }
        } else {
            lastDetectedSnapshot = newDetected
            lastChangeTimestamp = System.currentTimeMillis()
            _uiState.update { it.copy(showIdleOverlay = false) }
        }
    }

    fun resetIdleOverlay() {
        _uiState.update { it.copy(showIdleOverlay = false) }
        lastChangeTimestamp = System.currentTimeMillis()
    }

    fun observeTxnDetailsForTxn() {
        viewModelScope.launch {
            pillCountTxnDetailsDao.observeAllForTxn(preferenceHelper.getTxnId())
                .collectLatest { entities ->
                    _uiState.update { currentState ->
                        val history = entities.map { e ->
                            TxnDetail(
                                txnDetailId = e.txnDetailsId,
                                count = e.pillCount ?: 0,
                                image = e.imagePath,
                                createdAt = e.createdAt
                            )
                        }

                        currentState.copy(
                            txnDetailHistory = history
                        )
                    }
                }
        }
    }


    /**
     * Initializes the TensorFlow Lite interpreter with retry support.
     *
     * Once initialized, it creates a [PillAnalyzer] with a callback to update
     * [DetectedPill]s in UI state and store the latest frame bitmap.
     *
     * @param retryCount Number of retries in case of failure.
     * @param viewWidth Width of the PreviewView for scaling coordinates.
     * @param viewHeight Height of the PreviewView for scaling coordinates.
     */
    fun initializeInterpreter(
        retryCount: Int = 1,
        viewWidth: Int = 640,
        viewHeight: Int = 640
    ) {
        if (_modelState.value is ModelState.Ready) {
            logger.w("Interpreter already initialized — skipping")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _modelState.value = ModelState.Loading
            var attempt = 0
            var success = false
            val start = System.currentTimeMillis()

            while (attempt <= retryCount && !success) {
                try {
                    val buffer = loadModelFile(MODEL_FILENAME)
                    val options = Interpreter.Options().apply {
                        setUseXNNPACK(true)
                        numThreads = Runtime.getRuntime().availableProcessors().coerceAtMost(4)
                    }

                    interpreter = Interpreter(buffer, options).also { tflite ->
                        val analyzer = PillAnalyzer(
                            interpreter = tflite,
                            viewWidth = viewWidth,
                            viewHeight = viewHeight
                            // Update callback to accept the bitmap
                        ) { count, detections, bitmap ->
                            logger.i("✅ Pills detected: $count")

                            // Store the latest bitmap and recycle the previous one
                            currentFrameBitmap?.recycle()
                            currentFrameBitmap = bitmap

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

                        val duration = System.currentTimeMillis() - start
                        logger.i("✅ Interpreter ready in ${duration}ms")
                        _modelState.value = ModelState.Ready(analyzer)
                        success = true
                    }
                } catch (e: Exception) {
                    attempt++
                    logger.e("❌ Failed to initialize interpreter (attempt $attempt)", e)

                    if (attempt > retryCount) {
                        _modelState.value = ModelState.Error(
                            message = "Interpreter initialization failed",
                            cause = e
                        )
                    } else {
                        logger.w("Retrying interpreter initialization…")
                    }
                }
            }
        }
    }

    /**
     * Updates the list of detected pills in the UI state.
     */
    private fun updateDetectedPills(pills: List<DetectedPill>) {
        _uiState.update { it.copy(detectedPills = pills) }
    }

    /**
     * Handles a captured camera frame ([ImageProxy]).
     *
     * Frames are processed only if the model is ready. Otherwise, the frame is discarded.
     */
    fun onFrameCaptured(image: ImageProxy) {
        val currentState = _modelState.value
        if (currentState is ModelState.Ready) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    currentState.analyzer.analyze(image)
                } catch (e: Exception) {
                    logger.e("Frame processing failed", e)
                    image.close()
                }
            }
        } else {
            image.close()
        }
    }

    /**
     * Loads a TFLite model from the assets folder into a [MappedByteBuffer].
     */
    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val afd = getApplication<Application>().assets.openFd(fileName)
        FileInputStream(afd.fileDescriptor).use { input ->
            return input.channel.map(
                FileChannel.MapMode.READ_ONLY,
                afd.startOffset,
                afd.declaredLength
            )
        }
    }

    /**
     * Cleans up interpreter and bitmap resources when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        interpreter?.close()
        interpreter = null
        currentFrameBitmap?.recycle()
        currentFrameBitmap = null
        _modelState.value = ModelState.Idle
        logger.i("Interpreter closed and resources released")
    }

    /**
     * Handles user events from the scanning UI.
     *
     * - [AddBatchClicked]: Adds a new batch with current detected pills and the last thumbnail.
     * - [RescanClicked]: Clears current detected pills for a fresh scan.
     * - [PauseClicked]: Placeholder to stop camera flow temporarily.
     * - [DoneClicked]: Placeholder to finalize and save results.
     */
    fun onEvent(event: PillScanningEvent) {
        when (event) {
            is PillScanningEvent.AddTransactionDetailClicked -> {
                val totalBatchCount = _uiState.value.txnDetailHistory.sumOf { it.count }
                val targetCount = _uiState.value.targetCount
                val currentCount = _uiState.value.detectedPills.size

                // Predict the total after adding the current batch
                val predictedTotal = totalBatchCount + currentCount

                if (_uiState.value.scanType == CountType.FIXED.toString() && predictedTotal > targetCount) {
                    logger.w("Skipping add transaction detail because predicted total exceeds target.")
                    _uiState.update {
                        it.copy(
                            restrictAdd = true
                        )
                    }
                    return
                }

                logger.i("Add batch clicked with count: $currentCount")

                if (currentCount == 0) {
                    logger.w("Skipping add batch because current count is zero.")
                    return
                }
                val filePath = currentFrameBitmap?.let {
                    saveBitmapToFile(
                        getApplication(),
                        it,
                        "txn_detail_${System.currentTimeMillis()}.jpg",
                        "transaction_details"
                    )
                }

                viewModelScope.launch {
                    val detail = PillCountTxnDetailsEntity(
                        txnId = preferenceHelper.getTxnId(), // FK to transaction
                        pillCount = currentCount,
                        imagePath = filePath,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    pillCountTxnDetailsDao.insert(detail)
                }
                currentFrameBitmap = null

            }

            is PillScanningEvent.RescanClicked -> {
                logger.i("Rescan requested")
                _uiState.update { it.copy(detectedPills = emptyList()) }
            }

            is PillScanningEvent.PauseClicked -> {
                logger.i("Pause clicked")
                // TODO: stop camera flow temporarily
            }

            PillScanningEvent.DoneClicked -> {
                viewModelScope.launch {
                    val txnId = preferenceHelper.getTxnId()
                    val totalPillCount = pillCountTxnDetailsDao.getTotalPillCountForTxn(txnId)

                    if (totalPillCount == 0) {
                        _uiState.update { it.copy(showNoTransaction = true) }
                        return@launch
                    }

                    // proceeding because Pill count > 0
                    if (preferenceHelper.getShowNotesDialogSetting()) {
                        _uiState.update { it.copy(showNotesDialog = true) }
                    } else {
                        showConfirmDialogAfterDone()
                    }
                }
            }

            is PillScanningEvent.NoteSaved -> {
                setNoteDialogShown(false)
                viewModelScope.launch {
                    pillCountTxnDao.updateNote(
                        txnId = preferenceHelper.getTxnId(),
                        note = event.note
                    )
                    showConfirmDialogAfterDone()
                }
            }

            PillScanningEvent.NoteSkip -> {
                setNoteDialogShown(false)
                showConfirmDialogAfterDone()
            }

            is PillScanningEvent.ConfirmDone -> {
                viewModelScope.launch {
                    val txnId = preferenceHelper.getTxnId()
                    val totalPillCount = pillCountTxnDetailsDao.getTotalPillCountForTxn(txnId)
                    val txn =
                        pillCountTxnDao.getById(txnId) // get txn to know countType & targetCount
                    if (txn == null) {
                        return@launch
                    }
                    //safety check
                    if (totalPillCount == 0) {
                        _uiState.update {
                            it.copy(
                                showConfirmDialog = false
                            )
                        }
                        return@launch
                    }

                    // Decide status
                    val finalStatus = if (txn.countType == CountType.FIXED) {
                        if (txn.targetCount != null && totalPillCount < txn.targetCount) {
                            CountStatus.PARTIAL
                        } else {
                            CountStatus.COMPLETED
                        }
                    } else {
                        CountStatus.COMPLETED
                    }
                    pillCountTxnDao.updateTxnStatus(txnId, finalStatus)

                    //navigation
                    _navigationEvent.send(
                        NavigationEvent.NavigateToDashboard
                    )
                }
            }

            is PillScanningEvent.CancelDone -> {
                _uiState.update { it.copy(showConfirmDialog = false) }
            }

            is PillScanningEvent.TransactionDetailDeleted -> {
                viewModelScope.launch {
                    logger.i("Transaction detail deleted: ${event.txnDetailId}")
                    pillCountTxnDetailsDao.softDelete(event.txnDetailId)
                }
            }
        }
    }

    private fun showConfirmDialogAfterDone() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun setScanType(type: String) {
        _uiState.update { it.copy(scanType = type) }
    }

    fun updateTargetCount(target: Int) {
        _uiState.update { it.copy(targetCount = target) }
        viewModelScope.launch {
            pillCountTxnDao.updateTargetCount(preferenceHelper.getTxnId(), target)
        }
    }

    fun showTxnInfo(countType: String) {
        viewModelScope.launch {
            val txnInfo = pillCountTxnDao.getTxnWithDetails(preferenceHelper.getTxnId())
            _uiState.update { currentState ->

                val shouldShowDialog =
                    countType == CountType.FIXED.toString() &&
                            (txnInfo?.targetCount == null || txnInfo.targetCount == 0) &&
                            !currentState.showTargetCountDialog

                currentState.copy(
                    drugName = txnInfo?.drugName ?: "",
                    targetCount = txnInfo?.targetCount ?: 0,
                    showTargetCountDialog = shouldShowDialog
                )
            }
        }
    }

    fun resetRestrictAdd() {
        _uiState.update { it.copy(restrictAdd = false) }
    }

    fun resetNoTransaction() {
        _uiState.update { it.copy(showNoTransaction = false) }
    }

    fun setTargetCountDialogShown(shown: Boolean) {
        _uiState.update { it.copy(showTargetCountDialog = shown) }
    }

    fun setNoteDialogShown(shown: Boolean) {
        _uiState.update { it.copy(showNotesDialog = shown) }
    }
}