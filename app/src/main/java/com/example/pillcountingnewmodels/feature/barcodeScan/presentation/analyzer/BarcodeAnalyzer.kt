package com.example.pillcountingnewmodels.feature.barcodeScan.analyzer

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.Closeable

/**
 * An ImageAnalysis.Analyzer that uses ML Kit to detect barcodes in a camera feed.
 * It processes camera frames, scans for barcodes, and invokes a callback with the result.
 * This analyzer includes state management, intelligent throttling to prevent duplicate scans,
 * and robust lifecycle handling.
 *
 * Implements [Closeable] to ensure ML Kit resources are released properly.
 *
 * @param onBarcodeScanned A callback function that receives the raw string value of a detected barcode.
 * @param onError A callback function for propagating exceptions that occur during analysis.
 */
class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer, Closeable {

    companion object {
        // Increased interval to prevent accidental re-scans if the user's hand shakes.
        private const val SCAN_INTERVAL_MS = 1500L
    }

    private val logger = AppLogger.create<BarcodeAnalyzer>()

    @Volatile
    private var isPaused = false
    private var lastScannedTimestamp = 0L
    private var lastScannedBarcode: String? = null

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    private val scanner = BarcodeScanning.getClient(options)

    init {
        logger.d("BarcodeAnalyzer initialized.")
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // If the analyzer is paused (e.g., after a successful scan), ignore new frames.
        if (isPaused) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            logger.w("Frame analysis skipped: mediaImage was null.")
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val currentTime = System.currentTimeMillis()
                // Check if enough time has passed since the last successful scan.
                if (currentTime - lastScannedTimestamp >= SCAN_INTERVAL_MS) {
                    barcodes.firstOrNull()?.rawValue?.takeIf { it.isNotBlank() }?.let { barcodeValue ->
                        // Process only if it's a new, unique barcode.
                        if (barcodeValue != lastScannedBarcode) {
                            logger.i("Barcode scanned successfully: $barcodeValue")
                            onBarcodeScanned(barcodeValue)
                            lastScannedTimestamp = currentTime
                            lastScannedBarcode = barcodeValue
                            // Automatically pause to prevent immediate re-scans.
                            pause()
                        } else {
                            logger.d("Duplicate barcode ignored: $barcodeValue")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Propagate errors to the caller for UI feedback.
                logger.e("Barcode scanning failed.", exception)
                onError(exception)
            }
            .addOnCompleteListener {
                // It's crucial to close the imageProxy to allow the next frame to be processed.
                imageProxy.close()
            }
    }

    /**
     * Pauses the barcode scanning process. No new frames will be analyzed until resume() is called.
     */
    fun pause() {
        logger.d("Analyzer paused.")
        isPaused = true
    }

    /**
     * Resumes the barcode scanning process and clears the last scanned value to allow for a new scan.
     */
    fun resume() {
        logger.d("Analyzer resumed.")
        isPaused = false
        lastScannedBarcode = null
    }

    /**
     * Closes the underlying ML Kit scanner to release resources.
     * This must be called when the analyzer is no longer needed to prevent memory leaks.
     */
    override fun close() {
        logger.d("Closing BarcodeAnalyzer and releasing resources.")
        scanner.close()
    }
}

