package com.rite.pillcounting.core.utils.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill

/**
 * Utility class for rendering visual overlays (pill detection markers)
 * directly onto captured camera frame bitmaps.
 * @object OverlayUtils
 * A stateless utility designed for composable and testable image overlay rendering.
 */
object OverlayUtils {

    /**
     * Draws detection markers on top of a provided bitmap using the given transformation.
     *
     * The function creates a mutable copy of [bitmap] to ensure the original
     * frame remains unmodified, then iterates through each [DetectedPill]
     * and draws a filled circle at its transformed position.
     *
     * @param bitmap The base camera frame [Bitmap] on which to overlay detections.
     * @param detectedPills List of detected pill objects, each containing normalized
     * coordinates (relative to the preview dimensions).
     * @param transform The [Matrix] that converts normalized or preview coordinates
     * into the actual pixel space of the given [bitmap].
     *
     * @return A new [Bitmap] instance with the detection overlay drawn.
     *
     * @see DetectedPill
     */
    fun drawDetectionsOnBitmap(
        bitmap: Bitmap,
        detectedPills: List<DetectedPill>,
        transform: Matrix
    ): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            strokeWidth = 6f
        }

        val tempPoint = FloatArray(2)
        for (pill in detectedPills) {
            // Convert normalized UI coordinates back to bitmap coordinates
            tempPoint[0] = pill.x * bitmap.width
            tempPoint[1] = pill.y * bitmap.height
            transform.mapPoints(tempPoint)
            canvas.drawCircle(tempPoint[0], tempPoint[1], 10f, paint)
        }

        return mutableBitmap
    }
}
