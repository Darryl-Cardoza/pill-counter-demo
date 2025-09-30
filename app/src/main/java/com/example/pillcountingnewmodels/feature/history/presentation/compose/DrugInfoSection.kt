package com.example.pillcountingnewmodels.feature.history.presentation.compose

import com.example.pillcountingnewmodels.core.utils.PdfExporter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.small
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.xxLarge
import com.example.pillcountingnewmodels.core.utils.compose.FilledButton
import com.example.pillcountingnewmodels.core.utils.compose.HollowButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DrugInfoSection(
    appTheme: AppTheme,
    navController: NavController,
    drugName: String,
    ndc: String,
    images: List<Int>,
    expiry: String,
    batch: Any,
    date: String,
    time: String,
) {
    val configuration = LocalConfiguration.current
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // PDF export state
    var showExportResult by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }

    val pdfExporter = remember { PdfExporter(context) }

    if (landscape) {
        Column(
            modifier = Modifier
                .background(appTheme.extendedColors.secondaryBackground)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(start = 20.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    ImagewithCount(landscape = landscape, appTheme = appTheme)
                    Spacer(Modifier.height(15.dp))
                    DrugHistoryDetailsBox(appTheme)
                    Row(
                        modifier = Modifier
                            .background(appTheme.extendedColors.secondaryBackground)
                            .padding(18.dp)
                    ) {
                        HollowButton(
                            text = stringResource(R.string.delete).uppercase(),
                            onClick = {},
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 20.dp)
                        )

                        FilledButton(
                            text = stringResource(R.string.ok).uppercase(),
                            onClick = {},
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.4f),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    HistoryDrugDetails(
                        ndc = ndc,
                        expiry = expiry,
                        batch = batch.toString(),
                        date = date,
                        time = time
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(0.2f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    ImageGallery(imageResources = images, landscape = landscape)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appTheme.extendedColors.secondaryBackground),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: back button + screen title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
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

            ImagewithCount(landscape = landscape, appTheme = appTheme)

            HistoryDrugDetails(
                ndc = ndc,
                expiry = expiry,
                batch = batch.toString(),
                date = date,
                time = time
            )

            DrugHistoryDetailsBox(appTheme)

            ImageGallery(imageResources = images, landscape = landscape)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = xxLarge, start = medium, end = medium),
                horizontalArrangement = Arrangement.spacedBy(small)
            ) {
                HollowButton(
                    text = stringResource(R.string.delete).uppercase(),
                    onClick = {},
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                FilledButton(
                    text = stringResource(R.string.ok).uppercase(),
                    onClick = {},
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
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