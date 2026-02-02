package com.rite.pillcounting.feature.barcodeScan.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.BarcodeDecoder
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.BackButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.responsiveDp
import com.rite.pillcounting.core.utils.compose.SplitResponsive
import com.rite.pillcounting.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.rite.pillcounting.feature.barcodeScan.domain.model.ScanBarcodeUiState
import com.rite.pillcounting.feature.barcodeScan.presentation.analyzer.BarcodeAnalyzer
import com.rite.pillcounting.feature.barcodeScan.presentation.compose.ManualDrugInfo
import com.rite.pillcounting.feature.barcodeScan.presentation.compose.ScannerView
import com.rite.pillcounting.feature.barcodeScan.presentation.viewmodel.ScanBarcodeViewModel
import com.rite.pillcounting.ui.theme.AppTheme

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
    analyzer: BarcodeAnalyzer,
    viewModel: ScanBarcodeViewModel = hiltViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        SplitResponsive(
            topOrLeft = {
                ScannerView(
                    analyzer = analyzer,
                    isActive = uiState.isScannerActive,
                    singleScanMode = true,
                    onBarcodeScanned = { value, imagePath ->
                        val decoder = BarcodeDecoder()
                        val cleanedImagePath = imagePath ?: ""

                        val isGs1 = decoder.isGs1Barcode(value)
                        val decoded = if (isGs1) decoder.decode(value) else null

                        val gtin14 = decoded?.gtin?.let { decoder.toGtin14(it) }
                            ?: decoder.toGtin14(value)
                            ?: value

                        onEvent(
                            ScanBarcodeEvent.BarcodeScanned(
                                gtin14 = gtin14,
                                imagePath = cleanedImagePath,
                                expiry = if (isGs1) decoded?.expirationDate.toString() else "",
                                lotNo = if (isGs1) decoded?.lotNumber ?: "" else ""
                            )
                        )
                    },
                    onError = { exception -> onEvent(ScanBarcodeEvent.ScannerError(exception)) }
                )
            },
            bottomOrRight = {
                ManualDrugInfo(
                    onConfirm = { drugName, ndc -> viewModel.addManualDrug(drugName, ndc) },
                    onDismiss = { navController.popBackStack() },
                )
            },
            landscapeRatio = 0.65f to 0.35f,
            portraitRatio = 0.70f to 0.30f
        )
        BackButton(navController, showBox = false)
    }

}

@Composable
fun FocusAnimationOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "focusPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(responsiveDp(140.dp))
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(
                    color = Color.Transparent,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AppTheme.extendedColors.primaryBackground,
                            AppTheme.extendedColors.secondaryBackground
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.scan_code),
                color = AppTheme.extendedColors.textColor,
                fontSize = 16.sp,
            )
        }
    }
}
