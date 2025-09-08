package com.example.pillcountingnewmodels.feature.dashboard.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.core.utils.compose.MenuButton
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
 * @param navController Used for navigation actions from dashboard sections.
 * @param viewModel ViewModel responsible for providing dashboard data/state.
 */
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // Prevent navigating back from dashboard screen
    BackHandler(enabled = true) { /* Intentionally left blank */ }

    // Collect dashboard UI state reactively
    val uiState by viewModel.uiState.collectAsState()

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
}
