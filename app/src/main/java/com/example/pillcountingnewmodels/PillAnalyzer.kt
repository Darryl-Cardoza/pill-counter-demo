package com.example.pillcountingnewmodels

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalGetImage::class)
class PillAnalyzer(
    private val interpreter: Interpreter,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val onPillCountUpdated: (Int, List<PillAnalyzer.Detection>) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "PillAnalyzer"

        // --- Model Configuration ---
        private const val INPUT_SIZE = 640
        private const val OUTPUT_DETECTIONS = 300 // Max number of detections from the model
        private const val OUTPUT_FEATURES = 6   // [center_x, center_y, w, h, confidence, class_id]

        // --- Post-processing Thresholds ---
        private const val CONFIDENCE_THRESHOLD = 0.4f // Minimum confidence to consider a detection
        //If you notice pills being missed, you can lower it to 0.3–0.4.
        //If you notice false positives, you can raise it to 0.6–0.7.

        private const val IOU_THRESHOLD = 0.3f       // IoU threshold for NMS
        //Lower value (e.g., 0.3) → more aggressive suppression → may merge nearby pills.
        //Higher value (e.g., 0.6) → less suppression → might keep overlapping boxes.

        // --- Performance ---
        private const val THROTTLE_MS = 100L

        // --- Output Array Indices (for clarity) ---
        private const val BBOX_X_INDEX = 0
        private const val BBOX_Y_INDEX = 1
        private const val BBOX_W_INDEX = 2
        private const val BBOX_H_INDEX = 3
        private const val CONFIDENCE_INDEX = 4
        // const val CLASS_ID_INDEX = 5 // If you need to use the class ID
    }

    /**
     * A helper data class to hold detection results in a structured way.
     * @property boundingBox The bounding box of the detected object.
     * @property confidence The model's confidence score for this detection.
     * @property pixelX The x-coordinate of the centroid in pixel units.
     * @property pixelY The y-coordinate of the centroid in pixel units.
     */
    data class Detection(
        val boundingBox: RectF,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    private var lastAnalyzedTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzedTime < THROTTLE_MS) {
            imageProxy.close()
            return
        }
        lastAnalyzedTime = currentTime

        try {
            // 1. Convert ImageProxy to Bitmap
            val bitmap = imageProxy.toBitmap() ?: run {
                Log.w(TAG, "❌ ImageProxy to Bitmap conversion failed.")
                return
            }

            // 2. Preprocess the image for the model
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            val inputBuffer = preprocess(resizedBitmap)
            resizedBitmap.recycle()

            // 3. Run inference
            val output = Array(1) { Array(OUTPUT_DETECTIONS) { FloatArray(OUTPUT_FEATURES) } }

            val start = System.currentTimeMillis()
            Log.i(TAG, "📥 Starting inference at ${getFormattedTime()}")
            interpreter.run(inputBuffer, output)
            val duration = System.currentTimeMillis() - start
            Log.i(TAG, "📤 Inference completed in ${duration}ms at ${getFormattedTime()}")

            // 4. Post-process the results
            val rawOutputArray = output[0]

            // Log the raw output from the model for debugging
            logRawOutput(rawOutputArray)

            // Pass the view dimensions to the parsing function
            val allDetections = parseOutput(rawOutputArray)
            val finalDetections = nonMaxSuppression(allDetections)
            val count = finalDetections.size

            Log.i(TAG, "✅ Detection complete: Found ${allDetections.size} candidates, filtered to $count pills after NMS.")

            // Pass the count AND the list of final detections
            onPillCountUpdated(count, finalDetections)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in analyzer: ${e.message}", e)
        } finally {
            imageProxy.close()
        }
    }

    /**
     * Logs the raw output from the TFLite model. This is useful for debugging.
     */
    private fun logRawOutput(rawOutput: Array<FloatArray>) {
        if (rawOutput.isEmpty()) {
            Log.d(TAG, "Debug: Raw output array is empty.")
            return
        }
        val sb = StringBuilder("Debug: Raw Model Output:\n")
        rawOutput.forEachIndexed { index, detection ->
            if (detection[CONFIDENCE_INDEX] > 0.1f) {
                sb.append("Box $index: ")
                detection.forEach { value ->
                    sb.append(String.format("%.2f ", value))
                }
                sb.append("\n")
            }
        }
        Log.d(TAG, sb.toString())
    }

    /**
     * Preprocesses the input bitmap to the format required by the TFLite model.
     */
    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]

                val r = (value shr 16) and 0xFF
                val g = (value shr 8) and 0xFF
                val b = value and 0xFF

                byteBuffer.putFloat(r / 255.0f)
                byteBuffer.putFloat(g / 255.0f)
                byteBuffer.putFloat(b / 255.0f)
            }
        }
        return byteBuffer
    }

    /**
     * Parses the raw model output into a list of [Detection] objects.
     */
    private fun parseOutput(rawOutput: Array<FloatArray>): List<Detection> {
        val detections = mutableListOf<Detection>()
        for (detectionData in rawOutput) {
            val confidence = detectionData[CONFIDENCE_INDEX]
            if (confidence >= CONFIDENCE_THRESHOLD) {
                val centerX = detectionData[BBOX_X_INDEX]
                val centerY = detectionData[BBOX_Y_INDEX]
                val width = detectionData[BBOX_W_INDEX]
                val height = detectionData[BBOX_H_INDEX]

                // Convert normalized coordinates to pixel coordinates for plotting
                val pixelX = centerX * viewWidth
                val pixelY = centerY * viewHeight

                val left = centerX - width / 2
                val top = centerY - height / 2
                val right = centerX + width / 2
                val bottom = centerY + height / 2

                detections.add(
                    Detection(
                        boundingBox = RectF(left, top, right, bottom),
                        confidence = confidence,
                        pixelX = pixelX,
                        pixelY = pixelY
                    )
                )
            }
        }
        return detections
    }

    /**
     * Applies Non-Maximum Suppression to filter out overlapping bounding boxes.
     */
    private fun nonMaxSuppression(detections: List<Detection>): List<Detection> {
        if (detections.isEmpty()) return emptyList()

        val sortedDetections = detections.sortedByDescending { it.confidence }
        val finalDetections = mutableListOf<Detection>()
        val remainingDetections = sortedDetections.toMutableList()

        while (remainingDetections.isNotEmpty()) {
            val bestDetection = remainingDetections.first()
            finalDetections.add(bestDetection)
            remainingDetections.removeFirst()

            val iterator = remainingDetections.iterator()
            while (iterator.hasNext()) {
                val nextDetection = iterator.next()
                if (calculateIoU(bestDetection.boundingBox, nextDetection.boundingBox) > IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }
        return finalDetections
    }

    /**
     * Calculates the Intersection over Union (IoU) of two bounding boxes.
     */
    private fun calculateIoU(boxA: RectF, boxB: RectF): Float {
        val xA = max(boxA.left, boxB.left)
        val yA = max(boxA.top, boxB.top)
        val xB = min(boxA.right, boxB.right)
        val yB = min(boxA.bottom, boxB.bottom)

        val intersectionArea = max(0f, xB - xA) * max(0f, yB - yA)
        val boxAArea = (boxA.right - boxA.left) * (boxA.bottom - boxA.top)
        val boxBArea = (boxB.right - boxB.left) * (boxB.bottom - boxB.top)
        val unionArea = boxAArea + boxBArea - intersectionArea

        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }

    private fun getFormattedTime(): String {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
    }
}