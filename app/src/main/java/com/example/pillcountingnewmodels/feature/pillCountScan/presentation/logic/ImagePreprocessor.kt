package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import com.example.pillcountingnewmodels.core.utils.AppLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

object ImagePreprocessor {

    private const val MODEL_INPUT_SIZE = 640
    private const val PIXEL_SIZE = 3  // RGB
    private const val FLOAT_SIZE = 4
    private const val BATCH_SIZE = 1

    private val logger = AppLogger.create<ImagePreprocessor>()

    /**
     * Suspend function → runs preprocessing off the main thread.
     */
    suspend fun preprocess(
        bitmap: Bitmap,
        rotationDegrees: Int = 0,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): ByteBuffer = withContext(dispatcher) {
        val startTime = System.currentTimeMillis()
        logger.i("Preprocessing started (rotation=$rotationDegrees, input=${bitmap.width}x${bitmap.height})")

        // Step 1: Fix orientation if needed
        val orientedBitmap = if (rotationDegrees != 0) {
            try {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
                    logger.d("Bitmap rotated by $rotationDegrees° → new size=${it.width}x${it.height}")
                }
            } catch (e: Exception) {
                logger.e("Rotation failed, using original bitmap", e)
                bitmap
            }
        } else bitmap

        // Step 2: Resize to model input
        val resized = try {
            Bitmap.createScaledBitmap(orientedBitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true).also {
                logger.d("Resized bitmap to ${it.width}x${it.height}")
            }
        } catch (e: OutOfMemoryError) {
            logger.e("OOM during resizing, reusing original (may fail in model)", e)
            orientedBitmap
        }

        // Step 3: Glare reduction
        val glareReduced = reduceGlare(resized)

        // Step 4: Allocate buffer
        val bufferSize = BATCH_SIZE * MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * PIXEL_SIZE * FLOAT_SIZE
        val buffer = ByteBuffer.allocateDirect(bufferSize).apply { order(ByteOrder.nativeOrder()) }
        logger.d("Allocated ByteBuffer of size=${bufferSize / 1024} KB")

        // Step 5: Normalize pixels
        var skipped = 0
        for (y in 0 until MODEL_INPUT_SIZE) {
            for (x in 0 until MODEL_INPUT_SIZE) {
                val pixel = glareReduced.getPixel(x, y)

                var r = (pixel shr 16 and 0xFF).toFloat()
                var g = (pixel shr 8 and 0xFF).toFloat()
                var b = (pixel and 0xFF).toFloat()

                // Adaptive contrast correction
                val avg = (r + g + b) / 3f
                val factor = when {
                    avg > 200 -> 0.8f
                    avg < 50 -> 1.2f
                    else -> 1f
                }

                r = (r * factor).coerceIn(0f, 255f)
                g = (g * factor).coerceIn(0f, 255f)
                b = (b * factor).coerceIn(0f, 255f)

                try {
                    buffer.putFloat(r / 255f)
                    buffer.putFloat(g / 255f)
                    buffer.putFloat(b / 255f)
                } catch (e: Exception) {
                    skipped++
                }
            }
        }

        buffer.rewind()
        val elapsed = System.currentTimeMillis() - startTime
        logger.i("Preprocessing complete in ${elapsed}ms (skipped=$skipped pixels)")

        buffer
    }

    /**
     * Simple glare reduction by clamping high-intensity regions.
     * Runs lighter by skipping pixels in steps.
     */
    private fun reduceGlare(bitmap: Bitmap): Bitmap {
        logger.d("Running glare reduction on ${bitmap.width}x${bitmap.height} bitmap...")
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        var adjusted = 0
        for (y in 0 until mutable.height step 2) {
            for (x in 0 until mutable.width step 2) {
                val pixel = mutable.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val maxVal = max(r, max(g, b))
                val minVal = min(r, min(g, b))

                var newR = r
                var newG = g
                var newB = b

                if (maxVal > 230 && minVal < 180) {
                    newR = (newR * 0.8).toInt()
                    newG = (newG * 0.8).toInt()
                    newB = (newB * 0.8).toInt()
                    mutable.setPixel(x, y, Color.rgb(newR, newG, newB))
                    adjusted++
                }
            }
        }

        logger.d("Glare reduction adjusted $adjusted pixels")
        return mutable
    }
}
