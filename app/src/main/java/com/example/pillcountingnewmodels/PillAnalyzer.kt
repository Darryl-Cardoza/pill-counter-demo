package com.example.pillcountingnewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

@OptIn(ExperimentalGetImage::class)
class PillAnalyzer(
    private val interpreter: Interpreter,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val onPillCountUpdated: (Int, List<Detection>) -> Unit
) {
    // tiny holder to return multiple values in when expression
    private data class Quad(val x: Float, val y: Float, val width: Float, val height: Float)

    companion object {
        private const val TAG = "PillAnalyzer"
        private const val INPUT_SIZE = 640
        private const val CONFIDENCE_THRESHOLD = 0.5f

        // Detection tensor layout
        private const val IDX_CX = 0
        private const val IDX_CY = 1
        private const val IDX_W = 2
        private const val IDX_H = 3
        private const val CLASS_START = 4
        private const val NUM_CLASSES = 2
    }

    data class Detection(
        val boundingBox: RectF?,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun analyze(imageProxy: ImageProxy) {
        try {
            // Preprocess camera frame for model input (handles rotation)
            val input = preprocess(imageProxy)
            logTensor("Input", input)

            // Output tensor shapes
            val out0Shape = interpreter.getOutputTensor(0).shape()
            val out1Shape = interpreter.getOutputTensor(1).shape()
            Log.d(TAG, "out0 shape: ${out0Shape.joinToString()}")
            Log.d(TAG, "out1 shape: ${out1Shape.joinToString()}")

            // Allocate output arrays
            val detC = out0Shape[1]
            val detN = out0Shape[2]
            val detectionTensor = Array(1) { Array(detC) { FloatArray(detN) } }

            val maskH = out1Shape[1]
            val maskW = out1Shape[2]
            val maskC = out1Shape[3]
            val protoTensor = Array(1) { Array(maskH) { Array(maskW) { FloatArray(maskC) } } }

            val outputs: MutableMap<Int, Any> = mutableMapOf(
                0 to detectionTensor as Any,
                1 to protoTensor as Any
            )

            // Run inference
            interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)
            logTensor("Output Detection", detectionTensor)

            // Parse detections (rotation already handled in preprocess)
            val detections = parseCentroids(
                detectionTensor[0],
                imageProxy.width,
                imageProxy.height,
                imageProxy.imageInfo.rotationDegrees,
                viewWidth,
                viewHeight
            )

            onPillCountUpdated(detections.size, detections)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in analyze: ${e.message}", e)
        } finally {
            imageProxy.close()
        }
    }


    // Debugging helper: logs tensor shape & values
    private fun logTensor(tag: String, tensor: Any) {
        when (tensor) {
            is FloatArray -> {
                val preview = tensor.take(10).joinToString(", ")  // Only first 10 values for readability
                Log.d(TAG, "$tag: $preview ... total=${tensor.size}")
            }
            is Array<*> -> {
                if (tensor.isNotEmpty() && tensor[0] is FloatArray) {
                    logTensor(tag, tensor[0] as FloatArray) // Log only first row if nested
                } else {
                    Log.d(TAG, "$tag: [array size=${tensor.size}]")
                }
            }
            is ByteBuffer -> {
                Log.d(TAG, "$tag: ByteBuffer capacity=${tensor.capacity()}")
            }
            else -> Log.d(TAG, "$tag: $tensor")
        }
    }

    // Converts raw model outputs → bounding boxes & centroids in preview coordinates
    private fun parseCentroids(
        det: Array<FloatArray>,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int,
        viewWidth: Int,
        viewHeight: Int
    ): List<Detection> {
        // Extract model outputs for center-x, center-y, width, height
        val cxArr = det[IDX_CX]
        val cyArr = det[IDX_CY]
        val wArr = det[IDX_W]
        val hArr = det[IDX_H]

        val raw = ArrayList<Detection>()
        val imgW = imageWidth.toFloat()
        val imgH = imageHeight.toFloat()

        for (i in cxArr.indices) {
            // Find best class score for current detection
            var bestScore = Float.NEGATIVE_INFINITY
            for (ci in 0 until NUM_CLASSES) {
                val score = det[CLASS_START + ci][i]
                if (score > bestScore) bestScore = score
            }

            // Skip low-confidence detections
            if (bestScore < CONFIDENCE_THRESHOLD) continue

            // Convert normalized coordinates → original image pixels
            val origX = cxArr[i] * imgW
            val origY = cyArr[i] * imgH
            val boxW = wArr[i] * imgW
            val boxH = hArr[i] * imgH

            // Rotate bounding box center according to device orientation
            val (rotX, rotY, rotW, rotH) = when ((rotationDegrees % 360 + 360) % 360) {
                0 -> Quad(origX, origY, imgW, imgH)
                90 -> Quad(origY, imgW - origX, imgH, imgW)    // Portrait rotation fix
                180 -> Quad(imgW - origX, imgH - origY, imgW, imgH)
                270 -> Quad(imgH - origY, origX, imgH, imgW)   // Portrait rotation fix
                else -> Quad(origX, origY, imgW, imgH)
            }

            // Scale to match PreviewView size (center-cropped)
            val scale = max(viewWidth / rotW, viewHeight / rotH)
            val dispW = rotW * scale
            val dispH = rotH * scale

            // Calculate offsets for center-cropping
            val offsetX = (dispW - viewWidth) / 2f
            val offsetY = (dispH - viewHeight) / 2f

            // Map center coordinates to preview space
            val px = (rotX * scale) - offsetX
            val py = (rotY * scale) - offsetY

            // Map bounding box to preview space
            val boxWPreview = boxW * scale
            val boxHPreview = boxH * scale
            val left = (px - boxWPreview / 2f).coerceAtLeast(0f)
            val top = (py - boxHPreview / 2f).coerceAtLeast(0f)
            val right = (px + boxWPreview / 2f).coerceAtMost(viewWidth.toFloat())
            val bottom = (py + boxHPreview / 2f).coerceAtMost(viewHeight.toFloat())

            // Calculate centroid in preview space after clamping
            val centerX = ((left + right) / 2f).coerceIn(0f, viewWidth.toFloat())
            val centerY = ((top + bottom) / 2f).coerceIn(0f, viewHeight.toFloat())

            // Save detection result
            raw.add(Detection(RectF(left, top, right, bottom), bestScore, centerX, centerY))
        }

        // Apply Non-Maximum Suppression to remove overlapping boxes
        return nonMaxSuppression(raw, 0.5f)
    }

    // Non-Maximum Suppression to filter overlapping detections
    private fun nonMaxSuppression(detections: List<Detection>, iouThreshold: Float): List<Detection> {
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val result = mutableListOf<Detection>()

        while (sorted.isNotEmpty()) {
            val highest = sorted.removeAt(0) // Take highest confidence detection
            result.add(highest)
            // Remove overlapping detections above IoU threshold
            sorted.removeAll { iou(highest.boundingBox, it.boundingBox) > iouThreshold }
        }
        return result
    }

    // Intersection over Union (IoU) calculation for NMS
    private fun iou(a: RectF?, b: RectF?): Float {
        if (a == null || b == null) return 0f
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)

        // Compute intersection and union areas
        val interArea = maxOf(0f, right - left) * maxOf(0f, bottom - top)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)

        // Return IoU ratio
        return if (areaA + areaB - interArea == 0f) 0f else interArea / (areaA + areaB - interArea)
    }


    private fun preprocess(imageProxy: ImageProxy): ByteBuffer {
        // Convert ImageProxy → Bitmap
        val bitmap = imageProxyToBitmap(imageProxy)

        // Rotate if portrait so width > height before scaling
        val rotatedBitmap = if (imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270) {
            val matrix = Matrix().apply { postRotate(imageProxy.imageInfo.rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else bitmap

        // Resize to square 640x640 for model input
        val resized = Bitmap.createScaledBitmap(rotatedBitmap, INPUT_SIZE, INPUT_SIZE, true)

        // Convert bitmap → ByteBuffer for TFLite
        val buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        var p = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val v = pixels[p++]
                buffer.putFloat(((v shr 16) and 0xFF) / 255f) // R
                buffer.putFloat(((v shr 8) and 0xFF) / 255f)  // G
                buffer.putFloat((v and 0xFF) / 255f)          // B
            }
        }

        // Recycle bitmaps to free memory
        if (rotatedBitmap != bitmap) bitmap.recycle()
        resized.recycle()
        rotatedBitmap.recycle()

        buffer.rewind()
        return buffer
    }


    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.buffer.get(nv21, 0, ySize)
        vPlane.buffer.get(nv21, ySize, vSize)
        uPlane.buffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    }
}

