package com.rite.pillcounting.core.utils.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * ## OverlayUtils
 *
 * A rendering utility responsible for overlaying **pill detections**, **numbering**, and
 * **operator metadata** on captured camera frames.
 * The overlay reverse-maps preview coordinates into original bitmap space by
 * reconstructing the same **CENTER_CROP** transformation logic used by
 * [com.rite.pillcounting.feature.pillCountScan.presentation.logic.Postprocessor], ensuring perfect alignment between preview detections and saved image output.
 *
 * ---
 */
object OverlayUtils {

    /**
     * Draws pill detection overlays and footer metadata onto a copy of the given [bitmap].
     *
     * @param bitmap          The base camera frame to draw on (copied internally, original remains unchanged).
     * @param detectedPills   List of pills with normalized positions (`x`, `y` in range `0..1`).
     * @param previewWidth    Width of the preview surface in pixels.
     * @param previewHeight   Height of the preview surface in pixels.
     * @param userName        Optional operator name displayed in the footer.
     * @param userId          Optional operator ID displayed in the footer.
     * @param location        Optional human-readable location text.
     * @param timestamp       Capture timestamp in milliseconds (defaults to current system time).
     *
     * @return A new [Bitmap] containing the rendered overlay (safe to save or display).
     */
    fun drawDetectionsOnBitmap(
        bitmap: Bitmap,
        detectedPills: List<DetectedPill>,
        previewWidth: Int,
        previewHeight: Int,
        userName: String? = null,
        userId: String? = null,
        location: String? = null,
        ndc:String? = null,
        count:String? = null,
        patientId: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Bitmap {
        // Create a mutable copy to avoid altering the original frame
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val w = result.width.toFloat()
        val h = result.height.toFloat()
        val scale = min(w, h) / 1080f

        // ---------------------------------------------------------------------
        // Paint Configuration
        // ---------------------------------------------------------------------

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(180, 0, 0, 0)
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 3f * scale
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 28f * scale
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            setShadowLayer(6f, 0f, 0f, Color.BLACK)
        }

        // ---------------------------------------------------------------------
        // Reverse Mapping (Preview → Bitmap)
        // ---------------------------------------------------------------------

        val srcW = w
        val srcH = h
        val scaleToView = max(previewWidth / srcW, previewHeight / srcH)
        val scaledW = srcW * scaleToView
        val scaledH = srcH * scaleToView
        val dx = (previewWidth - scaledW) / 2f
        val dy = (previewHeight - scaledH) / 2f

        // Build inverse transform for mapping from preview coordinates to bitmap coordinates
        val inverseTransform = Matrix().apply {
            postTranslate(-dx, -dy)
            postScale(1f / scaleToView, 1f / scaleToView)
        }

        // Prepare coordinate array for mapping
        val pts = FloatArray(detectedPills.size * 2)
        detectedPills.forEachIndexed { i, pill ->
            pts[i * 2] = pill.x * previewWidth
            pts[i * 2 + 1] = pill.y * previewHeight
        }

        // Apply transformation
        inverseTransform.mapPoints(pts)

        // Clamp mapped coordinates to ensure on-canvas safety
        for (i in pts.indices step 2) {
            pts[i] = pts[i].coerceIn(0f, w)
            pts[i + 1] = pts[i + 1].coerceIn(0f, h)
        }

        // ---------------------------------------------------------------------
        // Draw Numbered Circles (Detections)
        // ---------------------------------------------------------------------

        for (i in pts.indices step 2) {
            val cx = pts[i]
            val cy = pts[i + 1]
            val isLast = i / 2 == detectedPills.lastIndex

            val innerR = (if (isLast) 38f else 24f) * scale
            val outerR = (if (isLast) 34f else 20f) * scale
            val stroke = (if (isLast) 2f else 1f) * scale

            strokePaint.strokeWidth = stroke
            fillPaint.color = if (isLast)
                Color.argb(160, 0, 0, 0)      // Yellow highlight for latest pill
            else
                Color.argb(160, 0, 0, 0)       // Semi-transparent black background

            textPaint.textSize = if (isLast) {
                36f * scale   // BIG text for last pill
            } else {
                28f * scale   // normal text
            }
            // Draw pill marker
            canvas.drawCircle(cx, cy, innerR, fillPaint)
            canvas.drawCircle(cx, cy, outerR, strokePaint)

            // Draw index label (centered vertically)
            val fm = textPaint.fontMetrics
            val offsetY = (fm.descent - fm.ascent) / 2 - fm.descent
            canvas.drawText("${i / 2 + 1}", cx, cy + offsetY, textPaint)
        }

        // ---------------------------------------------------------------------
        // Footer Metadata
        // ---------------------------------------------------------------------

        val footerLine1 = buildString {
            if (!ndc.isNullOrBlank()) append("NDC: $ndc  ")
            if (!count.isNullOrBlank()) append("Count: $count  ")
            if (!patientId.isNullOrBlank()) append("Patient: $patientId")
        }

        val footerLine2 = buildString {
            if (!userName.isNullOrBlank()) append(userName)
            if (!userId.isNullOrBlank()) append(" ($userId)  ")
            if (!location.isNullOrBlank()) append("[$location]  ")
            append(
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date(timestamp))
            )
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f * scale
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, Color.BLACK)
        }

        val lineHeight = footerPaint.fontMetrics.run {
            descent - ascent
        }

        val bottomPadding = 40f * scale

        canvas.drawText(
            footerLine2,
            w / 2,
            h - bottomPadding,
            footerPaint
        )

        canvas.drawText(
            footerLine1,
            w / 2,
            h - bottomPadding - lineHeight,
            footerPaint
        )


        // ---------------------------------------------------------------------
        // Return final annotated bitmap
        // ---------------------------------------------------------------------
        return result
    }
}
