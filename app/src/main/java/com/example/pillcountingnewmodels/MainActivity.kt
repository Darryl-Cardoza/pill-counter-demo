package com.example.pillcountingnewmodels

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.provider.Settings
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.pillcountingnewmodels.core.api.viewmodel.ApplicationSettingsViewModel
import com.example.pillcountingnewmodels.core.utils.PreferenceHelper
import com.example.pillcountingnewmodels.core.utils.compose.CommonDialog
import com.example.pillcountingnewmodels.core.utils.compose.HelperFunctions.getStartDestination
import com.example.pillcountingnewmodels.core.utils.compose.MaintenanceScreen
import com.example.pillcountingnewmodels.core.utils.compose.UpdateScreen
import com.example.pillcountingnewmodels.core.utils.toColor
import com.example.pillcountingnewmodels.navigation.AppNavGraph
import com.example.pillcountingnewmodels.ui.theme.ExtendedColors
import com.example.pillcountingnewmodels.ui.theme.PillCountingNewModelsTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.system.exitProcess

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

    private val settingsViewModel: ApplicationSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            Crossfade(
                targetState = settingsState.isLoading || settingsState.colorSettings == null,
                label = "LoadingOrContent"
            ) { isLoading ->
                if (isLoading) {
                    LoadingScreen()
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
                                UpdateScreen(
                                    onUpdateClick = {
                                        val appPackageName = packageName // your app's package
                                        try {
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    "market://details?id=$appPackageName".toUri()
                                                )
                                            )
                                        } catch (e: android.content.ActivityNotFoundException) {
                                            // fallback if Play Store app not installed
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
                                                )
                                            )
                                        }
                                    }
                                )
                            }

                            else -> {
                                AppNavGraph(
                                    navController = navController,
                                    startDestination = startDestination
                                )
                            }
                        }

                        // Security check overlay if you want:
                        // val violations = getSecurityViolations()
                        // if (violations.isNotEmpty()) {
                        //     SecurityErrorDialog(violations)
                        // }
                    }
                }
            }
        }

        configureImmersiveFullscreen()
    }

    /**
     * Collects runtime environment violations.
     *
     * @return a list of violation messages, empty if no violations.
     */
    private fun getSecurityViolations(): List<String> {
        val violations = mutableListOf<String>()

        if (isAdbEnabled()) violations.add(getString(R.string.violation_adb_enabled))
        if (isDeviceRooted()) violations.add(getString(R.string.violation_rooted))
        if (isDebuggerAttached()) violations.add(getString(R.string.violation_debugger_attached))
        if (isRunningOnEmulator()) violations.add(getString(R.string.violation_emulator))
        if (isAppDebuggable()) violations.add(getString(R.string.violation_debuggable_build))
        if (!isSignatureValid()) violations.add(getString(R.string.violation_signature_mismatch))
        if (!isFromPlayStore()) violations.add(getString(R.string.violation_not_from_playstore))

        return violations
    }


    /** Developer options enabled (ADB debugging). */
    private fun isAdbEnabled(): Boolean {
        return Settings.Global.getInt(
            contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) == 1
    }

    /** Basic root detection by checking for `su` binary. */
    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }

    /** Detects if the app is being debugged. */
    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /** Emulator detection (basic heuristics). */
    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                "google_sdk" == Build.PRODUCT)
    }

    /** Check if the app is debuggable (should be false in production). */
    private fun isAppDebuggable(): Boolean {
        return applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    /** Verify the app signature (replace with your release signature hash). */
    private fun isSignatureValid(): Boolean {
        return try {
            val pm = packageManager
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            }

            val validHash = "YOUR_RELEASE_SIGNATURE_HASH"
            signatures?.any { sig ->
                sig.toCharsString().hashCode().toString() == validHash
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    /** Verify installation source is Google Play. */
    @Suppress("DEPRECATION")
    private fun isFromPlayStore(): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(packageName)
            }
            installer == "com.android.vending"
        } catch (e: Exception) {
            false
        }
    }

    /** Configure fullscreen immersive mode. */
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

/** Security error dialog shown on violations. */
@Composable
fun SecurityErrorDialog(violations: List<String>) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val title = stringResource(R.string.security_alert_title)
    val confirmText = stringResource(R.string.exit_app)
    val cancelText = stringResource(R.string.close_app)

    val message = buildString {
        append(stringResource(R.string.security_violation_intro))
        append("\n\n")
        violations.forEach { append("• $it\n") }
    }

    CommonDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        cancelText = cancelText,
        onConfirm = { exitApp() },
        onCancel = { exitApp() }
    )
}


/** Terminates the app. */
private fun exitApp() {
    android.os.Process.killProcess(android.os.Process.myPid())
    exitProcess(1)
}

/** Loading screen composable. */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
