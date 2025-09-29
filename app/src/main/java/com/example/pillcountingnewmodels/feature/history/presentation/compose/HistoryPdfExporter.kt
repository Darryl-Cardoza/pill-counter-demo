package com.example.pillcountingnewmodels.feature.history.presentation.compose

import android.content.Context
import androidx.compose.ui.text.style.LineBreak.Companion.Paragraph
import com.example.pillcountingnewmodels.feature.history.domain.model.CountRowData
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


class HistoryPdfExporter(private val context: Context) {

    fun generateHistoryPdf(counts: List<CountRowData>, selectedDate: String): File? {
        return try {
            val fileName = "DrugHistory_${selectedDate}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)

            // Initialize PDF writer and document
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Add content to PDF
            addHistoryPdfContent(document, counts, selectedDate)

            // Close document
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addHistoryPdfContent(
        document: Document,
        counts: List<CountRowData>,
        selectedDate: String
    ) {
        // Title
        document.add(
            Paragraph("Drug History Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )

        document.add(Paragraph("\n"))

        // Date Information
        document.add(
            Paragraph("Selected Date: $selectedDate")
                .setFontSize(14f)
                .setTextAlignment(TextAlignment.CENTER)
        )

        document.add(Paragraph("\n"))

        if (counts.isEmpty()) {
            document.add(
                Paragraph("No data available for the selected date.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14f)
                    .setItalic()
            )
        } else {
            // Create a table with 5 columns for the history data
            val table = Table(5)

            // Set table to be centered
            table.setHorizontalAlignment(HorizontalAlignment.CENTER)
            table.setWidth(UnitValue.createPercentValue(95f))

            // Add header row
            table.addHeaderCell(createHeaderCell("Drug \nName"))
            table.addHeaderCell(createHeaderCell("NDC"))
            table.addHeaderCell(createHeaderCell("Total \nPill Count"))
            table.addHeaderCell(createHeaderCell("Status"))
            table.addHeaderCell(createHeaderCell("Count \nType"))

            // Add data rows
            counts.forEach { countData ->
//                table.addCell(createCell(countData.drugName ?: "N/A"))
//                table.addCell(createCell(countData.ndc ?: "N/A"))
//                table.addCell(createCell(countData.totalCount?.toString() ?: "0"))
//                table.addCell(createCell(countData.status ?: "N/A"))
//                table.addCell(createCell(countData.countType ?: "N/A"))

                table.addCell(createCell(countData.name ?: "N/A"))
                table.addCell(createCell("123"))
                table.addCell(createCell(countData.count ?.toString()?: "N/A"))
                table.addCell(createCell( "Partial"))
                table.addCell(createCell("Regular"))

            }

            document.add(table)
            document.add(Paragraph("\n"))


        }

        // Footer
        document.add(Paragraph("\n\n"))
        document.add(
            Paragraph("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                Date()
            )}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
        )
    }

    private fun createCell(text: String, isHeader: Boolean = false): Cell {
        return Cell().apply {
            setPadding(8f)
            setVerticalAlignment(VerticalAlignment.MIDDLE)
            setTextAlignment(TextAlignment.CENTER)
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
    }
}

