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
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages CameraX setup, lifecycle, and frame processing pipeline.
 *
 * This helper class simplifies the integration of CameraX by handling:
 * - Camera provider initialization.
 * - Binding of `Preview` and `ImageAnalysis` use cases to a lifecycle.
 * - Providing a reactive `Flow` of camera frames for analysis.
 * - Exposing camera controls for features like Torch and Zoom.
 * - Publishing the current camera state (torch status, zoom levels).
 *
 * @param context The application context.
 * @param lifecycleOwner The lifecycle owner (typically a Fragment or Activity) to which the camera lifecycle is bound.
 * @param executor A background executor for camera operations and frame analysis.
 */
class CameraHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val executor: Executor
) {
    // Logger for this class
    private val logger = AppLogger.create<CameraHelper>()

    // Future for obtaining the CameraProvider instance
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context)

    // CameraX Use Cases
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var boundCamera: Camera? = null

    // State to prevent re-binding during configuration changes (e.g., rotation)
    private val isBound = AtomicBoolean(false)

    // Channel to send frames from the camera to the analyzer.
    // CONFLATED ensures only the latest frame is processed, dropping older ones if the consumer is slow.
    private val _frameChannel = Channel<ImageProxy>(Channel.CONFLATED)
    val frameFlow = _frameChannel.receiveAsFlow()

    /**
     * Data class to hold the current state of the camera.
     */
    data class CameraState(
        val isTorchOn: Boolean = false,
        val zoomRatio: Float = 1.0f,
        val minZoomRatio: Float = 1.0f,
        val maxZoomRatio: Float = 1.0f
    )

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState = _cameraState.asStateFlow()

    /**
     * Initializes and starts the camera, binding it to the provided lifecycle and preview view.
     *
     * @param previewView The [PreviewView] to display the camera feed.
     * @param targetResolution The desired resolution for image analysis.
     */
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
                // Configure the Preview use case
                preview = Preview.Builder()
                    .setTargetResolution(targetResolution)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                // Select the default back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Configure the ImageAnalysis use case
                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(targetResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setOutputImageRotationEnabled(true) // Ensures frames are oriented upright
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor, this::processImageProxy)
                    }

                // Unbind any previous use cases before re-binding
                cameraProvider.unbindAll()

                // Bind the use cases to the camera
                boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // After binding, set up observers for camera state
                observeCameraState()

                isBound.set(true)
                logger.i("Camera successfully bound to lifecycle")

            } catch (e: Exception) {
                logger.e("Failed to bind camera to lifecycle", e)
                isBound.set(false)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * The core analyzer function passed to [ImageAnalysis]. It receives frames,
     * attempts to send them to the processing channel, and ensures they are closed.
     */
    private fun processImageProxy(image: ImageProxy) {
        try {
            if (!_frameChannel.isClosedForSend) {
                // trySend is non-blocking and returns false if the channel is full.
                // With a CONFLATED channel, this helps drop frames when the analyzer is busy.
                if (!_frameChannel.trySend(image).isSuccess) {
                    logger.d("Dropped frame—channel busy")
                    image.close() // Must close the image manually if not sent
                }
            } else {
                logger.w("Frame channel closed—discarding frame")
                image.close()
            }
        } catch (t: Throwable) {
            logger.e("Analyzer crashed while processing frame", t)
            image.close() // Always ensure the image is closed to prevent stalls
        }
    }

    /**
     * Sets up observers on the bound camera's state (torch, zoom) to provide reactive updates.
     */
    private fun observeCameraState() {
        val cameraInfo = boundCamera?.cameraInfo ?: return
        val zoomState = cameraInfo.zoomState.value

        // Initialize state
        _cameraState.update {
            it.copy(
                isTorchOn = cameraInfo.torchState.value == TorchState.ON,
                zoomRatio = zoomState?.zoomRatio ?: 1f,
                minZoomRatio = zoomState?.minZoomRatio ?: 1f,
                maxZoomRatio = zoomState?.maxZoomRatio ?: 1f
            )
        }

        // Observe torch state changes
        cameraInfo.torchState.observe(lifecycleOwner) { torchState ->
            _cameraState.update { it.copy(isTorchOn = torchState == TorchState.ON) }
        }

        // Observe zoom state changes
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

    /**
     * Stops the camera and releases all associated resources.
     */
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
        }
    }

    /**
     * Toggles the camera's torch (flash) on or off.
     *
     * @param enable True to turn the torch on, false to turn it off.
     */
    fun enableTorch(enable: Boolean) {
        boundCamera?.cameraControl?.enableTorch(enable)?.addListener({
            logger.i("Torch state set to: $enable")
        }, executor)
    }

    /**
     * Sets the camera's zoom ratio.
     * The provided ratio will be clamped within the camera's supported min/max range.
     *
     * @param ratio The desired zoom ratio (e.g., 1.0 for no zoom, 2.0 for 2x zoom).
     */
    fun setZoomRatio(ratio: Float) {
        boundCamera?.cameraControl?.setZoomRatio(ratio)?.addListener({
            logger.i("Zoom ratio set to: $ratio")
        }, executor)
    }

    /**
     * Checks if the camera is currently bound and running.
     */
    fun isCameraRunning(): Boolean = isBound.get()
}