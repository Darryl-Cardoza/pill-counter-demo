package com.example.pillcountingnewmodels.feature.pillCountScan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.MenuButton
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.CameraPreviewSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.compose.InformationPanelSection
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.viewmodel.PillScanningViewModel
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
    val uiState by viewModel.uiState.collectAsState()
    val modelState by viewModel.modelState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Kick off model initialization once
    LaunchedEffect(Unit) {
        viewModel.initializeInterpreter(retryCount = 2)
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

        BackButton(navController)
        MenuButton(
            navController,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

