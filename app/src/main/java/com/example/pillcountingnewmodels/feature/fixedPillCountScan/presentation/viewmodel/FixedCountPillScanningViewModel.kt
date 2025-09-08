package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data.DetectedPill
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data.FixedCountPillScanningEvent
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.model.FixedCountPillScanningUiState
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.logic.PillAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

@HiltViewModel
class FixedCountPillScanningViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val logger = AppLogger.create<FixedCountPillScanningViewModel>()

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

    companion object {
        private const val MODEL_FILENAME = "best_float32_new.tflite"
        private const val TAG = "PillVM"
    }

    fun initializeInterpreter(retryCount: Int = 1) {
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
                    logger.i("Loading TFLite model (attempt ${attempt + 1})…")

                    val buffer = loadModelFile(MODEL_FILENAME)
                    val options = Interpreter.Options().apply {
                        setUseXNNPACK(true)
                        numThreads = Runtime.getRuntime().availableProcessors().coerceAtMost(4)
                    }

                    interpreter = Interpreter(buffer, options).also {
                        success = true
                        val analyzer = PillAnalyzer(it)
                        val duration = System.currentTimeMillis() - start
                        logger.i("✅ Interpreter ready in ${duration}ms")
                        _modelState.value = ModelState.Ready(analyzer)
                    }

                } catch (e: Exception) {
                    attempt++
                    logger.e("❌ Failed to initialize interpreter (attempt $attempt)", e)

                    if (attempt > retryCount) {
                        _modelState.value = ModelState.Error("Interpreter initialization failed", e)
                    } else {
                        logger.w("Retrying interpreter initialization…")
                    }
                }
            }
        }
    }

    fun updateDetectedPills(pills: List<DetectedPill>) {
        _uiState.value = _uiState.value.copy(detectedPills = pills)
    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.buffer.get(nv21, 0, ySize)
        vPlane.buffer.get(nv21, ySize, vSize)
        uPlane.buffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    }

    override fun onCleared() {
        super.onCleared()
        interpreter?.close()
        interpreter = null
        _modelState.value = ModelState.Idle
        logger.i("Interpreter closed and resources released")
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

    fun onEvent(event: FixedCountPillScanningEvent) {
        when (event) {
            is FixedCountPillScanningEvent.AddBatchClicked -> {
                logger.i("Add batch clicked")
                _uiState.value = _uiState.value.copy(
                    batchHistory = _uiState.value.batchHistory
                )
            }
            is FixedCountPillScanningEvent.RescanClicked -> {
                logger.i("Rescan requested")
                _uiState.value = _uiState.value.copy(detectedPills = emptyList())
            }
            is FixedCountPillScanningEvent.PauseClicked -> {
                logger.i("Pause clicked")
                // TODO: handle pause (e.g., stop camera flow temporarily)
            }
            is FixedCountPillScanningEvent.DoneClicked -> {
                logger.i("Done clicked — finalize process")
                // TODO: trigger save, navigation, etc.
            }
        }
    }
}

