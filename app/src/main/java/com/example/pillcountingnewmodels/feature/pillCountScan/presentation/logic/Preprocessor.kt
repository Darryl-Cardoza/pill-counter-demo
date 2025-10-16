package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

object Preprocessor {

    private const val INPUT_SIZE = 640
    private const val MAX_DIMENSION = 1920 // Prevent giant bitmaps (OOM safe)

    fun preprocess(imageProxy: ImageProxy): Pair<ByteBuffer, Bitmap> {
        // Step 1: Convert to bitmap
        val bitmap = imageProxyToBitmap(imageProxy)

        // Step 2: Determine correct orientation
        val rotation = imageProxy.imageInfo.rotationDegrees
        val rotatedBitmap = rotateAndResizeSafely(bitmap, rotation)

        // Step 3: Final model scaling (to 640×640)
        val resized = Bitmap.createScaledBitmap(rotatedBitmap, INPUT_SIZE, INPUT_SIZE, true)
        val buffer = convertToByteBuffer(resized)

        // Cleanup to avoid leaks
        if (rotatedBitmap != bitmap) bitmap.recycle()
        resized.recycle()
        buffer.rewind()

        return Pair(buffer, rotatedBitmap)
    }

    private fun rotateAndResizeSafely(original: Bitmap, rotation: Int): Bitmap {
        // Avoid massive bitmaps from portrait 4K images
        var temp = original
        val maxSide = max(original.width, original.height)
        if (maxSide > MAX_DIMENSION) {
            val scale = MAX_DIMENSION.toFloat() / maxSide
            val newWidth = (original.width * scale).toInt()
            val newHeight = (original.height * scale).toInt()
            temp = Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
        }

        // Apply rotation
        if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(temp, 0, 0, temp.width, temp.height, matrix, true)
            if (temp != original) temp.recycle()
            return rotated
        }
        return temp
    }

    private fun convertToByteBuffer(bmp: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bmp.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        var i = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val v = pixels[i++]
                buffer.putFloat(((v shr 16) and 0xFF) / 255f) // R
                buffer.putFloat(((v shr 8) and 0xFF) / 255f)  // G
                buffer.putFloat((v and 0xFF) / 255f)          // B
            }
        }
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
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 95, out)
        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    }
}
