package com.example.pillcountingnewmodels.feature.barcodeScan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.AppLogger
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.LoadingIndicator
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.example.pillcountingnewmodels.feature.barcodeScan.domain.model.ScanBarcodeUiState
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.InformationPanel
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.PermissionDeniedView
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose.ScannerView

/**
 * The stateless presentation component for the barcode scanning screen. It is responsible for
 * laying out the UI based on the provided state and forwarding user events.
 *
 * @param navController The navigation controller.
 * @param uiState The current state of the UI to be displayed.
 * @param hasCameraPermission Whether the camera permission has been granted.
 * @param onRequestPermission Lambda to request camera permission.
 * @param onEvent A lambda to call when a user action occurs.
 */
@Composable
fun ScanBarCodeScreenContent(
    navController: NavController,
    uiState: ScanBarcodeUiState,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onEvent: (ScanBarcodeEvent) -> Unit
) {
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
                            isScannerActive = uiState.isScannerActive,
                            onBarcodeScanned = { value, imagePath ->
                                onEvent(
                                    ScanBarcodeEvent.BarcodeScanned(
                                        barcodeValue = value,
                                        imagePath = imagePath
                                    )
                                )
                                AppLogger("ScanBarcode").i("Barcode=$value, saved image=$imagePath")
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

                        // Show loading indicator as an overlay when isLoading is true
                        if (uiState.isLoading) {
                            LoadingIndicator()
                        }
                    }
                }
            )

            BackButton(navController)

        }
    }
}
