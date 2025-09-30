package com.example.pillcountingnewmodels.feature.history.presentation


import com.example.pillcountingnewmodels.core.utils.PdfExporter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler

import com.example.pillcountingnewmodels.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.feature.history.presentation.compose.DrugInfoSection

import com.example.pillcountingnewmodels.feature.profile.presentation.viewmodel.ProfileViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}


@Composable
fun HistoryDetailScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val appTheme = AppTheme
    val landscape = isLandscape()

    val drugName = "Allopurinol 5MG"
    val ndc = "123654"
    val expiry = "12-08-2025"
    val batch = "45698"
    val date = "12-01-2025"
    val time = "11:25 am"

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // PDF export state
    var showExportResult by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }

    val pdfExporter = remember { PdfExporter(context) }

    val scrollState = rememberScrollState()

    val images = listOf(
        R.drawable.pill_count_image,
        R.drawable.pill_count_image,
        R.drawable.pill_count_image,
        R.drawable.pill_count_image
    )

    BackHandler {
        onBackClick()
    }

    if (landscape) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top

        ) {


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(appTheme.extendedColors.secondaryBackground)
            ) {
                BackButton(navController)

                Text(
                    text = drugName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = appTheme.extendedColors.textColor,
                    modifier = Modifier.weight(1f)

                )
                IconButton(
                    onClick = {
                            scope.launch {
                                val pdfFile = exportToPdf(
                                    pdfExporter = pdfExporter,
                                    drugName = drugName,
                                    totalCount = "456",
                                    description = "adasdnaudbahbdahd",
                                    ndc = ndc,
                                    expiry = expiry,
                                    batch = "5454545",
                                    date = date,
                                    time = time
                                )

                                if (pdfFile != null) {
                                    // Open the PDF file
                                    openPdfFile(context, pdfFile)
                                    exportSuccess = true
                                    exportMessage = "PDF exported and opened successfully!"
                                } else {
                                    exportSuccess = false
                                    exportMessage = "Failed to export PDF. Please try again."
                                }
                                showExportResult = true
                            }

                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pdf),
                        contentDescription = "Export PDF",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 20.dp)
                            .height(36.dp)
                    )
                }

            }

            DrugInfoSection(
                appTheme = appTheme,
                navController = navController,
                drugName = drugName,
                ndc = ndc,
                images = images,
                expiry = expiry,
                batch = batch,
                date = date,
                time = time,
                )
        }

    } else {
        Column(
            modifier = Modifier
                .fillMaxSize(),

            verticalArrangement = Arrangement.SpaceEvenly

        ) {
            DrugInfoSection(
                appTheme = appTheme,
                navController = navController,
                drugName = drugName,
                ndc = ndc,
                images = images,
                expiry = expiry,
                batch = batch,
                date = date,
                time = time
            )
        }
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
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share PDF via")
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "PDF saved to: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
// Updated PDF export function to return File object
private suspend fun exportToPdf(
    pdfExporter: PdfExporter,
    drugName: String,
    totalCount: String,
    description: String,
    ndc: String,
    expiry: String,
    batch: String,
    date: String,
    time: String
): File? {
    return withContext(Dispatchers.IO) {
        try {
            pdfExporter.generateDrugHistoryPdf(
                drugName = drugName,
                totalCount = totalCount,
                description = description,
                ndc = ndc,
                expiry = expiry,
                batch = batch,
                date = date,
                time = time
            )
        } catch (e: Exception) {
            null
        }
    }
}


