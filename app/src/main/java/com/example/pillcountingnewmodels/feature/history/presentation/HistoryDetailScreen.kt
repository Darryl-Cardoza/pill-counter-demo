package com.example.pillcountingnewmodels.feature.history.presentation


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.PdfExporter
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.feature.history.presentation.compose.DrugInfoSection
import com.example.pillcountingnewmodels.feature.history.presentation.viewmodel.HistoryDetailsViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun HistoryDetailScreen(
    navController: NavController,
    viewModel: HistoryDetailsViewModel = hiltViewModel()
) {
    val drugName = "Allopurinol 5MG"
    val ndc = "123654"
    val expiry = "12-08-2025"
    val lotNo = "45698"
    val date = "12-01-2025"
    val time = "11:25 am"
    val note = "My long note"

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pdfExporter = remember { PdfExporter(context) }

    val images = listOf(
        R.drawable.history,
        R.drawable.history,
        R.drawable.history,
        R.drawable.history
    )

    Column(
        modifier = Modifier.fillMaxSize().background(AppTheme.extendedColors.secondaryBackground),

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            BackButton(navController = navController)

            Text(
                text = drugName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.extendedColors.textColor,
                modifier = Modifier.weight(1f)

            )
            IconButton(
                onClick = {
                    scope.launch {
                        val pdfFile = exportToPdf(
                            pdfExporter = pdfExporter,
                            drugName = drugName,
                            totalCount = "456",
                            notes = "adasdnaudbahbdahd",
                            ndc = ndc,
                            expiry = expiry,
                            lotNo = "5454545",
                            date = date,
                            time = time
                        )

                        if (pdfFile != null) {
                            // Open the PDF file
                            openPdfFile(context, pdfFile)
                        }
                    }

                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pdf),
                    contentDescription = "Export PDF",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 10.dp)

                )
            }

        }

        DrugInfoSection(
            ndc = ndc,
            images = images,
            expiry = expiry,
            lotNo = lotNo,
            date = date,
            time = time,
            note = note
        )
    }
}

private fun openPdfFile(context: Context, pdfFile: File) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)

        // Use FileProvider for better security and compatibility
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Try to open with PDF viewer
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // If no PDF viewer, try with generic view intent
            val fallbackIntent = Intent(Intent.ACTION_VIEW)
            fallbackIntent.setDataAndType(uri, "text/plain")
            fallbackIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                context.startActivity(fallbackIntent)
            } catch (e2: ActivityNotFoundException) {
                // Last resort - share the PDF file
                sharePdfFile(context, pdfFile)
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
        sharePdfFile(context, pdfFile) // Fallback to sharing
    }
}

// Fallback function to share PDF
private fun sharePdfFile(context: Context, pdfFile: File) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.sharePdfVia))
        context.startActivity(chooserIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "PDF saved to: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
    }
}

// Updated PDF export function to return File object
private suspend fun exportToPdf(
    pdfExporter: PdfExporter,
    drugName: String,
    totalCount: String,
    notes: String,
    ndc: String,
    expiry: String,
    lotNo: String,
    date: String,
    time: String
): File? {
    return withContext(Dispatchers.IO) {
        try {
            pdfExporter.generateDrugHistoryPdf(
                drugName = drugName,
                totalCount = totalCount,
                notes = notes,
                ndc = ndc,
                expiry = expiry,
                lotNo = lotNo,
                date = date,
                time = time
            )
        } catch (e: Exception) {
            null
        }
    }
}


