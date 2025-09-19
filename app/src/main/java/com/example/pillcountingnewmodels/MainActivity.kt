package com.example.pillcountingnewmodels

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.core.utils.compose.HelperFunctions.getStartDestination
import com.example.pillcountingnewmodels.core.utils.toColor
import com.example.pillcountingnewmodels.feature.settings.presentation.viewmodel.ApplicationSettingsViewModel
import com.example.pillcountingnewmodels.navigation.AppNavGraph
import com.example.pillcountingnewmodels.ui.theme.ExtendedColors
import com.example.pillcountingnewmodels.ui.theme.PillCountingNewModelsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main entry point of the application.
 *
 * This activity is responsible for setting up the window, initializing the UI content with Jetpack Compose,
 * and providing the dynamic theme based on fetched settings. Permission handling and navigation logic
 * are delegated to the appropriate composable screens within the `AppNavGraph`.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: ApplicationSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Jetpack Compose UI.
        setContent {
            // Collect the UI state from the ViewModel in a lifecycle-aware manner.
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            // Use Crossfade for a smooth transition between loading and content states.
            Crossfade(
                targetState = settingsState.isLoading || settingsState.colorSettings == null,
                label = "LoadingOrContent"
            ) { isLoading ->
                if (isLoading) {
                    // Display a loading indicator while settings are being fetched.
                    LoadingScreen()
                } else {
                    // Once settings are available, build and apply the dynamic theme.
                    val colorSettings = requireNotNull(settingsState.colorSettings)

                    val lightColorSchemeDynamic = lightColorScheme(
                        primary = colorSettings.light.primary.toColor(),
                        secondary = colorSettings.light.secondary.toColor(),
                        tertiary = colorSettings.light.tertiary.toColor()
                    )

                    val darkColorSchemeDynamic = darkColorScheme(
                        primary = colorSettings.dark.primary.toColor(),
                        secondary = colorSettings.dark.secondary.toColor(),
                        tertiary = colorSettings.dark.tertiary.toColor()
                    )

                    val extendedDynamicLight = ExtendedColors(
                        primaryBackground = colorSettings.light.primaryBackground.toColor(),
                        secondaryBackground = colorSettings.light.secondaryBackground.toColor(),
                        textColor = colorSettings.light.textColor.toColor(),
                        inputBackground = colorSettings.light.inputBackground.toColor(),
                        statusChipBackgroundOnPrimary = colorSettings.light.statusChipBackgroundOnPrimary.toColor(),
                        statusChipBackgroundOnSecondary = colorSettings.light.statusChipBackgroundOnSecondary.toColor()
                    )

                    val extendedDynamicDark = ExtendedColors(
                        primaryBackground = colorSettings.dark.primaryBackground.toColor(),
                        secondaryBackground = colorSettings.dark.secondaryBackground.toColor(),
                        textColor = colorSettings.dark.textColor.toColor(),
                        inputBackground = colorSettings.dark.inputBackground.toColor(),
                        statusChipBackgroundOnPrimary = colorSettings.dark.statusChipBackgroundOnPrimary.toColor(),
                        statusChipBackgroundOnSecondary = colorSettings.dark.statusChipBackgroundOnSecondary.toColor()
                    )

                    PillCountingNewModelsTheme(
                        lightColors = lightColorSchemeDynamic,
                        darkColors = darkColorSchemeDynamic,
                        lightExtendedColors = extendedDynamicLight,
                        darkExtendedColors = extendedDynamicDark
                    ) {
                        val navController = rememberNavController()
                        val preferenceHelper = remember { PreferenceHelper(this) }
                        val startDestination = remember { getStartDestination(preferenceHelper) }

                        AppNavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }

        // Set the app to be fullscreen and immersive AFTER the content view is set.
//        configureImmersiveFullscreen()
    }

    /**
     * Configures the window to hide system bars for a fully immersive experience.
     * This is the recommended approach for a true fullscreen application.
     */
    private fun configureImmersiveFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}

/**
 * A simple composable for displaying a centered loading indicator.
 * This keeps the main `setContent` block cleaner.
 */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}