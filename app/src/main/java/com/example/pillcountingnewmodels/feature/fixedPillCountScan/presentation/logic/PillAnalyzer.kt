package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.logic

import android.graphics.Bitmap
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.domain.data.DetectedPill
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import kotlin.math.exp

class PillAnalyzer(
    private val interpreter: Interpreter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val logger = AppLogger.create<PillAnalyzer>()

    suspend fun analyzeFrame(
        bitmap: Bitmap,
        rotationDegrees: Int = 0
    ): List<DetectedPill> = withContext(dispatcher) {
        try {
            logger.i("Starting analysis (rotation=$rotationDegrees)...")

            val inputBuffer: ByteBuffer =
                ImagePreprocessor.preprocess(bitmap, rotationDegrees, dispatcher)

            val outputBuffer = Array(1) { Array(38) { FloatArray(8400) } }

            val start = System.currentTimeMillis()
            interpreter.run(inputBuffer, outputBuffer)
            logger.d("Inference complete in ${System.currentTimeMillis() - start} ms")

            logRawOutput(outputBuffer)

            val pills = PostProcessor.processAsync(outputBuffer)
            logger.i("Analysis finished. Detected ${pills.size} pills.")
            pills

        } catch (e: Exception) {
            logger.e("Analysis failed", e)
            emptyList()
        }
    }

    /**
     * Helper function to convert a raw logit score into a probability (0.0 to 1.0).
     */
    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    private fun logRawOutput(output: Array<Array<FloatArray>>) {
        val batchSize = output.size
        val features = output[0].size
        val detections = output[0][0].size
        logger.d("--- Raw Model Output ---")
        logger.d("Shape: [$batchSize][$features][$detections]")

        val topDetections = mutableListOf<Pair<Int, Float>>()
        val results = output[0]

        for (i in 0 until detections) {
            var maxScore = 0f
            for (j in 4 until features) {
                // *** FIX: Apply sigmoid to the raw logit score for logging ***
                val score = sigmoid(results[j][i])
                if (score > maxScore) {
                    maxScore = score
                }
            }
            if (maxScore > 0.1) {
                topDetections.add(Pair(i, maxScore))
            }
        }

        topDetections.sortByDescending { it.second }
        val top5 = topDetections.take(5)

        logger.d("Top 5 Detections (before NMS):")
        top5.forEach { (index, score) ->
            val cx = results[0][index]
            val cy = results[1][index]
            val w = results[2][index]
            val h = results[3][index]
            logger.d("  -> Index: $index, Score: %.4f, Box: [cx=%.2f, cy=%.2f, w=%.2f, h=%.2f]".format(score, cx, cy, w, h))
        }
        logger.d("--- End Raw Model Output ---")
    }
}