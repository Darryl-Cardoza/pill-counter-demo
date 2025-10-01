package com.example.pillcountingnewmodels.feature.pillCountScan.presentation

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.room.models.enums.CountType
import com.example.pillcountingnewmodels.core.utils.ToastUtils
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.CommonDialog
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.FixedCountPillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.CameraPreviewSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.InformationPanelSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.TargetPillsCountDialog
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * The main composable for the Fixed Count Pill Scanning screen.
 * It orchestrates the layout and state management for the feature.
 */
@Composable
fun PillScanningScreen(
    navController: NavController,
    countType: String,
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
            countType == CountType.FIXED.toString() && uiState.txnDetailHistory.sumOf { it.count } < uiState.targetCount
        ) {
            stringResource(R.string.confirm_done_desc_fixed)
        } else {
            stringResource(R.string.confirm_done_desc_regular)
        }

        CommonDialog(
            message = warningText,
            title = stringResource(R.string.confirm_done),
            confirmText = stringResource(R.string.ok),
            cancelText = stringResource(R.string.cancel),
            onConfirm = {
                viewModel.onEvent(FixedCountPillScanningEvent.ConfirmDone)
            },
            onCancel = { viewModel.onEvent(FixedCountPillScanningEvent.CancelDone) }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.initializeInterpreter(retryCount = 2)
        viewModel.showTxnInfo()
        viewModel.observeTxnDetailsForTxn(countType)

        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToDashboard -> {
                    navController.navigate(Screen.Dashboard.route)
                }
            }
        }
    }

    LaunchedEffect(countType) {
        viewModel.setScanType(countType)
    }

    LaunchedEffect(uiState.showTargetCountDialog) {
        if (uiState.showTargetCountDialog) {
            showTargetCountDialog = true
        }
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
            navController.popBackStack()
        })
    }

    if (showTargetCountDialog) {
        TargetPillsCountDialog(
            onDismiss = {
                showTargetCountDialog = false
                navController.popBackStack()
            },
            onOkay = { count ->
                viewModel.updateTargetCount(count)
                showTargetCountDialog = false
            }
        )
    }
}

