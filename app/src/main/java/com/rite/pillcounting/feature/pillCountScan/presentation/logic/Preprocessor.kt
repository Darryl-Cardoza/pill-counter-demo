package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.feature.pillCountScan.presentation.logic.Preprocessor.INPUT_SIZE
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

/**
 * Handles preprocessing of camera frames before TensorFlow Lite inference.
 *
 * Responsibilities:
 * - Convert CameraX [ImageProxy] frames (YUV_420_888) to [Bitmap].
 * - Apply rotation and resizing to fit model input requirements.
 * - Normalize pixel data into [ByteBuffer] for TFLite model input.
 *
 * Provides detailed logs for performance profiling and debugging.
 */
object Preprocessor {

    private const val INPUT_SIZE = 640
    private const val MAX_DIMENSION = 1920
    private val logger = AppLogger.create<Preprocessor>()

    /**
     * Converts a CameraX [ImageProxy] into:
     *  - A normalized [ByteBuffer] suitable for model input.
     *  - A correctly oriented [Bitmap] for visualization or debugging.
     *
     * This function performs the following steps:
     *  1. Convert YUV → Bitmap
     *  2. Apply rotation and safe resizing
     *  3. Center-crop and scale to [INPUT_SIZE]
     *  4. Convert to normalized RGB tensor buffer
     *
     * @param imageProxy The input image frame from CameraX.
     * @return A [Pair] containing (normalized ByteBuffer, rotated Bitmap).
     */
    fun preprocess(imageProxy: ImageProxy): Pair<ByteBuffer, Bitmap> {
        val startTime = System.currentTimeMillis()
        logger.i(
            "Preprocessing started | Frame=${imageProxy.width}x${imageProxy.height}, " +
                    "Rotation=${imageProxy.imageInfo.rotationDegrees}°"
        )

        // Step 1: Convert YUV to Bitmap
        val convertStart = System.currentTimeMillis()
        val bitmap = imageProxyToBitmap(imageProxy)
        logger.d(
            "YUV → Bitmap conversion completed in ${System.currentTimeMillis() - convertStart} ms | " +
                    "Bitmap=${bitmap.width}x${bitmap.height}"
        )

        // Step 2: Rotate and resize safely
        val rotateStart = System.currentTimeMillis()
        val rotation = when (imageProxy.imageInfo.rotationDegrees) {
            0, 90, 180, 270 -> imageProxy.imageInfo.rotationDegrees
            else -> 0
        }
        val rotatedBitmap = rotateAndResizeSafely(bitmap, rotation)
        logger.d(
            "Rotation and resize completed in ${System.currentTimeMillis() - rotateStart} ms | " +
                    "Final=${rotatedBitmap.width}x${rotatedBitmap.height}"
        )

        // Step 3: Center-crop to model input size (640x640)
        val cropStart = System.currentTimeMillis()
        val cropped = centerCropAndResize(src = rotatedBitmap)
        logger.d(
            "Center-crop resize completed in ${System.currentTimeMillis() - cropStart} ms | " +
                    "Size=${cropped.width}x${cropped.height}"
        )

        // Step 4: Convert bitmap to model input buffer
        val bufferStart = System.currentTimeMillis()
        val buffer = convertToByteBuffer(cropped)
        buffer.rewind()
        logger.d(
            "Bitmap → ByteBuffer conversion completed in ${System.currentTimeMillis() - bufferStart} ms | " +
                    "Buffer size=${buffer.capacity()} bytes"
        )

        // Cleanup intermediate bitmaps
        if (rotatedBitmap != bitmap) bitmap.recycle()
        cropped.recycle()

        val totalTime = System.currentTimeMillis() - startTime
        logger.i(
            "Preprocessing completed in $totalTime ms | Output bitmap=${rotatedBitmap.width}x${rotatedBitmap.height}"
        )

        return Pair(buffer, rotatedBitmap)
    }

