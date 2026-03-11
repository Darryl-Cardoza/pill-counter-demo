package com.rite.pillcounting.feature.history.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonDialog
import com.rite.pillcounting.core.utils.compose.SplitResponsive
import com.rite.pillcounting.feature.history.presentation.compose.CalendarSection
import com.rite.pillcounting.feature.history.presentation.compose.CountsSection
import com.rite.pillcounting.feature.history.presentation.compose.HistoryPdfExporter
import com.rite.pillcounting.feature.history.presentation.viewmodel.HistoryViewModel
import com.rite.pillcounting.ui.theme.AppTheme
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import com.rite.pillcounting.R
import com.rite.pillcounting.feature.history.domain.model.HistoryMode
import com.rite.pillcounting.feature.countResume.presentation.compose.HeadlineBar

/**
 * HistoryScreen
 *
 * Displays user's medicine history in a responsive split layout:
 * - **Calendar Section:** Select a date to filter counts.
 * - **Counts Section:** Shows medicines counted for selected date.
 *
 * State is fully managed by [HistoryViewModel].
 *
 * @param viewModel ViewModel providing state and counts data.
 * @param onBackClick Callback invoked when the back button is pressed.
 */
@Composable
fun HistoryScreen(
    navController: NavController,
    historyMode: HistoryMode = HistoryMode.NORMAL,
    viewModel: HistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Observe selected date and counts from ViewModel
    val selectedDate by viewModel.selectedDate.collectAsState()
    val counts by viewModel.counts.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val pdfExporter = remember { HistoryPdfExporter(context) }
    // Calendar state must be initialized in Composable context
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    var showSearch by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(3),
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    LaunchedEffect(historyMode) {
        viewModel.setHistoryMode(historyMode)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {

        if (showSearch) {

            // SEARCH MODE SCREEN
            HeadlineBar(
                navController = navController,
                title = "",
                searchQuery = searchQuery,
                showSearch = true,
                isMultiSelectMode = false,
                isAllSelected = false,
                hasSelection = false,
                showDelete = false,
                onSearchClick = {
                    viewModel.setSearchQuery("")  // clear text
                    showSearch = false           // return to calendar mode
                } ,
                onSearchChange = { viewModel.setSearchQuery(it) },
                onDeleteClick = {},
                onCancelClick = {},
                onConfirmDelete = {},
                onSelectAll = {}
            )

            val openPdfWith = stringResource(R.string.open_pdf_with)
            CountsSection(
                counts = counts,
                onExportClick = {
                    val file = pdfExporter.generateHistoryPdf(counts, selectedDate.toString())
                    file?.let {
                        sharePdfFile(context, it, openPdfWith)
                    }
                },
                onDeleteClick = { showDeleteConfirmationDialog = true },
                onTxnClick = { txnId ->
                    viewModel.selectCurrentTransaction(txnId)
                    navController.navigate(Screen.HistoryDetail.route)
                }
            )

        } else {

            // NORMAL MODE SCREEN
            SplitResponsive(
                topOrLeft = {
                    CalendarSection(
                        calendarState = calendarState,
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.selectDate(it) },
                        onBackClick = onBackClick,
                        navController = navController,
                        viewModel = viewModel,
                        showSearch = false,
                        onSearchToggle = { showSearch = true }
                    )
                },
                bottomOrRight = {
                    val openPdfWith = stringResource(R.string.open_pdf_with)
                    CountsSection(
                        counts = counts,
                        onExportClick = {
                            val file =
                                pdfExporter.generateHistoryPdf(counts, selectedDate.toString())
                            file?.let {
                                sharePdfFile(context, it, openPdfWith)
                            }
                        },
                        onDeleteClick = { showDeleteConfirmationDialog = true },
                        onTxnClick = { txnId ->
                            viewModel.selectCurrentTransaction(txnId)
                            navController.navigate(Screen.HistoryDetail.route)
                        }
                    )
                }
            )
        }
    }

    if (showDeleteConfirmationDialog) {
        CommonDialog(
            message = stringResource(R.string.confirm_delete_message),
            title = stringResource(R.string.confirm_delete_title),
            confirmText = stringResource(R.string.yes),
            cancelText = stringResource(R.string.no),
            onConfirm = {
                showDeleteConfirmationDialog = false
                viewModel.deleteCountsForSelectedDate()
            },
            onCancel = { showDeleteConfirmationDialog = false }
        )
    }
}

private fun sharePdfFile(context: Context, file: File, title: CharSequence) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    }

    val shareIntent = Intent.createChooser(intent, title)
    context.startActivity(shareIntent)
}