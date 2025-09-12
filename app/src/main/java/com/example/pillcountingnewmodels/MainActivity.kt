package com.example.pillcountingnewmodels

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.pillcountingnewmodels.feature.settings.presentation.viewmodel.ApplicationSettingsViewModel
import com.example.pillcountingnewmodels.navigation.AppNavGraph
import com.example.pillcountingnewmodels.ui.theme.ExtendedColors
import com.example.pillcountingnewmodels.ui.theme.PillCountingNewModelsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject the settings ViewModel
    private val settingsViewModel: ApplicationSettingsViewModel by viewModels()

    // Permission launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startApp()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE, // status bar color
                android.graphics.Color.BLACK  // icon color (for contrast)
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.BLACK
            )
        )

        /* Hide status and navigation bars - Fullscreen app */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startApp()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationale()
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("This app needs access to your camera to count pills accurately.")
            .setPositiveButton("Allow") { _, _ ->
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Camera permission was denied. Please enable it in settings to continue.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun startApp() {
        setContent {
            // Collect UI state from the ViewModel in a lifecycle-aware way
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val colorSettings = settingsState.colorSettings

            // Show a loading indicator while fetching settings or if settings are null
            if (settingsState.isLoading || colorSettings == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Once settings are loaded, build the theme dynamically
                val lightColorSchemeDynamic = lightColorScheme(
                    primary = colorSettings.light.primary,
                    secondary = colorSettings.light.secondary
                )

                val darkColorSchemeDynamic = darkColorScheme(
                    primary = colorSettings.dark.primary,
                    secondary = colorSettings.dark.secondary
                )

                val extendedDynamicLight = ExtendedColors(
                    primaryBackground = colorSettings.light.primaryBackground,
                    secondaryBackground = colorSettings.light.secondaryBackground,
                    textColor = colorSettings.light.textColor,
                    inputBackground = colorSettings.light.inputBackground,
                    statusChipBackgroundOnPrimary = colorSettings.light.statusChipBackgroundOnPrimary,
                    statusChipBackgroundOnSecondary = colorSettings.light.statusChipBackgroundOnSecondary
                )
                val extendedDynamicDark = ExtendedColors(
                    primaryBackground = colorSettings.dark.primaryBackground,
                    secondaryBackground = colorSettings.dark.secondaryBackground,
                    textColor = colorSettings.dark.textColor,
                    inputBackground = colorSettings.dark.inputBackground,
                    statusChipBackgroundOnPrimary = colorSettings.dark.statusChipBackgroundOnPrimary,
                    statusChipBackgroundOnSecondary = colorSettings.dark.statusChipBackgroundOnSecondary
                )

                PillCountingNewModelsTheme(
                    lightColors = lightColorSchemeDynamic,
                    darkColors = darkColorSchemeDynamic,
                    lightExtendedColors = extendedDynamicLight,
                    darkExtendedColors = extendedDynamicDark
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)

                    navController.navigate(Screen.Login.route)
                }
            }
        }
    }
}