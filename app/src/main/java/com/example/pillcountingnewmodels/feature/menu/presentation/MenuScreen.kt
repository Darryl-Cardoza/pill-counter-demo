package com.example.pillcountingnewmodels.feature.menu.presentation

import Screen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pillcountingnewmodels.R
import com.example.pillcountingnewmodels.core.utils.compose.BackButton
import com.example.pillcountingnewmodels.core.utils.compose.Dimens.medium
import com.example.pillcountingnewmodels.feature.menu.presentation.compose.MenuItemRow
import com.example.pillcountingnewmodels.feature.menu.presentation.compose.SimpleMenuRow
import com.example.pillcountingnewmodels.feature.menu.presentation.viewmodel.MenuViewModel
import com.example.pillcountingnewmodels.navigation.AUTH_GRAPH_ROUTE
import com.example.pillcountingnewmodels.ui.theme.AppTheme.extendedColors

@Composable
fun MenuScreen(
    navController: NavController,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler { /* consume back press */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.secondaryBackground)
    ) {

        BackButton(navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = medium, end = medium)
                .background(extendedColors.secondaryBackground)
        ) {
            // Fixed Count
            MenuItemRow(
                icon = R.drawable.fixed_count,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.menu_fixed_count),
                completed = stringResource(R.string.menu_completed, uiState.fixedCompleted),
                partial = stringResource(R.string.menu_partial, uiState.fixedPartial),
                completedTint = MaterialTheme.colorScheme.secondary,
                partialTint = MaterialTheme.colorScheme.secondary,
                completedIcon = R.drawable.tick,
                partialIcon = R.drawable.partial
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Regular Count
            MenuItemRow(
                icon = R.drawable.regular_count,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.menu_regular_count),
                completed = stringResource(R.string.menu_completed, uiState.regularCompleted),
                partial = stringResource(R.string.menu_partial, uiState.regularPartial),
                completedTint = MaterialTheme.colorScheme.primary,
                partialTint = MaterialTheme.colorScheme.primary,
                completedIcon = R.drawable.tick,
                partialIcon = R.drawable.partial
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Profile
            SimpleMenuRow(
                navController = navController,
                icon = R.drawable.profile,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.menu_profile),
                onClick = { navController.navigate(Screen.Profile.route) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // History
            SimpleMenuRow(
                navController = navController,
                icon = R.drawable.history,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.menu_history),
                trailingText = stringResource(R.string.menu_history_duration),
                onClick = { navController.navigate(Screen.History.route) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Settings
            SimpleMenuRow(
                navController = navController,
                icon = R.drawable.settings,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.menu_settings),
                onClick = { navController.navigate(Screen.Settings.route) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Logout
            SimpleMenuRow(
                navController = navController,
                icon = R.drawable.logout,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.menu_logout),
                onClick = {
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
