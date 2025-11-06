package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.rite.pillcounting.core.utils.logger.AppLogger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ## CameraHelper
 *
 * A unified utility class that manages CameraX setup, lifecycle binding, and frame streaming
 * for the **Pill Counting** module.
 *
 * This class abstracts away all CameraX boilerplate while maintaining predictable
 * lifecycle behavior and safe frame delivery.
 */
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
    private var previewView: PreviewView? = null

    private val isBound = AtomicBoolean(false)
    private val isStreaming = AtomicBoolean(true)

    private val _frameChannel = Channel<ImageProxy>(Channel.CONFLATED)

    /** Public flow of camera frames emitted for ML analysis. */
    val frameFlow = _frameChannel.receiveAsFlow()

    private val _cameraState = MutableStateFlow(CameraState())

    /**
     * Represents the current camera state (zoom, torch, etc.)
     */
    data class CameraState(
        val isTorchOn: Boolean = false,
        val zoomRatio: Float = 1.0f,
        val minZoomRatio: Float = 1.0f,
        val maxZoomRatio: Float = 1.0f
    )

    // ------------------------------------------------------------------------
    // CAMERA INITIALIZATION
    // ------------------------------------------------------------------------

    /**
     * Initializes and starts the CameraX pipeline.
     *
     * @param previewView The [PreviewView] into which the camera feed will be rendered.
     * @param targetResolution The preferred preview resolution (default 1280×720).
     */
    fun startCamera(
        previewView: PreviewView,
        targetResolution: Size = Size(1280, 720)
    ) {
        logger.i("Starting camera | Target=${targetResolution.width}×${targetResolution.height}")
        this.previewView = previewView

        cameraProviderFuture.addListener({
            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (e: Exception) {
                logger.e("Failed to obtain CameraProvider", e)
                return@addListener
            }

            if (isBound.get()) {
                logger.w("Camera already bound — skipping rebind.")
                return@addListener
            }

            try {
                // --- Resolution configuration ---
                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            targetResolution,
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                        )
                    )
                    .build()

                // --- Preview pipeline ---
                preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                // --- Image analysis pipeline ---
                imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setOutputImageRotationEnabled(true)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                            processImageProxy(
                                image
                            )
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // --- Bind to lifecycle ---
                cameraProvider.unbindAll()
                boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                observeCameraState()
                isBound.set(true)
                isStreaming.set(true)

                logger.i("Camera successfully bound to lifecycle")

                // --- Initial adjustments ---
                boundCamera?.let { cam ->
                    cam.cameraControl.setZoomRatio(1.5f)
                    setCenterFocus(previewView)
                }

            } catch (e: Exception) {
                logger.e("Failed to bind camera use cases", e)
                isBound.set(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // ------------------------------------------------------------------------
    // FRAME PROCESSING
    // ------------------------------------------------------------------------

    /**
     * Handles incoming frames from CameraX and emits them via [frameFlow].
     * Drops frames if analysis is still in progress.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun processImageProxy(image: ImageProxy) {
        try {
            if (!isStreaming.get()) {
                image.close()
                return
            }

            if (!_frameChannel.isClosedForSend) {
                if (!_frameChannel.trySend(image).isSuccess) {
                    logger.d("Frame dropped — downstream analyzer busy")
                    image.close()
                }
            } else {
                image.close()
            }
        } catch (t: Throwable) {
            logger.e("Analyzer crashed while processing frame", t)
            image.close()
        }
    }

    // ------------------------------------------------------------------------
    // CAMERA STATE OBSERVATION
    // ------------------------------------------------------------------------

    /**
     * Observes torch and zoom state changes from [CameraInfo] and exposes them via [_cameraState].
     */
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

        cameraInfo.torchState.observe(lifecycleOwner) { torch ->
            _cameraState.update { it.copy(isTorchOn = torch == TorchState.ON) }
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

    // ------------------------------------------------------------------------
    // CAMERA CONTROL FUNCTIONS
    // ------------------------------------------------------------------------

    /**
     * Sets the current camera zoom ratio.
     *
     * @param zoomRatio Desired zoom multiplier within min–max bounds.
     */
    fun setZoom(zoomRatio: Float) {
        try {
            val camera = boundCamera ?: return
            val info = camera.cameraInfo
            val control = camera.cameraControl
            val state = info.zoomState.value ?: return

            val newZoom = zoomRatio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
            control.setZoomRatio(newZoom)
            logger.i(
                "Zoom set to %.2fx (Range %.2f–%.2f)"
                    .format(newZoom, state.minZoomRatio, state.maxZoomRatio)
            )
        } catch (e: Exception) {
            logger.e("Failed to set camera zoom", e)
        }
    }

    /**
     * Returns the current zoom ratio, or `null` if unavailable.
     */
    fun getCurrentZoomRatio(): Float? =
        boundCamera?.cameraInfo?.zoomState?.value?.zoomRatio

    /**
     * Triggers an autofocus action at the center of the [PreviewView].
     */
    fun setCenterFocus(previewView: PreviewView) {
        try {
            val camera = boundCamera ?: return
            val factory = previewView.meteringPointFactory
            val center = factory.createPoint(previewView.width / 2f, previewView.height / 2f)

            val action = FocusMeteringAction.Builder(center)
                .addPoint(center, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                .build()

            camera.cameraControl.startFocusAndMetering(action)
            logger.i("Center autofocus triggered")
        } catch (e: Exception) {
            logger.e("Failed to perform autofocus", e)
        }
    }

    // ------------------------------------------------------------------------
    // CAMERA LIFECYCLE CONTROL
    // ------------------------------------------------------------------------

    /**
     * Pauses the camera by unbinding all active use cases.
     * The preview and analyzer pipelines are released.
     */
    fun pauseCamera() {
        logger.i("Pausing camera — unbinding all use cases")
        try {
            val provider = cameraProviderFuture.get()
            provider.unbindAll()
        } catch (e: Exception) {
            logger.e("Error while pausing camera", e)
        } finally {
            preview = null
            imageAnalysis = null
            boundCamera = null
            isBound.set(false)
            isStreaming.set(false)
        }
    }

    /**
     * Resumes the camera by reinitializing the pipeline.
     *
     * @param previewView The [PreviewView] instance to rebind.
     * @param targetResolution Preferred output resolution (default 1280×720).
     */
    fun resumeCamera(previewView: PreviewView, targetResolution: Size = Size(1280, 720)) {
        logger.i("Resuming camera preview")
        startCamera(previewView, targetResolution)
    }

    // ------------------------------------------------------------------------
    // HELPER FUNCTIONS
    // ------------------------------------------------------------------------

    /** Returns the current [PreviewView] width in pixels, or 640 if unavailable. */
    fun getPreviewWidth(): Int = previewView?.width ?: 640

    /** Returns the current [PreviewView] height in pixels, or 640 if unavailable. */
    fun getPreviewHeight(): Int = previewView?.height ?: 640

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(
            nv21, android.graphics.ImageFormat.NV21, image.width, image.height, null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 60, out)
        val jpegBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }
}
