package com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose.CameraPreviewSection
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.compose.InformationPanelSection
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.viewmodel.FixedCountPillScanningViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The main composable for the Fixed Count Pill Scanning screen.
 * It orchestrates the layout and state management for the feature.
 */
@Composable
fun FixedCountPillScanningScreen(
    navController: NavController,
    viewModel: FixedCountPillScanningViewModel = hiltViewModel()
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
                when (val state = modelState) {
                    is FixedCountPillScanningViewModel.ModelState.Idle,
                    is FixedCountPillScanningViewModel.ModelState.Loading -> {
                        // Loading spinner
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is FixedCountPillScanningViewModel.ModelState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Model failed: ${state.message}")
                        }
                    }

                    is FixedCountPillScanningViewModel.ModelState.Ready -> {
                        // Show camera preview and analyze frames
                        CameraPreviewSection(
                            pills = uiState.detectedPills,
                            modifier = Modifier,
                            onFrame = { imageProxy ->
                                coroutineScope.launch(Dispatchers.Default) {
                                    val analyzer = state.analyzer
                                    val bitmap = viewModel.imageProxyToBitmap(imageProxy)
                                    val results = analyzer.analyzeFrame(bitmap)
                                    viewModel.updateDetectedPills(results)
                                    imageProxy.close()
                                }
                            }
                        )
                    }
                }
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
    }
}
