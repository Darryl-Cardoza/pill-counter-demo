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
 */
class PillAnalyzer(
    private val interpreter: Interpreter,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val onPillCountUpdated: (
        Int,
        List<Detection>,
        Bitmap,
        Matrix /* expose transform matrix */
    ) -> Unit
) {

    companion object {
        private const val TAG = "PillAnalyzer"
        private const val INPUT_SIZE = 640
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private const val IDX_CX = 0
        private const val IDX_CY = 1
        private const val CLASS_START = 4
        private const val NUM_CLASSES = 2
    }

    /** Expose the latest transformation matrix used for mapping model → screen coordinates */
    private var lastTransformationMatrix: Matrix? = null

    /** Getter so the ViewModel or overlay util can reuse the same transform */
    fun getLastTransformationMatrix(): Matrix? = lastTransformationMatrix?.let { Matrix(it) }

    /** Represents a single detected pill. */
    data class Detection(
        val boundingBox: RectF?,
        val confidence: Float,
        val pixelX: Float,
        val pixelY: Float
    )

    /** Main analysis entrypoint. */
    fun analyze(imageProxy: ImageProxy) {
        var thumbnailBitmap: Bitmap? = null
        try {
            Log.d(TAG, "--- Start Analysis ---")
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

            val (detections, matrix) = parseCentroids(
                det = detectionTensor[0],
                imageWidth = imageProxy.width,
                imageHeight = imageProxy.height,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                viewWidth = viewWidth,
                viewHeight = viewHeight
            )

            // Save the last transformation matrix for later external use (overlay)
            lastTransformationMatrix = matrix

            onPillCountUpdated(detections.size, detections, thumbnailBitmap, matrix)

        } catch (e: Exception) {
            thumbnailBitmap?.recycle()
            Log.e(TAG, "Exception in analyze: ${e.message}", e)
        } finally {
            imageProxy.close()
        }
    }

    /**
     * Converts normalized model outputs to [Detection] objects and returns the list + transformation matrix.
     */
    private fun parseCentroids(
        det: Array<FloatArray>,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int,
        viewWidth: Int,
        viewHeight: Int
    ): Pair<List<Detection>, Matrix> {

        val cxArr = det[IDX_CX]
        val cyArr = det[IDX_CY]
        val rawDetections = mutableListOf<Detection>()

        val rotation = (rotationDegrees % 360 + 360) % 360
        val rotatedFrameWidth: Float
        val rotatedFrameHeight: Float
        if (rotation == 90 || rotation == 270) {
            rotatedFrameWidth = imageHeight.toFloat()
            rotatedFrameHeight = imageWidth.toFloat()
        } else {
            rotatedFrameWidth = imageWidth.toFloat()
            rotatedFrameHeight = imageHeight.toFloat()
        }

        val transformationMatrix = Matrix()

        // Step A: Undo 640x640 squashing
        transformationMatrix.postScale(
            rotatedFrameWidth / INPUT_SIZE.toFloat(),
            rotatedFrameHeight / INPUT_SIZE.toFloat()
        )

        // Step B: Scale to fill PreviewView (CENTER_CROP)
        val viewToFrameScale = max(
            viewWidth / rotatedFrameWidth,
            viewHeight / rotatedFrameHeight
        )
        transformationMatrix.postScale(viewToFrameScale, viewToFrameScale)

        // Step C: Center it
        val scaledImageWidth = rotatedFrameWidth * viewToFrameScale
        val scaledImageHeight = rotatedFrameHeight * viewToFrameScale
        val dx = (viewWidth - scaledImageWidth) / 2f
        val dy = (viewHeight - scaledImageHeight) / 2f
        transformationMatrix.postTranslate(dx, dy)

        val modelPoint = floatArrayOf(0f, 0f)
        for (i in cxArr.indices) {
            var bestScore = Float.NEGATIVE_INFINITY
            for (ci in 0 until NUM_CLASSES) {
                val score = det[CLASS_START + ci][i]
                if (score > bestScore) bestScore = score
            }
            if (bestScore < CONFIDENCE_THRESHOLD) continue

            modelPoint[0] = cxArr[i] * INPUT_SIZE
            modelPoint[1] = cyArr[i] * INPUT_SIZE

            transformationMatrix.mapPoints(modelPoint)
            val px = modelPoint[0]
            val py = modelPoint[1]

            val boxSize = 20f
            val detectionBox = RectF(
                px - boxSize / 2f,
                py - boxSize / 2f,
                px + boxSize / 2f,
                py + boxSize / 2f
            )

            rawDetections.add(
                Detection(
                    boundingBox = detectionBox,
                    confidence = bestScore,
                    pixelX = px.coerceIn(0f, viewWidth.toFloat()),
                    pixelY = py.coerceIn(0f, viewHeight.toFloat())
                )
            )
        }

        val filtered = nonMaxSuppression(rawDetections, 0.5f)
        return Pair(filtered, transformationMatrix)
    }

    /** Simple NMS implementation. */
    private fun nonMaxSuppression(detections: List<Detection>, iouThreshold: Float): List<Detection> {
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val result = mutableListOf<Detection>()
        while (sorted.isNotEmpty()) {
            val highest = sorted.removeAt(0)
            result.add(highest)
            sorted.removeAll { iou(highest.boundingBox, it.boundingBox) > iouThreshold }
        }
        return result
    }

    private fun iou(a: RectF?, b: RectF?): Float {
        if (a == null || b == null) return 0f
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)
        val interArea = maxOf(0f, right - left) * maxOf(0f, bottom - top)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        return if (areaA + areaB - interArea == 0f) 0f else interArea / (areaA + areaB - interArea)
    }

    /** Preprocess camera frame → ByteBuffer + Bitmap. */
    private fun preprocess(imageProxy: ImageProxy): Pair<ByteBuffer, Bitmap> {
        val bitmap = imageProxyToBitmap(imageProxy)
        val rotatedBitmap = if (imageProxy.imageInfo.rotationDegrees == 90 ||
            imageProxy.imageInfo.rotationDegrees == 270
        ) {
            val matrix = Matrix().apply { postRotate(imageProxy.imageInfo.rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else bitmap

        val resized = Bitmap.createScaledBitmap(rotatedBitmap, INPUT_SIZE, INPUT_SIZE, true)
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
