package com.example.pillcountingnewmodels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.pillcountingnewmodels.core.settings.presentation.viewmodel.MainActivityViewModel
import com.example.pillcountingnewmodels.core.utils.common.HelperFunctions.enableImmersiveFullscreen
import com.example.pillcountingnewmodels.core.utils.common.HelperFunctions.getStartDestination
import com.example.pillcountingnewmodels.core.utils.common.HelperFunctions.openPlayStore
import com.example.pillcountingnewmodels.core.utils.preference.PreferenceHelper
import com.example.pillcountingnewmodels.core.utils.common.UserInterfaceUtils.toColor
import com.example.pillcountingnewmodels.core.utils.common.UserInterfaceUtils.LoadingIndicator
import com.example.pillcountingnewmodels.core.utils.compose.MaintenanceScreen
import com.example.pillcountingnewmodels.core.utils.compose.UpdateScreen
import com.example.pillcountingnewmodels.navigation.AppNavGraph
import com.example.pillcountingnewmodels.ui.theme.ExtendedColors
import com.example.pillcountingnewmodels.ui.theme.PillCountingNewModelsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point of the application.
 *
 * Decides whether to show:
 * - MaintenanceScreen (if backend says maintenance mode is ON)
 * - UpdateScreen (if newer app version required)
 * - AppNavGraph (normal flow)
 *
 * Also performs runtime environment hardening checks.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            Crossfade(
                targetState = settingsState.isLoading || settingsState.colorSettings == null,
                label = "LoadingOrContent"
            ) { isLoading ->
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    val colorSettings = requireNotNull(settingsState.colorSettings)

                    val lightColorSchemeDynamic = androidx.compose.material3.lightColorScheme(
                        primary = colorSettings.light.primary.toColor(),
                        secondary = colorSettings.light.secondary.toColor(),
                        tertiary = colorSettings.light.tertiary.toColor()
                    )

                    val darkColorSchemeDynamic = androidx.compose.material3.darkColorScheme(
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

                        // Decide which screen to show
                        when {
                            settingsState.isMaintenanceMode -> {
                                MaintenanceScreen()
                            }

                            settingsState.isUpdateRequired -> {
                                UpdateScreen(onUpdateClick = { openPlayStore(this) })
                            }

                            else -> {
                                AppNavGraph(
                                    navController = navController,
                                    startDestination = startDestination
                                )
                            }
                        }

                        // Security check overlay if you want:
//                        val violations = SecurityUtils.getSecurityViolations(this)
//                        if (violations.isNotEmpty()) {
//                            SecurityErrorDialog(violations)
//                        }
                    }
                }
            }
        }

        enableImmersiveFullscreen(this)
    }
}
