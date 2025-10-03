package com.example.pillcountingnewmodels.feature.pillCountScan.presentation

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.room.models.enums.CountType
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.ToastUtils
import com.example.pillcountingnewmodels.core.utils.compose.ActionButtonPrimary
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.CommonDialog
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.NavigationEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.domain.data.PillScanningEvent
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.AddNoteDialog
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.CameraPreviewSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.InformationPanelSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.TargetPillsCountDialog
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@Composable
fun PillScanningScreen(
    navController: NavController,
    countType: String,
    viewModel: PillScanningViewModel = hiltViewModel()
) {
    val context = navController.context

    val uiState by viewModel.uiState.collectAsState()

    val logger = remember { AppLogger("PillScanningScreen") }

    // Buffer of last 10 detections
    var lastTenDetections by remember { mutableStateOf<List<Int>>(emptyList()) }

    // Whenever detected pills update, push into buffer
    LaunchedEffect(uiState.detectedPills) {
        val currentCount = uiState.detectedPills.size
        lastTenDetections = (lastTenDetections + currentCount).takeLast(10)
    }

    // Check if last 10 counts are all zero
    val isLastTenAllZero = lastTenDetections.size == 10 && lastTenDetections.all { it == 0 }

    // === Toasts ===
    if (uiState.restrictAdd) {
        ToastUtils.show(context, stringResource(id = R.string.max_count_reached))
        logger.w("Toast: Max count reached")
        viewModel.resetRestrictAdd()
    }

    if (uiState.showNoTransaction) {
        ToastUtils.show(context, stringResource(id = R.string.no_transaction_found))
        logger.w("Toast: No transaction found")
        viewModel.resetNoTransaction()
    }

    if (uiState.showTargetCountDialog) {
        TargetPillsCountDialog(
            onDismiss = {
                viewModel.setTargetCountDialogShown(false)
                navController.popBackStack()
            },
            onOkay = { count ->
                viewModel.updateTargetCount(count)
                viewModel.setTargetCountDialogShown(false)
            }
        )
    }

    if (uiState.showNotesDialog) {
        AddNoteDialog(
            onDismiss = { viewModel.setNoteDialogShown(false) },
            onSkip = {
                viewModel.onEvent(PillScanningEvent.NoteSkip)
            },
            onSave = { note ->
                viewModel.onEvent(PillScanningEvent.NoteSaved(note))
            }
        )
    }

    // --- Confirm Dialog ---
    if (uiState.showConfirmDialog) {
        val warningText = if (
            countType == CountType.FIXED.toString() &&
            uiState.txnDetailHistory.sumOf { it.count } < uiState.targetCount
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
            onConfirm = { viewModel.onEvent(PillScanningEvent.ConfirmDone) },
            onCancel = { viewModel.onEvent(PillScanningEvent.CancelDone) }
        )
    }

    // --- Overlay Logic ---
    val shouldShowOverlay =
        uiState.showIdleOverlay || (countType == "15" && isLastTenAllZero)

    if (shouldShowOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.extendedColors.primaryBackground.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            ActionButtonPrimary(
                text = stringResource(R.string.resume),
                onClick = {
                    viewModel.resetIdleOverlay()
                    lastTenDetections = emptyList() // reset buffer after resume
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // === Init & Navigation ===
    LaunchedEffect(Unit) {
        viewModel.initializeInterpreter(retryCount = 2)
        viewModel.showTxnInfo(countType)
        viewModel.observeTxnDetailsForTxn()

        viewModel.navigationEvent.collectLatest { event ->
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

    // --- Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        // Camera + Info
        SplitResponsive(
            topOrLeft = {
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

        BackButton(navController) { navController.popBackStack() }

        // === Overlay placed last → ensures it is on top ===
        if (uiState.showIdleOverlay) {
            logger.i("Overlay visible")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.extendedColors.secondaryBackground.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                ActionButtonPrimary(
                    text = stringResource(R.string.resume).toUpperCase(Locale.ROOT),
                    onClick = {
                        viewModel.resetIdleOverlay()
                    },
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
