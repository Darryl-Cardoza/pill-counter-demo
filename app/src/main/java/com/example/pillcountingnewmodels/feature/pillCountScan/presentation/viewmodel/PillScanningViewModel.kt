package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.room.dao.PillCountTxnDetailsDao
import com.example.pillcountingnewmodels.core.room.models.PillCountTxnDetailsEntity
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.FixedCountPillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.Batch
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.DetectedPill
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.FixedCountPillScanningUiState
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic.PillAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow(FixedCountPillScanningUiState())
    val uiState = _uiState.asStateFlow()

    private var interpreter: Interpreter? = null
    private var currentFrameBitmap: Bitmap? = null

    companion object {
        private const val MODEL_FILENAME = "best_float32_new.tflite"
        private const val TAG = "PillScanningVM"
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
    fun onEvent(event: FixedCountPillScanningEvent) {
        when (event) {
            is FixedCountPillScanningEvent.AddBatchClicked -> {
                val currentCount = _uiState.value.detectedPills.size
                logger.i("Add batch clicked with count: $currentCount")

                if (currentCount == 0) {
                    logger.w("Skipping add batch because current count is zero.")
                    return
                }

                _uiState.update { currentState ->
                    val nextBatchNumber = currentState.batchHistory.size + 1
                    // Create the new batch with the stored thumbnail
                    val newBatch = Batch(
                        count = currentCount,
                        batchNumber = nextBatchNumber,
                        thumbnail = currentFrameBitmap
                    )

                    currentState.copy(
                        batchHistory = currentState.batchHistory + newBatch,
                        detectedPills = emptyList()
                    )

                }
                currentFrameBitmap = null
                viewModelScope.launch {
                    val detail = PillCountTxnDetailsEntity(
                        txnId = preferenceHelper.getTxnId(), // FK to transaction
                        txnDetailsNo = uiState.value.batchNumber,
                        pillCount = currentCount,
                        imagePath = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    pillCountTxnDetailsDao.insert(detail)
                }

            }
            is FixedCountPillScanningEvent.RescanClicked -> {
                logger.i("Rescan requested")
                _uiState.update { it.copy(detectedPills = emptyList()) }
            }
            is FixedCountPillScanningEvent.PauseClicked -> {
                logger.i("Pause clicked")
                // TODO: stop camera flow temporarily
            }
            is FixedCountPillScanningEvent.DoneClicked -> {
                logger.i("Done clicked — finalize process")
                // TODO: trigger save/navigation
            }
        }
    }

    fun setScanType(type: String) {
        _uiState.update { it.copy(scanType = type) }
    }
}