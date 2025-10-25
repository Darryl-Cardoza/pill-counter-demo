package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.content.Context
import android.util.Size
import androidx.camera.core.Camera
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
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraHelper — manages CameraX preview, zoom, and frame analysis.
 *
 * Features:
 * - Starts and binds the camera lifecycle with preview and analyzer.
 * - Streams frames via [frameFlow] for ML analysis.
 * - Supports pause/resume functionality.
 * - Provides zoom and autofocus utilities.
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

    private val isBound = AtomicBoolean(false)
    private val isStreaming = AtomicBoolean(true)

    private val _frameChannel = Channel<ImageProxy>(Channel.CONFLATED)
    val frameFlow = _frameChannel.receiveAsFlow()

    private val _cameraState = MutableStateFlow(CameraState())

    private var lastDetections: List<DetectedPill> = emptyList()

    /**
     * Represents real-time camera parameters.
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
     * Starts the camera and binds Preview + ImageAnalysis.
     *
     * @param previewView The CameraX PreviewView surface.
     * @param targetResolution Desired target resolution.
     */
    fun startCamera(
        previewView: PreviewView,
        targetResolution: Size = Size(1280, 720)
    ) {
        logger.i("Starting camera | target=${targetResolution.width}x${targetResolution.height}")

        cameraProviderFuture.addListener({
            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (e: Exception) {
                logger.e("Failed to get CameraProvider", e)
                return@addListener
            }

            if (isBound.get()) {
                logger.w("Camera already bound — skipping rebind.")
                return@addListener
            }

            try {
                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            targetResolution,
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                        )
                    )
                    .build()

                preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setOutputImageRotationEnabled(true)
                    .build()
                    .also { analysis -> analysis.setAnalyzer(executor, this::processImageProxy) }

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

                // Apply default zoom & focus
                boundCamera?.let { cam ->
                    cam.cameraControl.setZoomRatio(1.6f) // Default zoom ratio
                    setCenterFocus(previewView)
                }

            } catch (e: Exception) {
                logger.e("Failed to bind camera to lifecycle", e)
                isBound.set(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // ------------------------------------------------------------------------
    // FRAME PROCESSING
    // ------------------------------------------------------------------------

    @OptIn(DelicateCoroutinesApi::class)
    private fun processImageProxy(image: ImageProxy) {
        try {
            if (!isStreaming.get()) {
                image.close()
                return
            }

            if (!_frameChannel.isClosedForSend) {
                if (!_frameChannel.trySend(image).isSuccess) {
                    logger.d("Dropped frame — channel busy")
                    image.close()
                }
            } else {
                logger.w("Frame channel closed — discarding frame")
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

    // ------------------------------------------------------------------------
    // CAMERA CONTROL FUNCTIONS
    // ------------------------------------------------------------------------

    /**
     * Adjusts the zoom ratio of the current camera.
     *
     * @param zoomRatio Desired zoom level (e.g. 1.5f for 1.5× zoom)
     */
    fun setZoom(zoomRatio: Float) {
        try {
            val camera = boundCamera ?: return
            val info = camera.cameraInfo
            val control = camera.cameraControl
            val state = info.zoomState.value ?: return

            val newZoom = zoomRatio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
            control.setZoomRatio(newZoom)
            logger.i("Zoom set to $newZoom× (range ${state.minZoomRatio}–${state.maxZoomRatio})")
        } catch (e: Exception) {
            logger.e("Failed to set camera zoom", e)
        }
    }

    /**
     * Smoothly transitions the zoom level over time.
     *
     * @param targetZoom Desired target zoom level.
     */
    fun smoothZoomTo(targetZoom: Float) {
        try {
            val camera = boundCamera ?: return
            val state = camera.cameraInfo.zoomState.value ?: return
            val clampedZoom = targetZoom.coerceIn(state.minZoomRatio, state.maxZoomRatio)
            camera.cameraControl.setLinearZoom(
                (clampedZoom - state.minZoomRatio) /
                        (state.maxZoomRatio - state.minZoomRatio)
            )
            logger.i("Smooth zoom transition to $clampedZoom×")
        } catch (e: Exception) {
            logger.e("Smooth zoom failed", e)
        }
    }

    fun getCurrentZoomRatio(): Float? {
        return boundCamera?.cameraInfo?.zoomState?.value?.zoomRatio
    }

    /**
     * Triggers a focus and metering action centered on the PreviewView.
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
            logger.e("Failed to set autofocus", e)
        }
    }

    // ------------------------------------------------------------------------
    // CAMERA LIFECYCLE CONTROL
    // ------------------------------------------------------------------------

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
        logger.i("Resuming camera (re-binding use cases)")
        startCamera(previewView, targetResolution)
    }

}
