package com.example.pillcountingnewmodels

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A utility class for preprocessing images for a pill detection TFLite model.
 *
 * This class handles the necessary transformations to convert a standard Android [Bitmap]
 * into a [ByteBuffer] suitable for model inference. The process includes resizing the
 * bitmap to the model's required input size and normalizing the pixel values.
 *
 * @property inputSize The required square dimension (width and height) of the model's input tensor.
 */
class PillDetector(private val inputSize: Int = 640) {

    // Pre-allocate a buffer for holding the bitmap's pixel data.
    // This avoids re-allocating memory on every call to preprocess().
    private val pixelArray = IntArray(inputSize * inputSize)

    /**
     * Preprocesses a given bitmap to prepare it for inference with the TFLite model.
     *
     * The steps include:
     * 1. Scaling the bitmap to the model's expected [inputSize] x [inputSize].
     * 2. Extracting all pixel data into an integer array for high-performance processing.
     * 3. Normalizing the R, G, B channels of each pixel to a floating-point value between 0.0 and 1.0.
     * 4. Writing the normalized, interleaved RGB data into a direct ByteBuffer.
     *
     * @param bitmap The source [Bitmap] to preprocess.
     * @return A direct [ByteBuffer] containing the normalized image data, ready for the TFLite interpreter.
     */
    fun preprocess(bitmap: Bitmap): ByteBuffer {
        // 1. Allocate a direct ByteBuffer for performance.
        // It holds 4 bytes (Float) * 3 channels (RGB) * width * height.
        val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        // 2. Scale the bitmap to the required input size.
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        // 3. **PERFORMANCE CRITICAL**: Use getPixels() for a massive speed boost.
        // This bulk operation is much faster than iterating with getPixel(x, y).
        scaledBitmap.getPixels(
            pixelArray,
            0,
            inputSize,
            0,
            0,
            inputSize,
            inputSize
        )

        // 4. Iterate through the 1D array and normalize pixel data.
        for (pixelValue in pixelArray) {
            // Extract and normalize each channel using fast bitwise operations.
            val r = ((pixelValue shr 16) and 0xFF) / 255.0f
            val g = ((pixelValue shr 8) and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f

            // Write the normalized values to the buffer.
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        // 5. Rewind the buffer to the beginning to make it readable by the interpreter.
        inputBuffer.rewind()
        return inputBuffer
    }
}