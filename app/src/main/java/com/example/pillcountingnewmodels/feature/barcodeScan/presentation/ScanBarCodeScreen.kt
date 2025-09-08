package com.example.pillcountingnewmodels.feature.barcodeScan.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.feature.barcodeScan.viewmodel.ScanBarcodeViewModel

/**
 * A stateful composable that manages the logic for camera permissions and collects state
 * from the ViewModel. It serves as the entry point for the barcode scanning screen.
 *
 * @param navController The navigation controller for handling screen transitions.
 * @param viewModel The ViewModel that holds the business logic and state for this screen.
 */
@Composable
fun ScanBarCodeScreen(
    navController: NavController,
    viewModel: ScanBarcodeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // Request camera permission when the composable is first launched if not already granted.
    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Collect the UI state from the ViewModel in a lifecycle-aware manner.
    val uiState by viewModel.uiState.collectAsState()

    // Delegate the UI rendering to the stateless content composable.
    ScanBarCodeScreenContent(
        navController = navController,
        uiState = uiState,
        hasCameraPermission = hasCameraPermission,
        onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onEvent = viewModel::onEvent
    )
}

