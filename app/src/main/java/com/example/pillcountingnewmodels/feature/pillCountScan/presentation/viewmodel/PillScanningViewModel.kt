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
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.PillScanningEvent
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
import java.util.ArrayDeque
import javax.inject.Inject

@HiltViewModel
class PillScanningViewModel @Inject constructor(
    app: Application,
    private val preferenceHelper: PreferenceHelper,
    private val pillCountTxnDao: PillCountTxnDao,
    private val pillCountTxnDetailsDao: PillCountTxnDetailsDao
) : AndroidViewModel(app) {

    private val logger = AppLogger("PillScanningVM")

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

    // Detection tracking
    private val lastTenDetections: ArrayDeque<Int> = ArrayDeque()
    private var lastDetectedSnapshot: List<Int> = emptyList()
    private var lastChangeTimestamp: Long = System.currentTimeMillis()

    // Pause state
    private var isPaused: Boolean = false

    // Keep track of last saved detection snapshot
    private var lastSavedDetectionSignature: Int? = null

    companion object {
        private const val MODEL_FILENAME = "best_float32_new.tflite"
        private const val ZERO_DETECTIONS_THRESHOLD = 10
        private const val IDLE_TIMEOUT_MS = 15_000L
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
     * Initializes TensorFlow Lite interpreter and sets up the analyzer.
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
                        ) { count, detections, bitmap ->
                            processDetections(count, detections, bitmap, viewWidth, viewHeight)
                        }

                        val duration = System.currentTimeMillis() - start
                        logger.i("Interpreter ready in ${duration}ms")
                        _modelState.value = ModelState.Ready(analyzer)
                        success = true
                    }
                } catch (e: Exception) {
                    attempt++
                    logger.e("Interpreter init failed (attempt $attempt)", e)
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
     * Process each detection frame: update pills, maintain rolling buffer,
     * check for idle overlay conditions.
     */
    private fun processDetections(
        count: Int,
        detections: List<PillAnalyzer.Detection>,
        bitmap: Bitmap,
        viewWidth: Int,
        viewHeight: Int
    ) {

        if (isPaused) {
            logger.d("Skipping detection → paused state active")
            bitmap.recycle()
            return
        }

        logger.d("Frame analyzed → Detected count = $count")

        // Replace current bitmap
        currentFrameBitmap?.recycle()
        currentFrameBitmap = bitmap

        // Update rolling buffer
        if (lastTenDetections.size >= ZERO_DETECTIONS_THRESHOLD) {
            lastTenDetections.removeFirst()
        }
        lastTenDetections.addLast(count)
        logger.d("Rolling buffer = $lastTenDetections")

        // Check for last-10 zeros
        val allZero = lastTenDetections.size == ZERO_DETECTIONS_THRESHOLD &&
                lastTenDetections.all { it == 0 }

        // Idle timer check (no changes for 15s)
        val sameAsLast = detections.map { it.hashCode() } == lastDetectedSnapshot
        val elapsed = System.currentTimeMillis() - lastChangeTimestamp
        if (sameAsLast && elapsed >= IDLE_TIMEOUT_MS) {
            logger.w("Idle overlay triggered after ${elapsed}ms unchanged")
            _uiState.update { it.copy(showIdleOverlay = true) }
        } else if (!sameAsLast) {
            lastDetectedSnapshot = detections.map { it.hashCode() }
            lastChangeTimestamp = System.currentTimeMillis()
            _uiState.update { it.copy(showIdleOverlay = false) }
        }

        // Show overlay if 10 consecutive zeros
        if (allZero) {
            logger.w("Overlay triggered: last 10 frames had zero detections")
            _uiState.update { it.copy(showIdleOverlay = true) }
            isPaused = true
        }

        // Update pills to UI
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

    /**
     * Reset overlay state, buffer and resume analyzer
     */
    fun resetIdleOverlay() {
        logger.i("Overlay reset → buffer cleared, idle timer restarted, analyzer resumed")
        _uiState.update { it.copy(showIdleOverlay = false) }
        lastTenDetections.clear()
        lastChangeTimestamp = System.currentTimeMillis()
        isPaused = false
    }

    fun observeTxnDetailsForTxn(countType: String) {
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
                        val shouldShowDialog =
                            countType == CountType.FIXED.toString() &&
                                    history.isEmpty() &&
                                    !currentState.showTargetCountDialog
                        currentState.copy(
                            txnDetailHistory = history,
                            showTargetCountDialog = shouldShowDialog
                        )
                    }
                }
        }
    }

    fun onFrameCaptured(image: ImageProxy) {
        if (isPaused) {
            logger.w("Frame ignored → analyzer paused due to idle overlay")
            image.close()
            return
        }

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
            logger.w("Frame ignored → model not ready")
            image.close()
        }
    }

    private fun updateDetectedPills(pills: List<DetectedPill>) {
        _uiState.update { it.copy(detectedPills = pills) }
    }

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

    override fun onCleared() {
        super.onCleared()
        interpreter?.close()
        interpreter = null
        currentFrameBitmap?.recycle()
        currentFrameBitmap = null
        _modelState.value = ModelState.Idle
        logger.i("Resources released & interpreter closed")
    }

    // === User event handlers ===
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

                if (currentCount == 0) {
                    logger.w("Skipping add detail → current detected count is 0")
                    return
                }

                logger.i("Adding transaction detail with count=$currentCount")

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
                        txnId = preferenceHelper.getTxnId(),
                        pillCount = currentCount,
                        imagePath = filePath,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    pillCountTxnDetailsDao.insert(detail)
                    logger.i("Transaction detail saved → count=$currentCount, file=$filePath")

                    // Update last saved snapshot signature
                    //lastSavedDetectionSignature = currentSignature
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
        logger.d("Scan type set to $type")
    }

    fun updateTargetCount(target: Int) {
        _uiState.update { it.copy(targetCount = target) }
        viewModelScope.launch {
            pillCountTxnDao.updateTargetCount(preferenceHelper.getTxnId(), target)
            logger.i("Target count updated → $target")
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
            logger.d("Txn info loaded → drug=${txnInfo?.drugName}, target=${txnInfo?.targetCount}")
        }
    }

    fun resetRestrictAdd() {
        _uiState.update { it.copy(restrictAdd = false) }
        logger.d("RestrictAdd flag reset")
    }

    fun resetNoTransaction() {
        _uiState.update { it.copy(showNoTransaction = false) }
        logger.d("NoTransaction flag reset")
    }

    fun setTargetCountDialogShown(shown: Boolean) {
        _uiState.update { it.copy(showTargetCountDialog = shown) }
    }

    fun setNoteDialogShown(shown: Boolean) {
        _uiState.update { it.copy(showNotesDialog = shown) }
    }
}
