package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.rite.pillcounting.core.utils.logger.AppLogger
import org.tensorflow.lite.Interpreter

/**
 * Handles the complete pill detection pipeline for each camera frame.
 *
 * Workflow:
 * 1. Preprocessing - Converts [ImageProxy] to a normalized [java.nio.ByteBuffer].
 * 2. Model Inference - Runs TensorFlow Lite model for detection.
 * 3. Postprocessing - Maps detections to screen coordinates and prepares output.
 *
 * This class ensures proper error handling, frame cleanup, and detailed logging
 * for profiling performance and diagnosing runtime issues.
 *
 * @property interpreter TensorFlow Lite interpreter instance for inference.
 * @property viewWidth Width of the preview surface.
 * @property viewHeight Height of the preview surface.
 * @property onPillCountUpdated Callback triggered when detections are available.
 */
class PillAnalyzer(
    private val interpreter: Interpreter,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val onPillCountUpdated: (
        pillCount: Int,
        detections: List<Postprocessor.Detection>,
        debugBitmap: Bitmap,
        transformMatrix: Matrix
    ) -> Unit
) {
    private val logger = AppLogger.create<PillAnalyzer>()
    private var lastTransformationMatrix: Matrix? = null

    /**
     * Analyzes a single camera frame and executes the full detection pipeline.
     *
     * The function performs:
     * - Frame preprocessing
     * - Model inference
     * - Postprocessing and transformation mapping
     * - Callback invocation
     *
     * All steps are individually timed and logged for performance insights.
     */
    fun analyze(imageProxy: ImageProxy) {
        var bitmap: Bitmap? = null
        val overallStart = System.currentTimeMillis()

        try {
            logger.d("Starting frame analysis | Image=${imageProxy.width}x${imageProxy.height}, Rotation=${imageProxy.imageInfo.rotationDegrees}°")

            // -----------------------------------------------------
            // STEP 1: PREPROCESSING
            // -----------------------------------------------------
            val preprocessStart = System.currentTimeMillis()
            val (inputBuffer, bmp) = try {
                Preprocessor.preprocess(imageProxy)
            } catch (e: Exception) {
                logger.e("Preprocessing failed: ${e.message}", e)
                imageProxy.close()
                return
            }

            // Save every few frames for debugging rotation/orientation
            /*if ((System.currentTimeMillis() / 5000) % 2L == 0L) {
                bmp.saveDebugCopy(tag = "preprocessed")
            }*/

            bitmap = bmp
            val preprocessTime = System.currentTimeMillis() - preprocessStart
            logger.i("Preprocessing completed in $preprocessTime ms | Bitmap=${bmp.width}x${bmp.height}")

            // -----------------------------------------------------
            // STEP 2: MODEL INFERENCE
            // -----------------------------------------------------
            val inferenceStart = System.currentTimeMillis()
            val detShape = interpreter.getOutputTensor(0).shape()
            //val maskShape = interpreter.getOutputTensor(1).shape()

            val out0 = Array(1) { Array(detShape[1]) { FloatArray(detShape[2]) } }
            //val out1 = Array(1) { Array(maskShape[1]) { Array(maskShape[2]) { FloatArray(maskShape[3]) } } }

            //val outputs = mapOf(0 to out0, 1 to out1)

            try {
                //interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)
                interpreter.run(inputBuffer, out0)
            } catch (e: Exception) {
                logger.e("Model inference failed: ${e.message}", e)
                bitmap.recycle()
                imageProxy.close()
                return
            }

            val inferenceTime = System.currentTimeMillis() - inferenceStart
            //logger.i("Model inference completed in $inferenceTime ms | Output tensors: det=${detShape.contentToString()}, mask=${maskShape.contentToString()}")
            logger.i("Measure time Model inference completed in $inferenceTime ms | Output tensors: det=${detShape.contentToString()}")

            // -----------------------------------------------------
            // STEP 3: POSTPROCESSING
            // -----------------------------------------------------
            val postStart = System.currentTimeMillis()
            val camRotation = imageProxy.imageInfo.rotationDegrees
            val rawIsPortrait = imageProxy.width < imageProxy.height
            val bmpIsPortrait = bitmap.height > bitmap.width

            // Determine the correct rotation adjustment
            val effectiveRotation = when {
                rawIsPortrait && !bmpIsPortrait -> 90
                !rawIsPortrait && bmpIsPortrait -> 90
                else -> camRotation
            }

            logger.d(
                "Postprocessing: raw=${imageProxy.width}x${imageProxy.height}, " +
                        "bitmap=${bitmap.width}x${bitmap.height}, " +
                        "cameraRotation=$camRotation°, effectiveRotation=$effectiveRotation°"
            )

            val (detections, matrix) = try {
                Postprocessor.parseDetections(
                    det = out0[0],
                    imageWidth = bitmap.width,
                    imageHeight = bitmap.height,
                    rotationDegrees = effectiveRotation,
                    viewWidth = viewWidth,
                    viewHeight = viewHeight
                )
            } catch (e: Exception) {
                logger.e("Postprocessing failed: ${e.message}", e)
                bitmap.recycle()
                imageProxy.close()
                return
            }

            val postTime = System.currentTimeMillis() - postStart
            logger.i("Postprocessing completed in $postTime ms | Detections=${detections.size}")

            // -----------------------------------------------------
            // STEP 4: FINAL RESULT CALLBACK
            // -----------------------------------------------------
            val totalTime = System.currentTimeMillis() - overallStart
            logger.i("Measure time Frame analysis successful in $totalTime ms | Pills detected=${detections.size}")

            lastTransformationMatrix = matrix
            onPillCountUpdated(detections.size, detections, bitmap, matrix)

        } catch (e: Exception) {
            logger.e("Exception during frame analysis: ${e.message}", e)
            bitmap?.recycle()
        } finally {
            try {
                imageProxy.close()
            } catch (closeEx: Exception) {
                logger.w("Failed to close ImageProxy: ${closeEx.message}", closeEx)
            }
        }
    }
}

/**

 * Saves a bitmap to external storage (Downloads/pill_debug/)

 * for debugging orientation and preprocessing results.

 */

/*fun Bitmap.saveDebugCopy(tag: String = "frame"): File? {

    return try {

        val dir = File(

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),

            "pill_debug"

        ).apply { mkdirs() }

        val name = "${tag}_${SimpleDateFormat("HHmmss", Locale.US).format(Date())}.jpg"

        val file = File(dir, name)

        FileOutputStream(file).use { out ->

            compress(Bitmap.CompressFormat.JPEG, 90, out)

        }

        file

    } catch (e: Exception) {

        e.printStackTrace()

        null

    }

}*/

