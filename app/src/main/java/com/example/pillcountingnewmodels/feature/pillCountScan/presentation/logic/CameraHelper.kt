package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic

import android.content.Context
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.pillcountingnewmodels.core.utils.AppLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class CameraHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val executor: Executor
) {

    //logger
    private val logger = AppLogger.create<CameraHelper>()

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var boundCamera: Camera? = null

    // Prevent re-binding on rotation flicker
    private val isBound = AtomicBoolean(false)

    // Channel for sending frames to the analyzer (conflated = latest only)
    private val _frameChannel = Channel<ImageProxy>(Channel.CONFLATED)
    val frameFlow = _frameChannel.receiveAsFlow()

    /**
     * Starts the camera preview and image analysis.
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
                logger.w("Camera already bound — skipping re-bind")
                return@addListener
            }

            try {
                preview = Preview.Builder()
                    .setTargetResolution(targetResolution)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(targetResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setOutputImageRotationEnabled(true) // Auto-rotate frames
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { image ->
                            try {
                                if (!_frameChannel.isClosedForSend) {
                                    val sent = _frameChannel.trySend(image).isSuccess
                                    if (!sent) {
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
                    }

                cameraProvider.unbindAll()
                boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                isBound.set(true)
                logger.i("Camera successfully bound to lifecycle")

            } catch (e: Exception) {
                logger.e("Failed to bind camera to lifecycle", e)
                isBound.set(false)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Stops the camera and releases resources.
     */
    fun stopCamera() {
        try {
            val provider = cameraProviderFuture.get()
            provider.unbindAll()
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
     * Checks if the camera is currently running.
     */
    fun isCameraRunning(): Boolean = isBound.get()
}
