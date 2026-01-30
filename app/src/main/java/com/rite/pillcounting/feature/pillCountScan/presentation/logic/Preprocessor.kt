package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.*
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import com.rite.pillcounting.core.utils.logger.AppLogger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

object Preprocessor {

    const val INPUT_SIZE = 640
    private val logger = AppLogger.create<Preprocessor>()

    data class LetterboxInfo(
        val scale: Float,
        val padX: Float,
        val padY: Float,
        val srcWidth: Int,
        val srcHeight: Int
    )

    /**
     * PREPROCESS PIPELINE (LETTERBOX)
     *
     * Camera frame (any size)
     *   → rotate
     *   → scale to fit inside 640×640
     *   → pad remaining area
     *   → normalize to Float32 RGB
     */
    fun preprocess(imageProxy: ImageProxy): Triple<ByteBuffer, Bitmap, LetterboxInfo> {

        val srcBitmap = imageProxyToGrayscaleBitmap(imageProxy)
        val rotated = rotate(srcBitmap, imageProxy.imageInfo.rotationDegrees)

        val srcW = rotated.width.toFloat()
        val srcH = rotated.height.toFloat()

        val scale = min(INPUT_SIZE / srcW, INPUT_SIZE / srcH)
        val scaledW = srcW * scale
        val scaledH = srcH * scale

        val padX = (INPUT_SIZE - scaledW) / 2f
        val padY = (INPUT_SIZE - scaledH) / 2f

        val letterboxed = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(letterboxed)
        canvas.drawColor(Color.BLACK)

        val dst = RectF(
            padX,
            padY,
            padX + scaledW,
            padY + scaledH
        )

        canvas.drawBitmap(rotated, null, dst, null)

        val buffer = bitmapToBuffer(letterboxed)

        val info = LetterboxInfo(
            scale = scale,
            padX = padX,
            padY = padY,
            srcWidth = rotated.width,
            srcHeight = rotated.height
        )

        srcBitmap.recycle()
        if (rotated != srcBitmap) rotated.recycle()

        logger.i(
            "Letterbox preprocess | src=${info.srcWidth}x${info.srcHeight} " +
                    "scale=$scale pad=($padX,$padY)"
        )

        return Triple(buffer, letterboxed, info)
    }

    // ---------------- helpers ----------------

    private fun bitmapToBuffer(bmp: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bmp.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (p in pixels) {
            buffer.putFloat(((p shr 16) and 0xFF) / 255f)
            buffer.putFloat(((p shr 8) and 0xFF) / 255f)
            buffer.putFloat((p and 0xFF) / 255f)
        }
        buffer.rewind()
        return buffer
    }

    private fun rotate(src: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return src
        val m = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    private fun imageProxyToGrayscaleBitmap(image: ImageProxy): Bitmap {
        val width = image.width
        val height = image.height
        val yBuffer = image.planes[0].buffer
        val yRowStride = image.planes[0].rowStride

        val bmp = createBitmap(width, height)
        val pixels = IntArray(width * height)

        var index = 0
        for (row in 0 until height) {
            yBuffer.position(row * yRowStride)
            for (col in 0 until width) {
                val y = yBuffer.get().toInt() and 0xFF
                pixels[index++] = Color.rgb(y, y, y)
            }
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height)
        return bmp
    }
}
