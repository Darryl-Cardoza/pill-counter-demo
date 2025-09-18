package com.example.pillcountingnewmodels.feature.barcodeScan.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pillcountingnewmodels.core.utils.compose.Dimens

/**
 * A composable that simulates a camera preview and provides a button to trigger a barcode scan.
 * This is useful for testing the UI and ViewModel logic without a physical camera.
 *
 * @param isScannerActive Controls whether the simulation button is enabled.
 * @param onBarcodeScanned A callback that provides the hardcoded NDC value of the simulated scan.
 * @param onError A callback for propagating exceptions (not used in simulation but kept for API consistency).
 */
@Composable
fun ScannerView(
    isScannerActive: Boolean,
    onBarcodeScanned: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera Preview (Simulated)",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        // This button simulates a successful scan.
        Button(
            onClick = {
                // Simulate scanning a valid NDC for Metformin Hydrochloride.
                onBarcodeScanned("59779-311")
                0
            },
            // The button is disabled after a scan until the user presses "Redo Scan".
            enabled = isScannerActive
        ) {
            Text("Simulate Scan")
        }

        if (!isScannerActive) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Scanner is paused.\nPress 'Redo Scan' below to enable.",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * A view shown when camera permission has not been granted.
 * @param onRequestPermission Lambda to be invoked when the button is clicked.
 */
@Composable
fun PermissionDeniedView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera permission is required to scan barcodes.", color = Color.White)
        Spacer(modifier = Modifier.height(Dimens.medium))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}
