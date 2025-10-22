package com.rite.pillcounting.core.utils.common

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.rite.pillcounting.feature.pillCountScan.domain.model.DetectedPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OverlayUtils {

    /**
     * Draws pills + focus box + metadata footer.
     */
    fun drawDetectionsOnBitmap(
        bitmap: Bitmap,
        detectedPills: List<DetectedPill>,
        transform: Matrix,
        boxRect: RectF? = null,
        userName: String? = null,
        userId: String? = null,
        location: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val width = mutableBitmap.width
        val height = mutableBitmap.height

        // 🟦 1️⃣ — Setup paints
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val innerFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 20, 20, 20)
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 44f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setShadowLayer(8f, 0f, 0f, Color.BLACK)
        }

        // 🟩 2️⃣ — Draw pills & focus box (same as before)
        drawFocusBox(canvas, boxRect, width, height)
        drawPills(canvas, detectedPills, transform, boxRect, outlinePaint, innerFillPaint, textPaint)

        // 🟨 3️⃣ — Add metadata footer
        drawMetadataFooter(canvas, width, height, userName, userId, location, timestamp)

        return mutableBitmap
    }

    private fun drawFocusBox(canvas: Canvas, boxRect: RectF?, width: Int, height: Int) {
        if (boxRect == null) return
        val rect = RectF(
            boxRect.left * width,
            boxRect.top * height,
            boxRect.right * width,
            boxRect.bottom * height
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
            alpha = 150
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(rect, 40f, 40f, paint)
    }

    private fun drawPills(
        canvas: Canvas,
        pills: List<DetectedPill>,
        transform: Matrix,
        boxRect: RectF?,
        outlinePaint: Paint,
        innerFillPaint: Paint,
        textPaint: Paint
    ) {
        val temp = FloatArray(2)
        var idx = 1
        for (p in pills) {
            val inside = boxRect?.let { p.x in it.left..it.right && p.y in it.top..it.bottom } ?: true
            if (!inside) continue

            temp[0] = p.x * canvas.width
            temp[1] = p.y * canvas.height
            transform.mapPoints(temp)
            val cx = temp[0]
            val cy = temp[1]

            canvas.drawCircle(cx, cy, 18f, innerFillPaint)
            canvas.drawCircle(cx, cy, 26f, outlinePaint)
            canvas.drawText("${idx++}", cx, cy + 14f, textPaint)
        }
    }

    private fun drawMetadataFooter(
        canvas: Canvas,
        width: Int,
        height: Int,
        userName: String?,
        userId: String?,
        location: String?,
        timestamp: Long
    ) {
        val barHeight = 120f
        val rect = RectF(0f, height - barHeight, width.toFloat(), height.toFloat())
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, rect.top, 0f, rect.bottom,
                Color.argb(220, 0, 0, 0), Color.argb(180, 0, 0, 0),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(rect, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timeStr = fmt.format(Date(timestamp))

        val line1 = "Captured by: ${userName ?: "Unknown"} (${userId ?: "-"})"
        val line2 = "Location: ${location ?: "N/A"}"
        val line3 = "Time: $timeStr"

        val startX = 40f
        val startY = height - barHeight / 2 - 10f
        val lineSpacing = 32f
        canvas.drawText(line1, startX, startY - lineSpacing, textPaint)
        canvas.drawText(line2, startX, startY, textPaint)
        canvas.drawText(line3, startX, startY + lineSpacing, textPaint)
    }
}
