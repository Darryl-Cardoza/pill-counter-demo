package com.rite.pillcounting.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImagePreprocessor {

    private const val INPUT_SIZE = 640

    fun preprocess(
        image: ImageProxy
    ): Pair<ByteBuffer, Bitmap> {

        val bitmap = image.toBitmap()
        val letterboxed = Letterbox.preprocess(bitmap, INPUT_SIZE)

        val buffer = bitmapToFloatBuffer(letterboxed)

        return buffer to letterboxed
    }

    private fun bitmapToFloatBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer =
            ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(
            pixels,
            0,
            INPUT_SIZE,
            0,
            0,
            INPUT_SIZE,
            INPUT_SIZE
        )

        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
            buffer.putFloat((pixel and 0xFF) / 255f)
        }

        buffer.rewind()
        return buffer
    }
}
