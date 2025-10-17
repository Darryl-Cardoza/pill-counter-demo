package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.content.Context
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.rite.pillcounting.core.utils.logger.AppLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class CameraHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val executor: Executor
) {
    private val logger = AppLogger.create<CameraHelper>()

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context)

    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var boundCamera: Camera? = null

    private val isBound = AtomicBoolean(false)

    private val _frameChannel = Channel<ImageProxy>(Channel.CONFLATED)
    val frameFlow = _frameChannel.receiveAsFlow()

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState = _cameraState.asStateFlow()

    // 🟢 NEW: flag to control frame streaming
    private val isStreaming = AtomicBoolean(true)

    data class CameraState(
        val isTorchOn: Boolean = false,
        val zoomRatio: Float = 1.0f,
        val minZoomRatio: Float = 1.0f,
        val maxZoomRatio: Float = 1.0f
    )

    fun startCamera(
        previewView: PreviewView,
        targetResolution: Size = Size(1280, 720)
    ) {
        logger.i("Starting camera with resolution=${targetResolution.width}x${targetResolution.height}")

        cameraProviderFuture.addListener({
            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (e: Exception) {
                logger.e("Failed to get CameraProvider", e)
                return@addListener
            }

            if (isBound.get()) {
                logger.w("Camera already bound—skipping re-bind")
                return@addListener
            }

            try {
                preview = Preview.Builder()
                    .setTargetResolution(targetResolution)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(targetResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setOutputImageRotationEnabled(true)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor, this::processImageProxy)
                    }

                cameraProvider.unbindAll()

                boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                observeCameraState()

                isBound.set(true)
                isStreaming.set(true) // ✅ ensure streaming starts
                logger.i("Camera successfully bound to lifecycle")

            } catch (e: Exception) {
                logger.e("Failed to bind camera to lifecycle", e)
                isBound.set(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun processImageProxy(image: ImageProxy) {
        try {
            // 🟢 Modified: respect pause/resume state
            if (!isStreaming.get()) {
                image.close()
                return
            }

            if (!_frameChannel.isClosedForSend) {
                if (!_frameChannel.trySend(image).isSuccess) {
                    logger.d("Dropped frame—channel busy")
                    image.close()
                }
            } else {
                logger.w("Frame channel closed—discarding frame")
                image.close()
            }
        } catch (t: Throwable) {
            logger.e("Analyzer crashed while processing frame", t)
            image.close()
        }
    }

    private fun observeCameraState() {
        val cameraInfo = boundCamera?.cameraInfo ?: return
        val zoomState = cameraInfo.zoomState.value

        _cameraState.update {
            it.copy(
                isTorchOn = cameraInfo.torchState.value == TorchState.ON,
                zoomRatio = zoomState?.zoomRatio ?: 1f,
                minZoomRatio = zoomState?.minZoomRatio ?: 1f,
                maxZoomRatio = zoomState?.maxZoomRatio ?: 1f
            )
        }

        cameraInfo.torchState.observe(lifecycleOwner) { torchState ->
            _cameraState.update { it.copy(isTorchOn = torchState == TorchState.ON) }
        }

        cameraInfo.zoomState.observe(lifecycleOwner) { state ->
            _cameraState.update {
                it.copy(
                    zoomRatio = state.zoomRatio,
                    minZoomRatio = state.minZoomRatio,
                    maxZoomRatio = state.maxZoomRatio
                )
            }
        }
    }

    fun stopCamera() {
        try {
            cameraProviderFuture.get().unbindAll()
            logger.i("Camera unbound successfully")
        } catch (e: Exception) {
            logger.e("Error while unbinding camera", e)
        } finally {
            preview = null
            imageAnalysis = null
            boundCamera = null
            isBound.set(false)
            isStreaming.set(false)
        }
    }

    fun enableTorch(enable: Boolean) {
        boundCamera?.cameraControl?.enableTorch(enable)?.addListener({
            logger.i("Torch state set to: $enable")
        }, executor)
    }

    fun setZoomRatio(ratio: Float) {
        boundCamera?.cameraControl?.setZoomRatio(ratio)?.addListener({
            logger.i("Zoom ratio set to: $ratio")
        }, executor)
    }

    fun isCameraRunning(): Boolean = isBound.get()

    // 🟢 NEW PUBLIC CONTROLS
    fun pauseStreaming() {
        logger.i("Camera frame streaming paused")
        isStreaming.set(false)
    }

    fun resumeStreaming() {
        logger.i("Camera frame streaming resumed")
        isStreaming.set(true)
    }

    fun pauseCamera() {
        logger.i("Pausing camera (unbinding use cases)")
        try {
            cameraProviderFuture.get().unbindAll()
        } catch (e: Exception) {
            logger.e("Error while pausing camera", e)
        } finally {
            preview = null
            imageAnalysis = null
            boundCamera = null
            isBound.set(false)
        }
    }

    fun resumeCamera(previewView: PreviewView, targetResolution: Size = Size(1280, 720)) {
        logger.i("Resuming camera by re-binding use cases")
        startCamera(previewView, targetResolution)
    }

}
