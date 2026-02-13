package com.rite.pillcounting.feature.history.presentation.compose

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextUtils
import com.rite.pillcounting.R
import com.rite.pillcounting.feature.history.domain.model.TxnWithDrugDto
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryPdfExporter(private val context: Context) {


    // A4 Dimensions (Points)
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40
    private val contentWidth = pageWidth - (2 * margin)

    // Row settings
    private val rowHeight = 30f
    private val headerHeight = 35f

    // --- Paints ---
    private val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val subTitlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }

    private val tableHeaderBgPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    private val tableHeaderTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 10f // Slightly smaller to fit 5 columns
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val cellTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 10f
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }

    private val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val footerPaint = Paint().apply {
        color = Color.GRAY
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    fun generateHistoryPdf(counts: List<TxnWithDrugDto>, selectedDate: String): File? {
        val fileName = "DrugHistory_${selectedDate}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)

        val pdfDocument = PdfDocument()

        try {
            // Define Column Widths (Must sum up to contentWidth approx 515)
            // 1. Name (30%), 2. NDC (20%), 3. Count (15%), 4. Status (15%), 5. Type (20%)
            val col1 = contentWidth * 0.30f // Drug Name
            val col2 = contentWidth * 0.22f // NDC
            val col3 = contentWidth * 0.13f // Count
            val col4 = contentWidth * 0.15f // Status
            val col5 = contentWidth * 0.20f // Type

            val colWidths = floatArrayOf(col1, col2, col3, col4, col5)
            val headers = arrayOf(
                context.getString(R.string.drugname_two_lines),
                context.getString(R.string.ndc_gtin14),
                context.getString(R.string.pills_count),
                context.getString(R.string.status),
                context.getString(R.string.count_type_two_lines)
            )

            // --- Page 1 Setup ---
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            var yPosition = margin.toFloat()

            // Draw Titles
            yPosition += 30
            canvas.drawText("Drug History Report", (pageWidth / 2).toFloat(), yPosition, titlePaint)
            yPosition += 25
            canvas.drawText(
                "Selected Date: $selectedDate",
                (pageWidth / 2).toFloat(),
                yPosition,
                subTitlePaint
            )
            yPosition += 30

            // Handle Empty State
            if (counts.isEmpty()) {
                yPosition += 50
                canvas.drawText(
                    "No data available for the selected date.",
                    (pageWidth / 2).toFloat(),
                    yPosition,
                    subTitlePaint
                )
            } else {
                // Draw Initial Table Header
                drawTableHeader(canvas, margin.toFloat(), yPosition, colWidths, headers)
                yPosition += headerHeight

                // Iterate Data
                for (item in counts) {
                    // Check if we reached the bottom of the page
                    if (yPosition + rowHeight > pageHeight - margin - 30) { // 30 padding for footer
                        pdfDocument.finishPage(page)

                        // Start New Page
                        pageNumber++
                        pageInfo =
                            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = margin.toFloat() + 30 // Reset Y

                        // Draw Header again on new page
                        drawTableHeader(canvas, margin.toFloat(), yPosition, colWidths, headers)
                        yPosition += headerHeight
                    }

                    // Prepare Row Data
                    val rowData = arrayOf(
                        item.drugName ?: "",
                        item.ndc ?: "N/A",
                        item.pillCount?.toString() ?: "0",
                        item.status.toString(),
                        item.countType.toString()
                    )

                    drawTableRow(canvas, margin.toFloat(), yPosition, colWidths, rowData)
                    yPosition += rowHeight
                }
            }

            // Draw Footer (on the last page)
            val footerText = "Generated on: ${
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            }"
            canvas.drawText(
                footerText,
                (pageWidth / 2).toFloat(),
                pageHeight - margin.toFloat(),
                footerPaint
            )

            pdfDocument.finishPage(page)

            // Write File
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()

            return file

        } catch (e: Exception) {
            e.printStackTrace()
            // Try to close page if open to prevent crash
            try {
                pdfDocument.close()
            } catch (ex: Exception) { /* ignore */
            }
            return null
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Draws the Gray Header Row with White Text
     */
    private fun drawTableHeader(
        canvas: Canvas,
        startX: Float,
        y: Float,
        colWidths: FloatArray,
        headers: Array<String>
    ) {
        var currentX = startX

        // Draw background bar
        val totalWidth = colWidths.sum()
        canvas.drawRect(startX, y, startX + totalWidth, y + headerHeight, tableHeaderBgPaint)

        // Draw cells
        for (i in headers.indices) {
            val width = colWidths[i]

            // Draw Border
            canvas.drawRect(currentX, y, currentX + width, y + headerHeight, borderPaint)

            // Draw Text (Centered)
            val textX = currentX + (width / 2)
            val textY =
                y + (headerHeight / 2) - ((tableHeaderTextPaint.descent() + tableHeaderTextPaint.ascent()) / 2)

            // Handle newlines in headers (e.g. "Drug Name\n(Details)") simply by replacing with space or truncating
            // For simplicity in native canvas, we stick to single line or ellipsize
            val safeHeader = headers[i].replace("\n", " ")
            canvas.drawText(safeHeader, textX, textY, tableHeaderTextPaint)

            currentX += width
        }
    }

    /**
     * Draws a single data row
     */
    private fun drawTableRow(
        canvas: Canvas,
        startX: Float,
        y: Float,
        colWidths: FloatArray,
        data: Array<String>
    ) {
        var currentX = startX
        for (i in data.indices) {
            val width = colWidths[i]

            // Draw Border
            canvas.drawRect(currentX, y, currentX + width, y + rowHeight, borderPaint)

            // Draw Text (Centered)
            val textX = currentX + (width / 2)
            val textY =
                y + (rowHeight / 2) - ((cellTextPaint.descent() + cellTextPaint.ascent()) / 2)

            // Truncate text if too long to fit in column
            val truncatedText = TextUtils.ellipsize(
                data[i],
                android.text.TextPaint(cellTextPaint),
                width - 10f, // 10f padding
                TextUtils.TruncateAt.END
            ).toString()

            canvas.drawText(truncatedText, textX, textY, cellTextPaint)

            currentX += width
        }
    }
}