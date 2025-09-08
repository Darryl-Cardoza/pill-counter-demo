// file: navigation/AppNavGraph.kt

package com.example.pillcountingnewmodels.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pillcountingnewmodels.feature.countResume.presentation.FixedCountResumeScreen
import com.example.pillcountingnewmodels.feature.countResume.presentation.RegularCountResumeScreen
import com.example.pillcountingnewmodels.feature.counts.presentation.viewmodel.CountsViewModel
import com.example.pillcountingnewmodels.feature.dashboard.presentation.DashboardScreen
import com.example.pillcountingnewmodels.feature.history.presentation.HistoryScreen
import com.example.pillcountingnewmodels.feature.menu.presentation.compose.MenuScreen
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.ScanBarCodeScreen
import com.example.pillcountingnewmodels.feature.settings.presentation.SettingsScreen
import com.example.pillcountingnewmodels.navigatio.authGraph

// Define constants for nested graph routes for better organization
const val AUTH_GRAPH_ROUTE = "auth"

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AUTH_GRAPH_ROUTE
    ) {
        // Nested graph for all authentication-related screens
        authGraph(navController)

        // Main app screens (post-login)
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(navController)
        }

        composable(
            route = Screen.ScanBarcode.route,
            arguments = Screen.ScanBarcode.navArguments
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString(Screen.ScanBarcode.ARG_TYPE)
            ScanBarCodeScreen(navController)
        }


        composable(route = Screen.Menu.route) {
            MenuScreen(navController)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }

        composable(route = Screen.ResumeFixedCounts.route) { backStackEntry ->
            val viewModel: CountsViewModel = hiltViewModel()
            val uiState by viewModel.fixedUiState.collectAsState()
            FixedCountResumeScreen(
                navController = navController,
                uiState = uiState,
                onEvent = viewModel::onFixedEvent
            )
        }

        composable(route = Screen.ResumeRegularCounts.route) { backStackEntry ->
            val viewModel: CountsViewModel = hiltViewModel()
            val uiState by viewModel.regularUiState.collectAsState()
            RegularCountResumeScreen(
                navController = navController,
                uiState = uiState,
                onEvent = viewModel::onRegularEvent
            )
        }

        composable(route = Screen.History.route) {
            HistoryScreen(onBackClick = {
                navController.popBackStack()
            })
        }
    }
}