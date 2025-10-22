package com.rite.pillcounting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.rite.pillcounting.core.settings.presentation.viewmodel.MainActivityViewModel
import com.rite.pillcounting.core.utils.common.HelperFunctions.enableImmersiveFullscreen
import com.rite.pillcounting.core.utils.common.HelperFunctions.getStartDestination
import com.rite.pillcounting.core.utils.common.HelperFunctions.openPlayStore
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.LoadingIndicator
import com.rite.pillcounting.core.utils.common.UserInterfaceUtils.toColor
import com.rite.pillcounting.core.utils.compose.MaintenanceScreen
import com.rite.pillcounting.core.utils.compose.UpdateScreen
import com.rite.pillcounting.core.utils.notification.FCMService
import com.rite.pillcounting.core.utils.preference.PreferenceHelper
import com.rite.pillcounting.navigation.AppNavGraph
import com.rite.pillcounting.ui.theme.ExtendedColors
import com.rite.pillcounting.ui.theme.PillCountingNewModelsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject
    lateinit var fcmService: FCMService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fcmService.initFCM()
        fcmService.subscribeToTopic("global_updates")

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
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
        }
    }
}


