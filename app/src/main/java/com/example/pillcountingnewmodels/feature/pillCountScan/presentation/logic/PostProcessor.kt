package com.example.pillcountingnewmodels.feature.pillCountScan.presentation.logic

import android.graphics.RectF
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.model.DetectedPill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

object PostProcessor {

    private val logger = AppLogger.create<PostProcessor>()

    // Updated confidence threshold as requested
    private const val CONFIDENCE_THRESHOLD = 0.7f
    private const val IOU_THRESHOLD = 0.45f

    private data class DetectionCandidate(val rect: RectF, val score: Float, val classId: Int)

    /**
     * Helper function to convert a raw logit score into a probability (0.0 to 1.0).
     */
    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    suspend fun processAsync(
        modelOutput: Array<Array<FloatArray>>
    ): List<DetectedPill> = withContext(Dispatchers.Default) {
        logger.i("Starting post-processing on background thread...")

        val rawDetections = mutableListOf<DetectionCandidate>()
        val results = modelOutput[0]

        for (i in 0 until 8400) {
            var maxScore = 0f
            var bestClassId = -1
            for (j in 4 until 38) {
                // *** FIX: Apply sigmoid to the raw logit score ***
                val score = sigmoid(results[j][i])
                if (score > maxScore) {
                    maxScore = score
                    bestClassId = j - 4
                }
            }

            if (maxScore > CONFIDENCE_THRESHOLD) {
                val cx = results[0][i]
                val cy = results[1][i]
                val w = results[2][i]
                val h = results[3][i]

                val left = cx - w / 2
                val top = cy - h / 2
                val right = cx + w / 2
                val bottom = cy + h / 2

                val rect = RectF(left, top, right, bottom)
                rawDetections.add(DetectionCandidate(rect, maxScore, bestClassId))
            }
        }
        logger.i("Found ${rawDetections.size} candidates above confidence threshold of $CONFIDENCE_THRESHOLD.")

        val finalDetections = nonMaxSuppression(rawDetections)
        logger.i("Post-NMS, final pill count is ${finalDetections.size}.")

        return@withContext finalDetections.map {
            DetectedPill(x = it.rect.centerX(), y = it.rect.centerY(), confidence = it.score)
        }
    }

    private fun nonMaxSuppression(detections: List<DetectionCandidate>): List<DetectionCandidate> {
        if (detections.isEmpty()) return emptyList()

        val sortedDetections = detections.sortedByDescending { it.score }.toMutableList()
        val finalDetections = mutableListOf<DetectionCandidate>()

        while (sortedDetections.isNotEmpty()) {
            val best = sortedDetections.removeAt(0)
            finalDetections.add(best)

            val iterator = sortedDetections.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                val iou = calculateIOU(best.rect, next.rect)
                if (iou > IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }
        return finalDetections
    }

    private fun calculateIOU(box1: RectF, box2: RectF): Float {
        val xA = max(box1.left, box2.left)
        val yA = max(box1.top, box2.top)
        val xB = min(box1.right, box2.right)
        val yB = min(box1.bottom, box2.bottom)

        val intersectionArea = max(0f, xB - xA) * max(0f, yB - yA)
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)
        val unionArea = box1Area + box2Area - intersectionArea

        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }
}