package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

/**
 * Performs pill detection on camera frames using a TensorFlow Lite model.
 *
 * Responsibilities:
 * 1. Converts [ImageProxy] frames to [Bitmap] suitable for model input.
 * 2. Preprocesses the image into a normalized [ByteBuffer].
 * 3. Runs inference using the provided [Interpreter].
 * 4. Parses model outputs to extract pill centroids.
 * 5. Transforms model coordinates to screen coordinates (accounting for rotation, aspect ratio, and PreviewView scaling).
 * 6. Applies Non-Maximum Suppression (NMS) to remove duplicate detections.
 * 7. Returns the pill count, list of detections, and a thumbnail [Bitmap] via [onPillCountUpdated] callback.
 *
 * @param interpreter TensorFlow Lite interpreter for the pill detection model.
 * @param viewWidth Width of the PreviewView on screen (pixels).
 * @param viewHeight Height of the PreviewView on screen (pixels).
 * @param onPillCountUpdated Callback invoked after analysis:
 *   - **Int**: Number of detected pills.
 *   - **List<Detection>**: List of detected pill centroids.
 *   - **Bitmap**: Thumbnail of the analyzed frame for UI purposes.
 */
class PillAnalyzer(
    private val interpreter: Interpreter,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val onPillCountUpdated: (Int, List<Detection>, Bitmap) -> Unit
) {
    // Tiny holder to return multiple values in when expression
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

    /**
     * Represents a single detected pill.
     *
     * @param boundingBox Bounding box around the pill in screen coordinates (nullable for centroid-only visualization).
     * @param confidence Confidence score for this detection (0f..1f).
     * @param pixelX X-coordinate of the pill centroid on the PreviewView.
     * @param pixelY Y-coordinate of the pill centroid on the PreviewView.
     */
    data class Detection(
        val boundingBox: RectF?,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    /**
     * Performs analysis on the given camera frame ([ImageProxy]).
     *
     * Flow:
     * 1. Preprocess frame → ByteBuffer + thumbnail bitmap.
     * 2. Run TFLite inference.
     * 3. Parse centroids from model output.
     * 4. Apply Non-Maximum Suppression.
     * 5. Invoke [onPillCountUpdated] with results.
     *
     * @param imageProxy Frame from CameraX.
     */
    fun analyze(imageProxy: ImageProxy) {
        var thumbnailBitmap: Bitmap? = null
        try {
            Log.d(TAG, "--- Start Analysis ---")
            Log.d(TAG, "ImageProxy Dims: ${imageProxy.width}x${imageProxy.height}, Rotation: ${imageProxy.imageInfo.rotationDegrees}")
            // Preprocess returns the input buffer and the thumbnail bitmap
            val (input, bmp) = preprocess(imageProxy)
            thumbnailBitmap = bmp

            val out0Shape = interpreter.getOutputTensor(0).shape()
            val detC = out0Shape[1]
            val detN = out0Shape[2]
            val detectionTensor = Array(1) { Array(detC) { FloatArray(detN) } }

            val out1Shape = interpreter.getOutputTensor(1).shape()
            val maskH = out1Shape[1]
            val maskW = out1Shape[2]
            val maskC = out1Shape[3]
            val protoTensor = Array(1) { Array(maskH) { Array(maskW) { FloatArray(maskC) } } }

            val outputs: MutableMap<Int, Any> = mutableMapOf(
                0 to detectionTensor as Any,
                1 to protoTensor as Any
            )

            interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)

            val detections = parseCentroids(
                det = detectionTensor[0],
                imageWidth = imageProxy.width,
                imageHeight = imageProxy.height,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                viewWidth = viewWidth,
                viewHeight = viewHeight
            )

            // Send results and bitmap back to the ViewModel
            onPillCountUpdated(detections.size, detections, thumbnailBitmap)

        } catch (e: Exception) {
            // If an error occurs, recycle the bitmap if it was created
            thumbnailBitmap?.recycle()
            Log.e(TAG, "❌ Exception in analyze: ${e.message}", e)
        } finally {
            imageProxy.close()
        }
    }

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

    /**
     * Converts normalized model outputs to [Detection] objects in screen coordinates.
     *
     * Handles:
     * - Model output scaling (640x640 → original frame dimensions).
     * - PreviewView scaling and centering (CENTER_CROP).
     * - Filtering low-confidence detections.
     * - Bounding box creation around centroids.
     * - Non-Maximum Suppression to remove duplicates.
     */
    private fun parseCentroids(
        det: Array<FloatArray>,
        imageWidth: Int,      // Original camera frame width (before preprocess rotation)
        imageHeight: Int,     // Original camera frame height (before preprocess rotation)
        rotationDegrees: Int, // Rotation reported by ImageProxy
        viewWidth: Int,       // Width of the PreviewView on screen
        viewHeight: Int       // Height of the PreviewView on screen
    ): List<PillAnalyzer.Detection> {
        Log.d(TAG, "Parse Centroids: Start")
        Log.d(TAG, "Parse Centroids: Image Dims: ${imageWidth}x${imageHeight}, Rotation: ${rotationDegrees}")
        Log.d(TAG, "Parse Centroids: View Dims: ${viewWidth}x${viewHeight}")

        val cxArr = det[IDX_CX] // X-coordinates of centroids (normalized [0,1])
        val cyArr = det[IDX_CY] // Y-coordinates of centroids (normalized [0,1])
        val rawDetections = mutableListOf<PillAnalyzer.Detection>()

        // --- Step 1: Calculate frame dimensions after applying camera rotation ---
        val rotation = (rotationDegrees % 360 + 360) % 360 // Normalize rotation to 0, 90, 180, 270
        val rotatedFrameWidth: Float
        val rotatedFrameHeight: Float
        if (rotation == 90 || rotation == 270) {
            // Width and height swap if rotated by 90° or 270°
            rotatedFrameWidth = imageHeight.toFloat()
            rotatedFrameHeight = imageWidth.toFloat()
        } else {
            rotatedFrameWidth = imageWidth.toFloat()
            rotatedFrameHeight = imageHeight.toFloat()
        }
        Log.d(TAG, "Parse Centroids: Calculated Rotated Frame Dims: ${rotatedFrameWidth}x${rotatedFrameHeight}")

        // --- Step 2: Build a transformation matrix ---
        // This matrix converts model-space coordinates → screen-space coordinates.
        val transformationMatrix = Matrix()

        // Step A: Undo INPUT_SIZE squashing
        // The model always receives a 640x640 square. Undo this by scaling back
        // to the original rotated frame dimensions (restoring aspect ratio).
        transformationMatrix.postScale(
            rotatedFrameWidth / INPUT_SIZE.toFloat(),
            rotatedFrameHeight / INPUT_SIZE.toFloat()
        )

        // Step B: Scale the corrected frame to fill the PreviewView
        // Keep aspect ratio while making sure frame fully covers the view (like CENTER_CROP).
        val viewToFrameScale = max(
            viewWidth / rotatedFrameWidth,
            viewHeight / rotatedFrameHeight
        )
        transformationMatrix.postScale(viewToFrameScale, viewToFrameScale)

        // Step C: Translate the scaled frame so it is centered in the PreviewView
        val scaledImageWidth = rotatedFrameWidth * viewToFrameScale
        val scaledImageHeight = rotatedFrameHeight * viewToFrameScale
        val dx = (viewWidth - scaledImageWidth) / 2f
        val dy = (viewHeight - scaledImageHeight) / 2f
        Log.d(TAG, "Parse Centroids: Scale: ${viewToFrameScale}, Offsets: dx=${dx}, dy=${dy}")
        transformationMatrix.postTranslate(dx, dy)

        // --- Step 3: Apply transformation to each detection ---
        val modelPoint = floatArrayOf(0f, 0f) // Reusable buffer for a single (x,y)

        for (i in cxArr.indices) {
            // 3A: Find best confidence score across all classes for this detection
            var bestScore = Float.NEGATIVE_INFINITY
            for (ci in 0 until NUM_CLASSES) {
                val score = det[CLASS_START + ci][i]
                if (score > bestScore) bestScore = score
            }
            if (bestScore < CONFIDENCE_THRESHOLD) continue // Skip low confidence detections

            // 3B: Convert normalized model coords → model-space pixels (640x640)
            modelPoint[0] = cxArr[i] * INPUT_SIZE
            modelPoint[1] = cyArr[i] * INPUT_SIZE

            // 3C: Apply transformation matrix → screen coordinates (px, py)
            transformationMatrix.mapPoints(modelPoint)
            val px = modelPoint[0]
            val py = modelPoint[1]

            // 3D: Create a small bounding box around the centroid (for visualization)
            val boxSize = 20f
            val detectionBox = RectF(
                px - boxSize / 2f,
                py - boxSize / 2f,
                px + boxSize / 2f,
                py + boxSize / 2f
            )

            // 3E: Add detection, clamping coordinates within view bounds
            rawDetections.add(
                PillAnalyzer.Detection(
                    boundingBox = detectionBox,
                    confidence = bestScore,
                    pixelX = px.coerceIn(0f, viewWidth.toFloat()),
                    pixelY = py.coerceIn(0f, viewHeight.toFloat())
                )
            )
        }

        // --- Step 4: Apply Non-Maximum Suppression to remove duplicates ---
        return nonMaxSuppression(rawDetections, iouThreshold = 0.5f)
    }
    private fun nonMaxSuppression(detections: List<Detection>, iouThreshold: Float): List<Detection> {
        // Step 1: Sort all detections by confidence (highest first)
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val result = mutableListOf<Detection>()

        // Step 2: Process until no detections are left
        while (sorted.isNotEmpty()) {
            // Pick the detection with the highest confidence
            val highest = sorted.removeAt(0)
            result.add(highest)

            // Step 3: Remove all remaining boxes that overlap too much with it
            // (IoU > threshold means they likely represent the same object)
            sorted.removeAll { iou(highest.boundingBox, it.boundingBox) > iouThreshold }
        }

        // Step 4: Return the final list of non-overlapping detections
        return result
    }
    private fun iou(a: RectF?, b: RectF?): Float {
        // If either rectangle is null, no overlap possible
        if (a == null || b == null) return 0f

        // Step 1: Find coordinates of the overlapping rectangle
        val left = maxOf(a.left, b.left)       // Left boundary of overlap
        val top = maxOf(a.top, b.top)          // Top boundary of overlap
        val right = minOf(a.right, b.right)    // Right boundary of overlap
        val bottom = minOf(a.bottom, b.bottom) // Bottom boundary of overlap

        // Step 2: Compute intersection area
        // If boxes don't overlap, result will be clamped to 0
        val interArea = maxOf(0f, right - left) * maxOf(0f, bottom - top)

        // Step 3: Compute area of each rectangle
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)

        // Step 4: IoU formula: Intersection / Union
        // Union = areaA + areaB - intersection
        return if (areaA + areaB - interArea == 0f) {
            0f // Prevent division by zero
        } else {
            interArea / (areaA + areaB - interArea)
        }
    }


    /**
     * Preprocesses the input camera frame (ImageProxy) into a normalized ByteBuffer
     * suitable for TensorFlow Lite model inference.
     * returns a Pair of the ByteBuffer and the created Bitmap.
     */
    private fun preprocess(imageProxy: ImageProxy): Pair<ByteBuffer, Bitmap> {
        val bitmap = imageProxyToBitmap(imageProxy)
        Log.d(TAG, "Preprocess: Original Converted Bitmap Dims: ${bitmap.width}x${bitmap.height}")

        val rotatedBitmap = if (imageProxy.imageInfo.rotationDegrees == 90 ||
            imageProxy.imageInfo.rotationDegrees == 270
        ) {
            val matrix = Matrix().apply { postRotate(imageProxy.imageInfo.rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else bitmap
        Log.d(TAG, "Preprocess: Rotated Bitmap Dims: ${rotatedBitmap.width}x${rotatedBitmap.height}")

        val resized = Bitmap.createScaledBitmap(rotatedBitmap, INPUT_SIZE, INPUT_SIZE, true)
        Log.d(TAG, "Preprocess: Resized for model Dims: ${resized.width}x${resized.height}")

        val buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        var p = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val v = pixels[p++]
                buffer.putFloat(((v shr 16) and 0xFF) / 255f)
                buffer.putFloat(((v shr 8) and 0xFF) / 255f)
                buffer.putFloat((v and 0xFF) / 255f)
            }
        }

        if (rotatedBitmap != bitmap) bitmap.recycle()
        resized.recycle()

        buffer.rewind()

        // Returns both the buffer and the bitmap
        return Pair(buffer, rotatedBitmap)
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