    /**
     * Rotates and safely resizes an image while preventing OutOfMemory errors.
     *
     * @param original The original [Bitmap].
     * @param rotation Rotation in degrees to be applied (0, 90, 180, or 270).
     * @return A rotated and optionally downscaled [Bitmap].
     */
    private fun rotateAndResizeSafely(original: Bitmap, rotation: Int): Bitmap {
        var temp = original
        val maxSide = max(original.width, original.height)

        // Downscale large images to prevent memory overflow
        if (maxSide > MAX_DIMENSION) {
            val scale = MAX_DIMENSION.toFloat() / maxSide
            val newWidth = (original.width * scale).toInt()
            val newHeight = (original.height * scale).toInt()
            temp = original.scale(newWidth, newHeight)
            logger.w("Image downscaled to ${newWidth}x${newHeight} (Max dimension=$MAX_DIMENSION px)")
        }

        // Apply rotation if required
        return if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(temp, 0, 0, temp.width, temp.height, matrix, true)
            if (temp != original) temp.recycle()
            logger.i("Bitmap rotated by $rotation° | New size=${rotated.width}x${rotated.height}")
            rotated
        } else temp
    }

    /**
     * Performs aspect-ratio–preserving center crop and resize to target dimensions.
     *
     * @param src The source [Bitmap].
     * @param targetWidth The target width in pixels.
     * @param targetHeight The target height in pixels.
     * @return A center-cropped, scaled [Bitmap].
     */
    private fun centerCropAndResize(
        src: Bitmap,
        targetWidth: Int = INPUT_SIZE,
        targetHeight: Int = INPUT_SIZE
    ): Bitmap {
        val srcWidth = src.width.toFloat()
        val srcHeight = src.height.toFloat()

        val scale = max(targetWidth / srcWidth, targetHeight / srcHeight)
        val scaledWidth = (srcWidth * scale).toInt()
        val scaledHeight = (srcHeight * scale).toInt()

        val scaledBitmap = src.scale(scaledWidth, scaledHeight)
        val xOffset = (scaledWidth - targetWidth) / 2
        val yOffset = (scaledHeight - targetHeight) / 2

        val cropped = Bitmap.createBitmap(scaledBitmap, xOffset, yOffset, targetWidth, targetHeight)
        if (scaledBitmap != src) scaledBitmap.recycle()

        return cropped
    }

    /**
     * Converts a [Bitmap] into a normalized RGB tensor buffer for model inference.
     *
     * @param bmp The [Bitmap] to convert.
     * @return A [ByteBuffer] containing normalized RGB values (float32, 0–1 range).
     */
    private fun convertToByteBuffer(bmp: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bmp.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        var index = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val value = pixels[index++]
                buffer.putFloat(((value shr 16) and 0xFF) / 255f) // R
                buffer.putFloat(((value shr 8) and 0xFF) / 255f)  // G
                buffer.putFloat((value and 0xFF) / 255f)          // B
            }
        }

        val approxMB = buffer.capacity() / (1024f * 1024f)
        logger.d("ByteBuffer filled | Size=${"%.2f".format(approxMB)} MB")
        return buffer
    }

    /**
     * Converts a CameraX [ImageProxy] in YUV_420_888 format into a correctly
     * oriented RGB [Bitmap]. This implementation accounts for row and pixel
     * stride variations, avoiding distortion in portrait or landscape frames.
     *
     * @param image The CameraX [ImageProxy] frame.
     * @return The converted [Bitmap].
     */
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val start = System.currentTimeMillis()

        val width = image.width
        val height = image.height
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val yRowStride = image.planes[0].rowStride
        val uvRowStride = image.planes[1].rowStride
        val uvPixelStride = image.planes[1].pixelStride

        val nv21 = ByteArray(width * height * 3 / 2)
        var pos = 0

        // Copy Y plane respecting row stride
        for (row in 0 until height) {
            yBuffer.position(row * yRowStride)
            yBuffer.get(nv21, pos, width)
            pos += width
        }

        // Copy interleaved UV data (VU order)
        val chromaHeight = height / 2
        val chromaWidth = width / 2
        val vPos = vBuffer.position()
        val uPos = uBuffer.position()

        for (row in 0 until chromaHeight) {
            val vRowStart = vPos + row * uvRowStride
            val uRowStart = uPos + row * uvRowStride
            for (col in 0 until chromaWidth) {
                nv21[pos++] = vBuffer.get(vRowStart + col * uvPixelStride)
                nv21[pos++] = uBuffer.get(uRowStart + col * uvPixelStride)
            }
        }

        // Convert NV21 → JPEG → Bitmap
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 95, out)
        val jpegBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

        image.close()

        val duration = System.currentTimeMillis() - start
        logger.d("YUV → Bitmap conversion completed in $duration ms | Frame=${width}x${height}")

        return bitmap
    }
}
