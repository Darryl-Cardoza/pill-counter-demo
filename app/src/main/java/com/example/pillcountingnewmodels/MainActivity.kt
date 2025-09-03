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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.pillcountingnewmodels.core.utils.toColor
import com.example.pillcountingnewmodels.navigation.AppNavGraph
import com.example.pillcountingnewmodels.navigation.Routes
import com.example.pillcountingnewmodels.ui.theme.ExtendedColors
import com.example.pillcountingnewmodels.ui.theme.PillCountingNewModelsTheme
import com.example.pillcountingnewmodels.viewmodel.PillViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ViewModel scoped to Activity lifecycle
    private val pillViewModel: PillViewModel by viewModels()

    private val primaryColorLight = "#01BBD3"
    private val secondaryColorLight = "#FD82B5"
    private val primaryBackgroundLight = "#EDEEEE"
    private val secondaryBackgroundLight = "#FFFFFF"
    private val textColorLight = "#666666"
    private val inputBackgroundLight = "#FFFFFF"


    private val primaryColorDark = "#01BBD3"
    private val secondaryColorDark = "#FD82B5"
    private val primaryBackgroundDark = "#333333"
    private val secondaryBackgroundDark = "#191919"
    private val textColorDark = "#EDEEEE"
    private val inputBackgroundDark = "#191919"


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

            val lightColorSchemeDynamic = remember {
                lightColorScheme(
                    primary = primaryColorLight.toColor(),
                    secondary = secondaryColorLight.toColor()
                )
            }

            val darkColorSchemeDynamic = remember {
                darkColorScheme(
                    primary = primaryColorDark.toColor(),
                    secondary = secondaryColorDark.toColor()
                )
            }

            val extendedDynamicLight = ExtendedColors(
                primaryBackground = primaryBackgroundLight.toColor(),  // your light bg
                secondaryBackground = secondaryBackgroundLight.toColor(),
                textColor = textColorLight.toColor(),
                inputBackground = inputBackgroundLight.toColor()

            )
            val extendedDynamicDark = ExtendedColors(
                primaryBackground = primaryBackgroundDark.toColor(),  // your light bg
                secondaryBackground = secondaryBackgroundDark.toColor(),
                textColor = textColorDark.toColor(),
                inputBackground = inputBackgroundDark.toColor()
            )


            PillCountingNewModelsTheme(
                lightColors = lightColorSchemeDynamic,
                darkColors = darkColorSchemeDynamic,
                lightExtendedColors = extendedDynamicLight,
                darkExtendedColors = extendedDynamicDark
            ) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
                navController.navigate(Routes.LOGIN)
                //HomeScreen(pillViewModel = pillViewModel)
            }
        }
    }
}
