package com.example.pillcountingnewmodels.feature.barcodeScan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.logger.AppLogger
import com.example.pillcountingnewmodels.core.utils.common.UserInterfaceUtils.BackButton
import com.example.pillcountingnewmodels.core.utils.common.UserInterfaceUtils.LoadingIndicator
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.model.ScanBarcodeUiState
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.analyzer.BarcodeAnalyzer
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.InformationPanel
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.PermissionDeniedView
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.ScannerView

/**
 * Stateless composable for the barcode scanning screen.
 * Handles lifecycle, permissions, and analyzer interaction.
 */
@Composable
fun ScanBarCodeScreenContent(
    navController: NavController,
    uiState: ScanBarcodeUiState,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onEvent: (ScanBarcodeEvent) -> Unit,
    analyzer: BarcodeAnalyzer
) {
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    // Automatically pause/resume camera based on lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> analyzer.resume()
                Lifecycle.Event.ON_PAUSE -> analyzer.pause()
                Lifecycle.Event.ON_DESTROY -> analyzer.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            SplitResponsive(
                portraitRatio = 0.60f to 0.40f,
                landscapeRatio = 0.60f to 0.40f,
                topOrLeft = {
                    if (hasCameraPermission) {
                        ScannerView(
                            analyzer = analyzer,
                            isActive = uiState.isScannerActive,
                            singleScanMode = true,
                            onBarcodeScanned = { value, imagePath ->
                                onEvent(
                                    ScanBarcodeEvent.BarcodeScanned(
                                        barcodeValue = value,
                                        imagePath = imagePath ?: ""
                                    )
                                )
                                AppLogger("ScanBarcode").i(
                                    "Barcode=$value | Image=$imagePath"
                                )
                            },
                            onError = { exception ->
                                onEvent(ScanBarcodeEvent.ScannerError(exception))
                            }
                        )
                    } else {
                        PermissionDeniedView(onRequestPermission)
                    }
                },
                bottomOrRight = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        InformationPanel(
                            navController = navController,
                            drugName = uiState.drugName,
                            ndc = uiState.ndc,
                            onEvent = onEvent
                        )

                        // Overlay loading indicator if needed
                        if (uiState.isLoading) {
                            LoadingIndicator()
                        }
                    }
                }
            )

            // Global Back Button
            BackButton(navController)
        }
    }
}
