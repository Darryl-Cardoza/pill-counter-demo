package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.analyzer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.pillcountingnewmodels.core.utils.common.HelperFunctions.saveBitmapToFile
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A professional and reusable MLKit barcode analyzer.
 *
 * Handles lifecycle events, pause/resume, and robust CameraX binding.
 * Supports single-scan and continuous-scan modes.
 */
@Singleton
class BarcodeAnalyzer @Inject constructor(
    private val appContext: Context
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var scanner: BarcodeScanner? = null
    private var cameraJob: Job? = null
    private var isPaused = AtomicBoolean(false)
    private var isActive = AtomicBoolean(false)
    private var hasScannedOnce = AtomicBoolean(false)

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Start the camera and attach MLKit barcode analyzer.
     *
     * @param previewView The PreviewView for showing the camera feed
     * @param lifecycleOwner Lifecycle for automatic binding/unbinding
     * @param singleScanMode If true, pauses scanning after first success
     */
    fun start(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        singleScanMode: Boolean = true,
        barcodeFormats: Int = Barcode.FORMAT_ALL_FORMATS,
        onBarcodeDetected: (String, String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (isActive.get()) {
            Log.w(TAG, "Analyzer already running.")
            return
        }
        isActive.set(true)
        hasScannedOnce.set(false)
        isPaused.set(false)

        cameraJob = ioScope.launch(Dispatchers.Main) {
            try {
                val providerFuture = ProcessCameraProvider.getInstance(appContext)
                cameraProvider = providerFuture.get()

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(barcodeFormats)
                    .build()

                scanner = BarcodeScanning.getClient(options)

                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().apply {
                        setAnalyzer(ContextCompat.getMainExecutor(appContext)) { imageProxy ->
                            analyzeImage(imageProxy, singleScanMode, onBarcodeDetected, onError)
                        }
                    }

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )

                Log.i(TAG, "Camera and Analyzer started successfully.")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera: ${e.message}")
                onError(e)
                stop()
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeImage(
        imageProxy: ImageProxy,
        singleScanMode: Boolean,
        onBarcodeDetected: (String, String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || isPaused.get() || !isActive.get()) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        // ✅ Make a safe copy immediately before MLKit processing
        val safeBitmapCopy = try {
            imageProxy.toBitmap() // small cost, but safe
        } catch (e: Exception) {
            null
        }

        val image = InputImage.fromMediaImage(mediaImage, rotation)

        scanner?.process(image)
            ?.addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull()
                if (barcode != null && (!singleScanMode || !hasScannedOnce.get())) {
                    hasScannedOnce.set(true)

                    ioScope.launch {
                        val filePath = safeBitmapCopy?.let {
                            saveBitmapToFile(
                                appContext,
                                it,
                                "barcode_${System.currentTimeMillis()}.jpg"
                            )
                        }
                        withContext(Dispatchers.Main) {
                            onBarcodeDetected(barcode.rawValue.orEmpty(), filePath)
                            if (singleScanMode) pause()
                        }
                    }
                }
            }
            ?.addOnFailureListener { ex ->
                Log.e(TAG, "Barcode detection failed: ${ex.message}")
                onError(ex)
            }
            ?.addOnCompleteListener {
                // ✅ Always close after all listeners complete
                imageProxy.close()
            }
    }


    /**
     * Pause scanning but keep camera feed active.
     */
    fun pause() {
        if (isActive.get()) {
            isPaused.set(true)
            Log.d(TAG, "Analyzer paused.")
        }
    }

    /**
     * Resume scanning after pause.
     */
    fun resume() {
        if (isActive.get()) {
            isPaused.set(false)
            hasScannedOnce.set(false)
            Log.d(TAG, "Analyzer resumed.")
        }
    }

    /**
     * Completely stop scanning and unbind all resources.
     */
    fun stop() {
        try {
            cameraProvider?.unbindAll()
            imageAnalysis?.clearAnalyzer()
            isActive.set(false)
            isPaused.set(false)
            hasScannedOnce.set(false)
            scanner?.close()
            Log.d(TAG, "Analyzer stopped and resources released.")
        } catch (e: Exception) {
            Log.e(TAG, "Stop error: ${e.message}")
        }
    }

    /**
     * Destroy analyzer and cancel background coroutines.
     */
    fun destroy() {
        stop()
        cameraJob?.cancel()
        ioScope.cancel()
        Log.d(TAG, "Analyzer fully destroyed.")
    }

    companion object {
        private const val TAG = "BarcodeAnalyzer"
    }
}
