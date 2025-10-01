package com.example.pillcountingnewmodels.core.utils

import android.content.Context
import com.example.pillcountingnewmodels.R
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailsPDFExporter(private val context: Context) {

    // Generate a consistent filename based on drug data
    private fun generateFileName(drugName: String, ndc: String, batch: String): String {
        val safeDrugName = drugName.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
        return "DrugHistory_${safeDrugName}_${ndc}_$batch.pdf"
    }

    // Check if PDF already exists
    fun doesPdfExist(drugName: String, ndc: String, batch: String): File? {
        val fileName = generateFileName(drugName, ndc, batch)
        val file = File(context.getExternalFilesDir(null), fileName)
        return if (file.exists() && file.length() > 0) file else null
    }

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
            // First check if PDF already exists
            val existingFile = doesPdfExist(drugName, ndc, lotNo)
            if (existingFile != null) {
                return existingFile
            }

            // Create new PDF file if it doesn't exist
            val fileName = generateFileName(drugName, ndc, lotNo)
            val file = File(context.getExternalFilesDir(null), fileName)

            // Initialize PDF writer and document
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Add content to PDF
            addPdfContent(document, drugName, totalCount, notes, ndc, expiry, lotNo, date, time)

            // Close document
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


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
        // Title
        document.add(
            Paragraph(context.getString(R.string.drug_history_details))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )

        document.add(Paragraph("\n"))

        // Drug Information
        document.add(
            Paragraph("Drug Information")
                .setFontSize(16f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )

        // Create a table with 2 columns
        val drugInfo = Table(2)

        // Set table to be centered
        drugInfo.setHorizontalAlignment(HorizontalAlignment.CENTER)
        drugInfo.setWidth(UnitValue.createPercentValue(80f)) // 80% of page width

        // Add header row
        drugInfo.addHeaderCell(createHeaderCell("Field"))
        drugInfo.addHeaderCell(createHeaderCell("Value"))

        // Add data rows
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

        // Description
        document.add(
            Paragraph(context.getString(R.string.note))
                .setFontSize(16f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )
        document.add(
            Paragraph(description)
                .setTextAlignment(TextAlignment.CENTER)
        )

        // Footer
        document.add(Paragraph("\n\n"))
        document.add(
            Paragraph("${context.getString(R.string.generated_on)} ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
        )
    }

    private fun createCell(text: String, isHeader: Boolean = false): Cell {
        return Cell().apply {
            // Add padding and styling
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

    private fun createHeaderCell(text: String): Cell {
        return Cell().apply {
            setPadding(10f)
            setBackgroundColor(ColorConstants.GRAY)
            setVerticalAlignment(VerticalAlignment.MIDDLE)
            setTextAlignment(TextAlignment.CENTER)
            add(Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
        }
    }}