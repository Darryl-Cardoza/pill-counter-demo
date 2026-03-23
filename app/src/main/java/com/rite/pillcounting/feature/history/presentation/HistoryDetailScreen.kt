package com.rite.pillcounting.feature.history.presentation


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.PDFHelperExporter
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.BackButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonDialog
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.toDateString
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.toTimeString
import com.rite.pillcounting.feature.history.presentation.compose.DrugInfoSection
import com.rite.pillcounting.feature.history.presentation.viewmodel.HistoryDetailsViewModel
import com.rite.pillcounting.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun HistoryDetailScreen(
    navController: NavController,
    viewModel: HistoryDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val historyDetailsPDFExporter = remember { PDFHelperExporter(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.extendedColors.secondaryBackground),

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
                text = uiState.txnInfo?.drugName?.uppercase() ?: "",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.extendedColors.textColor,
                modifier = Modifier.weight(1f)

            )
            IconButton(
                onClick = {
                    scope.launch {
                        val pdfFile = exportToPdf(
                            historyDetailsPDFExporter = historyDetailsPDFExporter,
                            drugName = uiState.txnInfo?.drugName ?: "",
                            totalCount = uiState.txnInfo?.totalPillCount.toString(),
                            notes = uiState.txnInfo?.note ?: "",
                            ndc = uiState.txnInfo?.ndc ?: "",
                            expiry = uiState.txnInfo?.expiry ?: "",
                            lotNo = uiState.txnInfo?.lotNo ?: "",
                            date = uiState.txnInfo?.createdAt?.toDateString() ?: "",
                            time = uiState.txnInfo?.createdAt?.toTimeString() ?: ""
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 10.dp)

                )
            }

        }
        DrugInfoSection(
            ndc = uiState.txnInfo?.ndc ?: "",
            drugName = uiState.txnInfo?.drugName ?: "",
            expiry = uiState.txnInfo?.expiry ?: "",
            lotNo = uiState.txnInfo?.lotNo ?: "",
            date = uiState.txnInfo?.createdAt?.toDateString() ?: "",
            time = uiState.txnInfo?.createdAt?.toTimeString() ?: "",
            note = uiState.txnInfo?.note ?: "",
            barcodeImage = uiState.txnInfo?.barcodeImage,
            targetCount = uiState.txnInfo?.targetCount,
            transactionDetails = uiState.txnInfo?.txnDetails ?: emptyList(),
            onDelete = {
                showDeleteConfirmDialog = true
            },
            onOk = {
                navController.popBackStack()
            },
            isFromHl7 = uiState.txnInfo?.isComingFromHL7 ?: false,
            isEquivalence = uiState.txnInfo?.equivalence ?: "false"
        )

        if (showDeleteConfirmDialog) {
            CommonDialog(
                message = stringResource(R.string.delete_item_text),
                title = stringResource(R.string.confirm_delete_title),
                confirmText = stringResource(R.string.delete),
                cancelText = stringResource(R.string.cancel),
                onConfirm = {
                    viewModel.deleteTransaction()
                    showDeleteConfirmDialog = false
                    navController.popBackStack()
                },
                onCancel = { showDeleteConfirmDialog = false }
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
        } catch (_: ActivityNotFoundException) {
            // If no PDF viewer, try with generic view intent
            val fallbackIntent = Intent(Intent.ACTION_VIEW)
            fallbackIntent.setDataAndType(uri, "text/plain")
            fallbackIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                context.startActivity(fallbackIntent)
            } catch (_: ActivityNotFoundException) {
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

        val chooserIntent =
            Intent.createChooser(shareIntent, context.getString(R.string.sharePdfVia))
        context.startActivity(chooserIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "PDF saved to: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
    }
}

// Updated PDF export function to return File object
private suspend fun exportToPdf(
    historyDetailsPDFExporter: PDFHelperExporter,
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
            historyDetailsPDFExporter.generateDrugHistoryPdf(
                drugName = drugName,
                totalCount = totalCount,
                notes = notes,
                ndc = ndc,
                expiry = expiry,
                lotNo = lotNo,
                date = date,
                time = time
            )
        } catch (_: Exception) {
            null
        }
    }
}


