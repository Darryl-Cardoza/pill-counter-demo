package com.example.pillcountingnewmodels.feature.dashboard.presentation

import Screen
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.preference.PreferenceHelper
import com.example.pillcountingnewmodels.core.utils.common.UserInterfaceUtils.CommonDialog
import com.example.pillcountingnewmodels.core.utils.common.UserInterfaceUtils.MenuButton
import com.example.pillcountingnewmodels.core.utils.compose.SplitResponsive
import com.example.pillcountingnewmodels.feature.dashboard.presentation.compose.FixedCountSection
import com.example.pillcountingnewmodels.feature.dashboard.presentation.compose.RegularCountSection
import com.example.pillcountingnewmodels.feature.dashboard.presentation.viewmodel.DashboardViewModel
import com.example.pillcountingnewmodels.ui.theme.AppTheme

/**
 * Dashboard screen entry point.
 *
 * Displays:
 * - [FixedCountSection] on the top/left.
 * - [RegularCountSection] on the bottom/right.
 * - [MenuButton] for navigation.
 *
 * The layout adapts responsively based on available space via [SplitResponsive].
 *
 * Handles:
 * - Exit confirmation dialog on back press.
 * - Navigation to Profile screen if profile is incomplete (unless "Do not ask again" is set).
 *
 * @param navController Used for navigation actions from dashboard sections.
 * @param viewModel ViewModel responsible for providing dashboard data/state.
 */
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val preferenceHelper = remember { PreferenceHelper(context) }

    // Prevent navigating back from dashboard screen
    BackHandler(enabled = true) {
        showLogoutDialog = true
    }

    // Collect dashboard UI state reactively
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to Profile screen if profile is incomplete and user hasn’t opted out
    LaunchedEffect(uiState.navigateToProfile) {
        if (uiState.navigateToProfile && !preferenceHelper.isDoNotAskAgain()) {
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = false }
            }
        }
    }

    Box(
        modifier = Modifier
            .systemBarsPadding()
            .background(AppTheme.extendedColors.secondaryBackground)
    ) {
        // Split screen layout: fixed counts vs regular counts
        SplitResponsive(
            topOrLeft = {
                FixedCountSection(
                    completedFixedCount = uiState.completedFixedCount,
                    partialFixedCount = uiState.partialFixedCount,
                    navController = navController
                )
            },
            bottomOrRight = {
                RegularCountSection(
                    completedRegularCount = uiState.completedRegularCount,
                    partialRegularCount = uiState.partialRegularCount,
                    navController = navController
                )
            },
        )

        // Global navigation menu button (top-right aligned)
        MenuButton(
            navController,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }

    // Exit confirmation dialog
    if (showLogoutDialog) {
        CommonDialog(
            message = stringResource(R.string.exit_text),
            confirmText = stringResource(R.string.yes),
            cancelText = stringResource(R.string.no),
            onConfirm = {
                showLogoutDialog = false
                activity?.finishAffinity()
            },
            onCancel = { showLogoutDialog = false }
        )
    }
}
