package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.analyzer.BarcodeAnalyzer

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

    // Remember a single instance of PreviewView across recompositions
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Initialize camera and analyzer when the view enters composition
    LaunchedEffect(Unit) {
        analyzer.start(
            previewView = previewView,
            lifecycleOwner = lifecycleOwner,
            singleScanMode = singleScanMode,
            onBarcodeDetected = onBarcodeScanned,
            onError = onError
        )
    }

    // Pause / Resume logic driven by isActive state
    LaunchedEffect(isActive) {
        if (isActive) {
            analyzer.resume()
        } else {
            analyzer.pause()
        }
    }

    // Render the camera preview
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { previewView },
        update = { /* No-op: handled by analyzer */ }
    )
}