//@OptIn(ExperimentalGetImage::class)
//class PillAnalyzer(
//    private val interpreter: Interpreter,
//    private val viewWidth: Int,
//    private val viewHeight: Int,
//    private val onPillCountUpdated: (Int, List<PillAnalyzer.Detection>) -> Unit
//) : ImageAnalysis.Analyzer {
//
//    companion object {
//        private const val TAG = "PillAnalyzer"
//
//        // --- Model Configuration ---
//        private const val INPUT_SIZE = 640
//        private const val OUTPUT_DETECTIONS = 300 // Max number of detections from the model
//        private const val OUTPUT_FEATURES = 6   // [center_x, center_y, w, h, confidence, class_id]
//
//        // --- Post-processing Thresholds ---
//        private const val CONFIDENCE_THRESHOLD = 0.4f // Minimum confidence to consider a detection
//        //If you notice pills being missed, you can lower it to 0.3–0.4.
//        //If you notice false positives, you can raise it to 0.6–0.7.
//
//        private const val IOU_THRESHOLD = 0.3f       // IoU threshold for NMS
//        //Lower value (e.g., 0.3) → more aggressive suppression → may merge nearby pills.
//        //Higher value (e.g., 0.6) → less suppression → might keep overlapping boxes.
//
//        // --- Performance ---
//        private const val THROTTLE_MS = 100L
//
//        // --- Output Array Indices (for clarity) ---
//        private const val BBOX_X_INDEX = 0
//        private const val BBOX_Y_INDEX = 1
//        private const val BBOX_W_INDEX = 2
//        private const val BBOX_H_INDEX = 3
//        private const val CONFIDENCE_INDEX = 4
//        // const val CLASS_ID_INDEX = 5 // If you need to use the class ID
//    }
//
//    /**
//     * A helper data class to hold detection results in a structured way.
//     * @property boundingBox The bounding box of the detected object.
//     * @property confidence The model's confidence score for this detection.
//     * @property pixelX The x-coordinate of the centroid in pixel units.
//     * @property pixelY The y-coordinate of the centroid in pixel units.
//     */
//    data class Detection(
//        val boundingBox: RectF,
//        val confidence: Float,
//        val pixelX: Float,
//        val pixelY: Float
//    )
//
//    private var lastAnalyzedTime = 0L
//
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    override fun analyze(imageProxy: ImageProxy) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastAnalyzedTime < THROTTLE_MS) {
//            imageProxy.close()
//            return
//        }
//        lastAnalyzedTime = currentTime
//
//        try {
//            // 1. Convert ImageProxy to Bitmap
//            val bitmap = imageProxy.toBitmap() ?: run {
//                Log.w(TAG, "❌ ImageProxy to Bitmap conversion failed.")
//                return
//            }
//
//            // 2. Preprocess the image for the model
//            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
//            val inputBuffer = preprocess(resizedBitmap)
//            resizedBitmap.recycle()
//
//            // 3. Run inference
//            val output = Array(1) { Array(OUTPUT_DETECTIONS) { FloatArray(OUTPUT_FEATURES) } }
//
//            val start = System.currentTimeMillis()
//            Log.i(TAG, "📥 Starting inference at ${getFormattedTime()}")
//            interpreter.run(inputBuffer, output)
//            val duration = System.currentTimeMillis() - start
//            Log.i(TAG, "📤 Inference completed in ${duration}ms at ${getFormattedTime()}")
//
//            // 4. Post-process the results
//            val rawOutputArray = output[0]
//
//            // Log the raw output from the model for debugging
//            logRawOutput(rawOutputArray)
//
//            // Pass the view dimensions to the parsing function
//            val allDetections = parseOutput(rawOutputArray)
//            val finalDetections = nonMaxSuppression(allDetections)
//            val count = finalDetections.size
//
//            Log.i(TAG, "✅ Detection complete: Found ${allDetections.size} candidates, filtered to $count pills after NMS.")
//
//            // Pass the count AND the list of final detections
//            onPillCountUpdated(count, finalDetections)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Exception in analyzer: ${e.message}", e)
//        } finally {
//            imageProxy.close()
//        }
//    }
//
//    /**
//     * Logs the raw output from the TFLite model. This is useful for debugging.
//     */
//    private fun logRawOutput(rawOutput: Array<FloatArray>) {
//        if (rawOutput.isEmpty()) {
//            Log.d(TAG, "Debug: Raw output array is empty.")
//            return
//        }
//        val sb = StringBuilder("Debug: Raw Model Output:\n")
//        rawOutput.forEachIndexed { index, detection ->
//            if (detection[CONFIDENCE_INDEX] > 0.1f) {
//                sb.append("Box $index: ")
//                detection.forEach { value ->
//                    sb.append(String.format("%.2f ", value))
//                }
//                sb.append("\n")
//            }
//        }
//        Log.d(TAG, sb.toString())
//    }
//
//    /**
//     * Preprocesses the input bitmap to the format required by the TFLite model.
//     */
//    private fun preprocess(bitmap: Bitmap): ByteBuffer {
//        val byteBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
//        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//
//        var pixel = 0
//        for (i in 0 until INPUT_SIZE) {
//            for (j in 0 until INPUT_SIZE) {
//                val value = intValues[pixel++]
//
//                val r = (value shr 16) and 0xFF
//                val g = (value shr 8) and 0xFF
//                val b = value and 0xFF
//
//                byteBuffer.putFloat(r / 255.0f)
//                byteBuffer.putFloat(g / 255.0f)
//                byteBuffer.putFloat(b / 255.0f)
//            }
//        }
//        return byteBuffer
//    }
//
//    /**
//     * Parses the raw model output into a list of [Detection] objects.
//     */
//    private fun parseOutput(rawOutput: Array<FloatArray>): List<Detection> {
//        val detections = mutableListOf<Detection>()
//        for (detectionData in rawOutput) {
//            val confidence = detectionData[CONFIDENCE_INDEX]
//            if (confidence >= CONFIDENCE_THRESHOLD) {
//                val centerX = detectionData[BBOX_X_INDEX]
//                val centerY = detectionData[BBOX_Y_INDEX]
//                val width = detectionData[BBOX_W_INDEX]
//                val height = detectionData[BBOX_H_INDEX]
//
//                // Convert normalized coordinates to pixel coordinates for plotting
//                val pixelX = centerX * viewWidth
//                val pixelY = centerY * viewHeight
//
//                val left = centerX - width / 2
//                val top = centerY - height / 2
//                val right = centerX + width / 2
//                val bottom = centerY + height / 2
//
//                detections.add(
//                    Detection(
//                        boundingBox = RectF(left, top, right, bottom),
//                        confidence = confidence,
//                        pixelX = pixelX,
//                        pixelY = pixelY
//                    )
//                )
//            }
//        }
//        return detections
//    }
//
//    /**
//     * Applies Non-Maximum Suppression to filter out overlapping bounding boxes.
//     */
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    private fun nonMaxSuppression(detections: List<Detection>): List<Detection> {
//        if (detections.isEmpty()) return emptyList()
//
//        val sortedDetections = detections.sortedByDescending { it.confidence }
//        val finalDetections = mutableListOf<Detection>()
//        val remainingDetections = sortedDetections.toMutableList()
//
//        while (remainingDetections.isNotEmpty()) {
//            val bestDetection = remainingDetections.first()
//            finalDetections.add(bestDetection)
//            remainingDetections.removeFirst()
//
//            val iterator = remainingDetections.iterator()
//            while (iterator.hasNext()) {
//                val nextDetection = iterator.next()
//                if (calculateIoU(bestDetection.boundingBox, nextDetection.boundingBox) > IOU_THRESHOLD) {
//                    iterator.remove()
//                }
//            }
//        }
//        return finalDetections
//    }
//
//    /**
//     * Calculates the Intersection over Union (IoU) of two bounding boxes.
//     */
//    private fun calculateIoU(boxA: RectF, boxB: RectF): Float {
//        val xA = max(boxA.left, boxB.left)
//        val yA = max(boxA.top, boxB.top)
//        val xB = min(boxA.right, boxB.right)
//        val yB = min(boxA.bottom, boxB.bottom)
//
//        val intersectionArea = max(0f, xB - xA) * max(0f, yB - yA)
//        val boxAArea = (boxA.right - boxA.left) * (boxA.bottom - boxA.top)
//        val boxBArea = (boxB.right - boxB.left) * (boxB.bottom - boxB.top)
//        val unionArea = boxAArea + boxBArea - intersectionArea
//
//        return if (unionArea > 0) intersectionArea / unionArea else 0f
//    }
//
//    private fun getFormattedTime(): String {
//        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
//    }
//}