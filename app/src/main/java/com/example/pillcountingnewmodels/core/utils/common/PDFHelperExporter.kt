package com.example.pillcountingnewmodels.core.utils.common

import android.content.Context
import com.example.pillcountingnewmodels.R
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for generating and exporting **drug history PDFs** using iText.
 * Handles file naming, existence checks, and structured PDF content creation.
 */
class PDFHelperExporter(private val context: Context) {

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
     * Includes drug info, counts, notes, and generated date.
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
        return try {
            val existingFile = doesPdfExist(drugName, ndc, lotNo)
            if (existingFile != null) return existingFile

            val fileName = generateFileName(drugName, ndc, lotNo)
            val file = File(context.getExternalFilesDir(null), fileName)

            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            addPdfContent(document, drugName, totalCount, notes, ndc, expiry, lotNo, date, time)

            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Adds table, title, and footer content to the PDF document. */
    private fun addPdfContent(
        document: Document,
        drugName: String,
        totalCount: String,
        description: String,
        ndc: String,
        expiry: String,
        batch: String,
        date: String,
        time: String
    ) {
        document.add(
            Paragraph(context.getString(R.string.drug_history_details))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )

        document.add(Paragraph("\n"))

        document.add(
            Paragraph("Drug Information")
                .setFontSize(16f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )

        val drugInfo = Table(2).apply {
            setHorizontalAlignment(HorizontalAlignment.CENTER)
            setWidth(UnitValue.createPercentValue(80f))
            addHeaderCell(createHeaderCell("Field"))
            addHeaderCell(createHeaderCell("Value"))
        }

        drugInfo.addCell(createCell(context.getString(R.string.drugname), true))
        drugInfo.addCell(createCell(drugName))

        drugInfo.addCell(createCell(context.getString(R.string.total_count), true))
        drugInfo.addCell(createCell("$totalCount Pills"))

        drugInfo.addCell(createCell(context.getString(R.string.ndc).uppercase(), true))
        drugInfo.addCell(createCell(ndc))

        drugInfo.addCell(createCell(context.getString(R.string.expiry), true))
        drugInfo.addCell(createCell(expiry))

        drugInfo.addCell(createCell(context.getString(R.string.lotNo), true))
        drugInfo.addCell(createCell(batch))

        drugInfo.addCell(createCell(context.getString(R.string.date), true))
        drugInfo.addCell(createCell(date))

        drugInfo.addCell(createCell(context.getString(R.string.time), true))
        drugInfo.addCell(createCell(time))

        document.add(drugInfo)
        document.add(Paragraph("\n"))

        document.add(
            Paragraph(context.getString(R.string.note))
                .setFontSize(16f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )
        document.add(Paragraph(description).setTextAlignment(TextAlignment.CENTER))

        document.add(Paragraph("\n\n"))
        document.add(
            Paragraph(
                "${context.getString(R.string.generated_on)} " +
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            )
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
        )
    }

    /** Creates a table cell with optional header styling. */
    private fun createCell(text: String, isHeader: Boolean = false): Cell {
        return Cell().apply {
            setPadding(8f)
            setVerticalAlignment(VerticalAlignment.MIDDLE)
            add(Paragraph(text).apply {
                if (isHeader) {
                    setBold()
                    setBackgroundColor(ColorConstants.LIGHT_GRAY)
                }
            })
        }
    }

    /** Creates a styled header cell with bold white text on a gray background. */
    private fun createHeaderCell(text: String): Cell {
        return Cell().apply {
            setPadding(10f)
            setBackgroundColor(ColorConstants.GRAY)
            setVerticalAlignment(VerticalAlignment.MIDDLE)
            setTextAlignment(TextAlignment.CENTER)
            add(Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
        }
    }
}
