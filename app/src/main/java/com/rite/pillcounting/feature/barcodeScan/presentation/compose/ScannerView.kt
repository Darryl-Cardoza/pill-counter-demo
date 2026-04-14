package com.rite.pillcounting.feature.barcodeScan.presentation.compose

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.rite.pillcounting.feature.barcodeScan.presentation.FocusAnimationOverlay
import com.rite.pillcounting.feature.barcodeScan.presentation.analyzer.BarcodeAnalyzer

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerView(
    analyzer: BarcodeAnalyzer,
    isActive: Boolean,
    singleScanMode: Boolean = true,
    onBarcodeScanned: (String, String?) -> Unit,
    onError: (Exception) -> Unit
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        analyzer.start(
            previewView = previewView,
            lifecycleOwner = lifecycleOwner,
            singleScanMode = singleScanMode,
            onBarcodeDetected = onBarcodeScanned,
            onError = onError
        )
        onDispose {
        }
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            analyzer.resume()
        } else {
            analyzer.pause()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { previewView }
    )

    FocusAnimationOverlay()
}