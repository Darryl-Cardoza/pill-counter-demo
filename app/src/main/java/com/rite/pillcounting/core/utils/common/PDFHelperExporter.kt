package com.rite.pillcounting.core.utils.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.rite.pillcounting.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for generating and exporting **drug history PDFs** using Android's native PdfDocument API.
 * Handles file naming, existence checks, and manual drawing of structured content on the PDF Canvas.
 */
class PDFHelperExporter(private val context: Context) {

    // Define standard A4 page dimensions in points (1 inch = 72 points)
    // A4 width: 595 points, height: 842 points
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 50

    // --- Paint Objects for Styling ---

    // Title: Bold, Large, Centered
    private val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    // Header: Bold, Medium, Centered
    private val headerPaint = Paint().apply {
        color = Color.BLACK
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    // Normal Cell Text
    private val cellTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        typeface = Typeface.DEFAULT
    }

    // Bold Label Text (for first column)
    private val cellLabelPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // Table Header Background (Dark Gray)
    private val tableHeaderBgPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    // Table Header Text (White)
    private val tableHeaderTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // Light Gray Background for labels
    private val lightGrayPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }

    // Footer: Italic, Small, Gray
    private val footerPaint = Paint().apply {
        color = Color.GRAY
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textAlign = Paint.Align.CENTER
    }

    // Borders
    private val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    /** Generates a consistent PDF filename using drug details. */
    private fun generateFileName(drugName: String, ndc: String, batch: String): String {
        val safeDrugName = drugName.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
        return "DrugHistory_${safeDrugName}_${ndc}_$batch.pdf"
    }

    /** Checks if a drug PDF already exists for the given batch. */
    fun doesPdfExist(drugName: String, ndc: String, batch: String): File? {
        val fileName = generateFileName(drugName, ndc, batch)
        val file = File(context.getExternalFilesDir(null), fileName)
        return if (file.exists() && file.length() > 0) file else null
    }

    /**
     * Creates a new drug history PDF or returns the existing one.
     * Uses native Android PdfDocument API.
     */
    fun generateDrugHistoryPdf(
        drugName: String,
        totalCount: String,
        notes: String,
        ndc: String,
        expiry: String,
        lotNo: String,
        date: String,
        time: String
    ): File? {
        // 1. Check for existing file
        val existingFile = doesPdfExist(drugName, ndc, lotNo)
        if (existingFile != null) return existingFile

        val fileName = generateFileName(drugName, ndc, lotNo)
        val file = File(context.getExternalFilesDir(null), fileName)

        // 2. Initialize PDF Document
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            var yPosition = margin.toFloat()

            // --- Draw Main Title ---
            yPosition += 40
            canvas.drawText(
                context.getString(R.string.drug_history_details),
                (pageWidth / 2).toFloat(),
                yPosition,
                titlePaint
            )
            yPosition += 40

            // --- Draw "Drug Information" Header ---
            canvas.drawText(
                "Drug Information",
                (pageWidth / 2).toFloat(),
                yPosition,
                headerPaint
            )
            yPosition += 30

            // --- Draw Table ---
            val tableWidth = pageWidth - (2 * margin)
            val column1Width = tableWidth * 0.4f // 40% width for Labels
            val column2Width = tableWidth * 0.6f // 60% width for Values
            val startX = margin.toFloat()
            val rowHeight = 35f

            // Table Header Row
            drawTableRow(
                canvas, startX, yPosition, column1Width, column2Width, rowHeight,
                "Field", "Value", isHeader = true
            )
            yPosition += rowHeight

            // Table Data Rows
            val rows = listOf(
                context.getString(R.string.drugname) to drugName,
                context.getString(R.string.total_count) to "$totalCount Pills",
                context.getString(R.string.ndc_gtin14).uppercase() to ndc,
                context.getString(R.string.expiry) to expiry,
                context.getString(R.string.lotNo) to lotNo,
                context.getString(R.string.date) to date,
                context.getString(R.string.time) to time
            )

            for ((label, value) in rows) {
                drawTableRow(
                    canvas, startX, yPosition, column1Width, column2Width, rowHeight,
                    label, value, isHeader = false
                )
                yPosition += rowHeight
            }

            yPosition += 30

            // --- Draw "Notes" Header ---
            canvas.drawText(
                context.getString(R.string.note),
                (pageWidth / 2).toFloat(),
                yPosition,
                headerPaint
            )
            yPosition += 20

            // --- Draw Notes Body (Multi-line) ---
            // We use StaticLayout to handle text wrapping automatically
            val notesTextPaint = TextPaint(cellTextPaint)
            val notesLayout = StaticLayout.Builder.obtain(
                notes,
                0,
                notes.length,
                notesTextPaint,
                pageWidth - (2 * margin)
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.0f)
                .setIncludePad(false)
                .build()

            canvas.save()
            canvas.translate(margin.toFloat(), yPosition)
            notesLayout.draw(canvas)
            canvas.restore()

            // Update Y position based on how tall the notes were
            yPosition += notesLayout.height + 40

            // --- Draw Footer (Date) ---
            val footerText = "${context.getString(R.string.generated_on)} " +
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            canvas.drawText(
                footerText,
                (pageWidth / 2).toFloat(),
                yPosition,
                footerPaint
            )

            // 3. Finish Page and Write File
            pdfDocument.finishPage(page)

            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()

            return file
        } catch (e: IOException) {
            e.printStackTrace()
            // If writing fails, try to close the page cleanly
            try {
                pdfDocument.finishPage(page)
            } catch (ignored: Exception) {
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Helper to draw a specific row in the table (Header or Normal).
     * Draws background rects, borders, and text.
     */
    private fun drawTableRow(
        canvas: Canvas,
        startX: Float,
        y: Float,
        col1Width: Float,
        col2Width: Float,
        height: Float,
        text1: String,
        text2: String,
        isHeader: Boolean
    ) {
        // 1. Draw Backgrounds
        if (isHeader) {
            // Dark gray background for whole header row
            canvas.drawRect(
                startX,
                y,
                startX + col1Width + col2Width,
                y + height,
                tableHeaderBgPaint
            )
        } else {
            // Light gray background for the Label cell (left side)
            canvas.drawRect(startX, y, startX + col1Width, y + height, lightGrayPaint)
        }

        // 2. Draw Borders (Outlines)
        canvas.drawRect(startX, y, startX + col1Width, y + height, borderPaint) // Cell 1 Border
        canvas.drawRect(
            startX + col1Width,
            y,
            startX + col1Width + col2Width,
            y + height,
            borderPaint
        ) // Cell 2 Border

        // 3. Calculate Text Vertical Center
        // Font metrics are needed for perfect centering, but (height/2) + fudge_factor works well for simple PDFs
        val textY = y + (height / 2) + 5

        // 4. Draw Text
        val paint1 = if (isHeader) tableHeaderTextPaint else cellLabelPaint
        val paint2 = if (isHeader) tableHeaderTextPaint else cellTextPaint

        // Draw Column 1 (Left aligned with padding)
        canvas.drawText(text1, startX + 10, textY, paint1)

        // Draw Column 2 (Left aligned with padding)
        canvas.drawText(text2, startX + col1Width + 10, textY, paint2)
    }
}