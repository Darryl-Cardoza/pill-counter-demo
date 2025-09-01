package com.example.pillcountingnewmodels

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

/**
 * A class dedicated to performing custom preprocessing on bitmaps before model inference.
 *
 * This can include operations like color space conversion, normalization, or artifact reduction
 * to improve model accuracy.
 */
class ImagePreprocessor {

    // Reusable Paint and Canvas objects to avoid object creation during processing.
    private val grayscalePaint = Paint().apply {
        val matrix = ColorMatrix().apply { setSaturation(0f) }
        colorFilter = ColorMatrixColorFilter(matrix)
    }
    private lateinit var canvas: Canvas
    private lateinit var grayscaleBitmap: Bitmap

    /**
     * Processes a bitmap by converting it to grayscale and applying a simple glare reduction.
     *
     * This method is optimized to reuse bitmap and canvas objects to reduce memory allocation
     * and garbage collection overhead during continuous analysis.
     *
     * @param original The input [Bitmap] in its original color format.
     * @return A new [Bitmap] that has been converted to grayscale with reduced glare.
     */
    fun process(original: Bitmap): Bitmap {
        // Initialize or reconfigure the reusable bitmap and canvas if dimensions change.
        if (!::grayscaleBitmap.isInitialized || grayscaleBitmap.width != original.width || grayscaleBitmap.height != original.height) {
            grayscaleBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(grayscaleBitmap)
        }

        // 1. Convert to grayscale using the pre-configured paint.
        canvas.drawBitmap(original, 0f, 0f, grayscalePaint)

        // 2. Apply glare suppression by clamping highlight values.
        suppressGlare(grayscaleBitmap)

        return grayscaleBitmap
    }

    /**
     * Modifies a bitmap in-place to reduce simple glare by clamping pixel highlights.
     * @param bitmap The bitmap to modify.
     */
    private fun suppressGlare(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val red = Color.red(pixels[i])
            // Clamp highlights: any grayscale value above 220 is set to 220.
            if (red > 220) {
                pixels[i] = Color.rgb(220, 220, 220)
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
}