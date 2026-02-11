package com.rite.pillcounting.feature.pillCountScan.presentation

import Screen
import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.room.models.enums.CountType
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.ActionButtonPrimary
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.BackButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.CommonDialog
import com.rite.pillcounting.core.utils.compose.SplitResponsive
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.feature.pillCountScan.domain.data.NavigationEvent
import com.rite.pillcounting.feature.pillCountScan.domain.data.PillScanningEvent
import com.rite.pillcounting.feature.pillCountScan.presentation.compose.AddNoteDialog
import com.rite.pillcounting.feature.pillCountScan.presentation.compose.CameraPreviewSection
import com.rite.pillcounting.feature.pillCountScan.presentation.compose.InformationPanelSection
import com.rite.pillcounting.feature.pillCountScan.presentation.compose.TargetPillsCountDialog
import com.rite.pillcounting.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
import com.rite.pillcounting.ui.theme.AppTheme
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
    val topHeightPortrait = maxHeight * 0.75f
    val bottomHeightPortrait = maxHeight * 0.25f
    val startWidthLandScape = maxWidth * 0.7f
    val endWidthLandscape = maxWidth * 0.3f

    // Buffer of last 10 detections
    var lastTenDetections by remember { mutableStateOf<List<Int>>(emptyList()) }

    var filteredPillCount by remember { mutableStateOf(0) }

    var previewWidth by remember { mutableStateOf<Int?>(null) }
    var previewHeight by remember { mutableStateOf<Int?>(null) }

    // Whenever detected pills update, push into buffer
    LaunchedEffect(uiState.detectedPills) {
        val currentCount = uiState.detectedPills.size
        lastTenDetections = (lastTenDetections + currentCount).takeLast(10)
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    // === Toasts ===
    if (uiState.restrictAdd) {
        UserInterfaceUtils.showToast(context, stringResource(id = R.string.max_count_reached))
        logger.w("Toast: Max count reached")
        viewModel.resetRestrictAdd()
    }

    if (uiState.showNoTransaction) {
        UserInterfaceUtils.showToast(context, stringResource(id = R.string.no_transaction_found))
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

    // === Init & Navigation ===
    LaunchedEffect(Unit) {
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
//            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        viewModel.resetIdleTimer()
                    }
                }
            }
    ) {
        // Camera + Info
        SplitResponsive(
            topOrLeft = {
                CameraPreviewSection(
                    viewModel = viewModel,
                    pills = uiState.detectedPills,
                    isCameraPaused = viewModel.cameraPaused.collectAsState().value,
                    onFrame = { imageProxy ->
                        viewModel.onFrameCaptured(imageProxy)
                    },
                    onFilteredCountChanged = { count -> filteredPillCount = count },
                    modifier = Modifier.fillMaxSize(),

                    imageFrameWidth = uiState.imageFrameWidth,
                    imageFrameHeight = uiState.imageFrameHeight,

                    onPreviewSizeKnown = { w, h ->
                        // Store once
                        if (previewWidth == null || previewHeight == null) {
                            previewWidth = w
                            previewHeight = h

                            viewModel.initializeInterpreter(
                                retryCount = 2,
                                previewWidth = w,
                                previewHeight = h
                            )
                        }
                    }
                )
            },
            bottomOrRight = {
                InformationPanelSection(
                    uiState = uiState,
                    viewModel = viewModel,
                    onEvent = viewModel::onEvent,
                    filteredPillCount = filteredPillCount
                )
            },
            landscapeRatio = startWidthLandScape to endWidthLandscape,
            portraitRatio = topHeightPortrait to bottomHeightPortrait
        )

        if (!uiState.showIdleOverlay) {
            BackButton(navController) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            if (!isLandscape) {
                Text(
                    text = stringResource(R.string.pills_count).uppercase(Locale.ROOT),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    color = AppTheme.extendedColors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .fillMaxWidth()
                )
            }
        }
        // === Overlay placed last → ensures it is on top ===
        if (uiState.showIdleOverlay) {
            logger.i("Overlay visible")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.extendedColors.secondaryBackground.copy(alpha = 0.5f))
                    .pointerInput(Unit) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.counting_paused).uppercase(Locale.ROOT),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        color = AppTheme.extendedColors.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ActionButtonPrimary(
                        text = stringResource(R.string.resume).uppercase(Locale.ROOT),
                        onClick = { viewModel.resetIdleOverlay() },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}
