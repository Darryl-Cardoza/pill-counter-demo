package com.example.pillcountingnewmodels.feature.pillCountScan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.room.models.CountType
import com.example.pillcountingnewmodels.core.utils.ToastUtils
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.CommonDialog
import com.example.pillcountingnewmodels.core.utils.compose.MenuButton
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.FixedCountPillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.CameraPreviewSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.InformationPanelSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.TargetPillsCountDialog
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import com.example.pillcountingnewmodels.navigation.AUTH_GRAPH_ROUTE
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * The main composable for the Fixed Count Pill Scanning screen.
 * It orchestrates the layout and state management for the feature.
 */
@Composable
fun PillScanningScreen(
    navController: NavController,
    scanType: String,
    viewModel: PillScanningViewModel = hiltViewModel()
) {
    val context = navController.context
    var showTargetCountDialog by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    if (uiState.restrictAdd) {
        ToastUtils.show(context, stringResource(id = R.string.max_count_reached))
        viewModel.resetRestrictAdd()
    }
    if (uiState.showNoTransaction) {
        ToastUtils.show(context, stringResource(id = R.string.no_transaction_found))
        viewModel.resetNoTransaction()
    }
    if (uiState.showConfirmDialog) {
        val warningText = if (
            scanType == CountType.FIXED.toString() && uiState.txnDetailHistory.sumOf { it.count } < uiState.targetCount
        ) {
            stringResource(R.string.confirm_done_desc_fixed)
        } else {
            stringResource(R.string.confirm_done_desc_regular)
        }

        AlertDialog(
            onDismissRequest = { viewModel.onEvent(FixedCountPillScanningEvent.CancelDone) },
            title = { Text(stringResource(R.string.confirm_done)) },
            text = { Text(warningText) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(FixedCountPillScanningEvent.ConfirmDone) }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(FixedCountPillScanningEvent.CancelDone) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
        /*CommonDialog(
            message = stringResource(R.string.confirm_exit_message),
            title = stringResource(R.string.confirm_exit_title),
            confirmText = stringResource(R.string.yes),
            cancelText = stringResource(R.string.no),
            onConfirm = {
                showExitConfirmationDialog = false
                navController.popBackStack()
            },
            onCancel = { showExitConfirmationDialog = false }
        )*/
    }

    LaunchedEffect(Unit) {
        viewModel.initializeInterpreter(retryCount = 2)
        viewModel.showDrugName()

        if (scanType == CountType.FIXED.toString() && uiState.txnDetailHistory.isEmpty()) {
            /*uiState.txnDetailHistory.isEmpty() confirms that user has just created the transaction
            and we should ask for target count if its count type is FIXED*/
            showTargetCountDialog = true
        }

        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToDashboard -> {
                    navController.navigate(Screen.Dashboard.route)
                }
            }
        }
    }

    LaunchedEffect(scanType) {
        viewModel.setScanType(scanType)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        SplitResponsive(
            topOrLeft = {
                // Camera preview with overlays
                CameraPreviewSection(
                    pills = uiState.detectedPills,
                    onFrame = { imageProxy ->
                        viewModel.onFrameCaptured(imageProxy)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            },
            bottomOrRight = {
                InformationPanelSection(
                    navController = navController,
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            },
            landscapeRatio = 0.6f to 0.4f,
            portraitRatio = 0.5f to 0.5f
        )

        BackButton(navController, onClick = {
            navController.navigate(Screen.Dashboard.route)
        })
        MenuButton(
            navController,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }

    if (showTargetCountDialog) {
        TargetPillsCountDialog(
            onDismiss = { showTargetCountDialog = false },
            onOkay = { count ->
                viewModel.updateTargetCount(count)
                showTargetCountDialog = false
            }
        )
    }
}

