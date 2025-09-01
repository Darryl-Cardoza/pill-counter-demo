package com.example.pillcountingnewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import org.tensorflow.lite.Interpreter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

// ENHANCEMENT: Enums for camera state and focus results for clearer UI logic.
enum class CameraState { ACTIVE, ASLEEP }
sealed class FocusResult {
    object Success : FocusResult()
    object Failure : FocusResult()
}

class CameraHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val coroutineScope: CoroutineScope,
    private val previewView: PreviewView,
    private val interpreter: Interpreter,
    private val isLiveAnalyzingFlow: StateFlow<Boolean>,
    private val onPillCountUpdated: (Int, List<PillAnalyzer.Detection>) -> Unit // UPDATED: Now accepts a list of detections
) : LifecycleEventObserver {

    companion object {
        private const val TAG = "CameraHelper"
        private val ANALYSIS_TARGET_RESOLUTION = Size(1280, 720)
        private const val SLEEP_DELAY_MS = 15000L // 15 seconds of inactivity
    }

    private lateinit var analysisExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null

    // --- NEW AND UPDATED STATES ---
    private val _cameraState = MutableStateFlow(CameraState.ASLEEP)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _focusResult = MutableStateFlow<FocusResult?>(null)
    val focusResult: StateFlow<FocusResult?> = _focusResult.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _cameraError = MutableStateFlow<CameraError?>(null)
    val cameraError: StateFlow<CameraError?> = _cameraError.asStateFlow()

    private val oneShotAnalysisRequested = AtomicBoolean(false)
    private var sleepJob: Job? = null // Job to manage the sleep timer

    // FIXED: The PillAnalyzer instance is now initialized in `bindUseCases`
    // where the previewView dimensions are available, which is more reliable.
    private var analyzer: PillAnalyzer? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun initializeAndStartCamera() {
        wakeUp()
    }

    fun wakeUp() {
        coroutineScope.launch {
            Log.d(TAG, "Waking up camera...")
            try {
                if (!::analysisExecutor.isInitialized || analysisExecutor.isShutdown) {
                    analysisExecutor = Executors.newSingleThreadExecutor()
                }

                if (cameraProvider == null) {
                    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
                    cameraProvider = cameraProviderFuture.await()
                }

                bindUseCases()
                _cameraState.value = CameraState.ACTIVE
                resetSleepTimer()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to wake up/initialize camera: ${e.message}", e)
                _cameraError.value = CameraError.INITIALIZATION_FAILED
            }
        }
    }

    private fun sleep() {
        Log.d(TAG, "Putting camera to sleep...")
        cameraProvider?.unbindAll()
        _cameraState.value = CameraState.ASLEEP
    }

    fun resetSleepTimer() {
        sleepJob?.cancel()
        sleepJob = coroutineScope.launch {
            delay(SLEEP_DELAY_MS)
            Log.d(TAG, "Inactivity timer finished.")
            sleep()
        }
    }

    private fun bindUseCases() {
        val preview = Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }

        // ADDED: Initialize the PillAnalyzer here where the dimensions are reliable.
        val viewWidth = previewView.width
        val viewHeight = previewView.height
        analyzer = PillAnalyzer(
            interpreter,
            viewWidth,
            viewHeight,
            onPillCountUpdated // Pass the updated callback
        )

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(ANALYSIS_TARGET_RESOLUTION)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply { setAnalyzer(analysisExecutor, this@CameraHelper::analyzeFrame) }

        try {
            cameraProvider?.unbindAll()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            camera = cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            observeTorchState()
            Log.d(TAG, "Camera use cases bound successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind use cases: ${e.message}", e)
            _cameraError.value = CameraError.BINDING_FAILED
        }
    }

    fun triggerAnalysis() {
        resetSleepTimer()
        Log.d(TAG, "One-shot analysis triggered.")
        oneShotAnalysisRequested.set(true)
    }

    fun toggleTorch() {
        resetSleepTimer()
        _isTorchOn.value = !_isTorchOn.value
    }

    fun handleTapToFocus(factory: MeteringPointFactory, x: Float, y: Float) {
        resetSleepTimer()
        _focusResult.value = null
        val action = FocusMeteringAction.Builder(factory.createPoint(x, y), FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val focusFuture = camera?.cameraControl?.startFocusAndMetering(action)
        focusFuture?.addListener({
            try {
                val result = focusFuture.get()
                if (result.isFocusSuccessful) {
                    _focusResult.value = FocusResult.Success
                    Log.d(TAG, "Focus successful.")
                } else {
                    _focusResult.value = FocusResult.Failure
                    Log.d(TAG, "Focus failed.")
                }
            } catch (e: Exception) {
                _focusResult.value = FocusResult.Failure
                Log.e(TAG, "Focus threw exception", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val shouldAnalyzeOneShot = oneShotAnalysisRequested.getAndSet(false)
        if (shouldAnalyzeOneShot || isLiveAnalyzingFlow.value) {
            // FIXED: Use the nullable analyzer instance
            analyzer?.analyze(imageProxy)
        } else {
            imageProxy.close()
        }
    }

    private fun observeTorchState() {
        val observer = Observer<Int> { torchState ->
            _isTorchOn.value = (torchState == TorchState.ON)
        }
        camera?.cameraInfo?.torchState?.observe(lifecycleOwner, observer)
    }

    private fun shutdown() {
        Log.d(TAG, "Shutting down camera helper.")
        sleepJob?.cancel()
        cameraProvider?.unbindAll()
        if (::analysisExecutor.isInitialized) {
            analysisExecutor.shutdown()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            shutdown()
            source.lifecycle.removeObserver(this)
        }
    }
}

sealed class CameraError {
    object INITIALIZATION_FAILED : CameraError()
    object BINDING_FAILED : CameraError()
}