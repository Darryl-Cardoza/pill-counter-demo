package com.rite.pillcounting.feature.barcodeScan.presentation

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rite.pillcounting.R
import com.rite.pillcounting.core.utils.common.BarcodeDecoder
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.BackButton
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.responsiveDp
import com.rite.pillcounting.core.utils.constants.Dimens.medium
import com.rite.pillcounting.core.utils.logger.AppLogger
import com.rite.pillcounting.feature.barcodeScan.domain.data.ScanBarcodeEvent
import com.rite.pillcounting.feature.barcodeScan.domain.model.ScanBarcodeUiState
import com.rite.pillcounting.feature.barcodeScan.presentation.analyzer.BarcodeAnalyzer
import com.rite.pillcounting.feature.barcodeScan.presentation.compose.PermissionDeniedView
import com.rite.pillcounting.feature.barcodeScan.presentation.compose.ScannerView
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
    analyzer: BarcodeAnalyzer
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
            if (hasCameraPermission) {
                ScannerView(
                    analyzer = analyzer,
                    isActive = uiState.isScannerActive,
                    singleScanMode = true,
                    onBarcodeScanned = { value, imagePath ->
                        val decoder = BarcodeDecoder() // ideally injected, not recreated each scan
                        val cleanedImagePath = imagePath ?: ""

                        val isGs1 = decoder.isGs1Barcode(value)
                        val decoded = if (isGs1) decoder.decode(value) else null

                        // Normalize GTIN to GTIN-14 if possible
                        val gtin14 = decoded?.gtin?.let { decoder.toGtin14(it) }
                            ?: decoder.toGtin14(value)
                            ?: value

                        AppLogger("ScanBarcode").i("Barcode=$value | GTIN14=$gtin14 | Image=$cleanedImagePath")

                        onEvent(
                            ScanBarcodeEvent.BarcodeScanned(
                                barcodeValue = gtin14,
                                imagePath = cleanedImagePath
                            )
                        )
                    },
                    onError = { exception ->
                        onEvent(ScanBarcodeEvent.ScannerError(exception))
                    }
                )
                FocusAnimationOverlay()
            } else {
                PermissionDeniedView(onRequestPermission)
            }

            // Global Back Button
            BackButton(navController, showBox = true)

            //Manual option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .padding(medium)
                        .size(responsiveDp(40.dp))
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { onEvent(ScanBarcodeEvent.ManualPillInfo) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pencil),
                        contentDescription = "Manual",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(responsiveDp(15.dp))
                    )
                }
            }


        }
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
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
                .padding(bottom = if (isLandscape) 20.dp else 100.dp)
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
