// file: navigation/AppNavGraph.kt

package com.example.pillcountingnewmodels.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pillcountingnewmodels.feature.dashboard.presentation.DashboardScreen
import com.example.pillcountingnewmodels.feature.history.presentation.compose.HistoryScreen
import com.example.pillcountingnewmodels.feature.menu.presentation.compose.MenuScreen
import com.example.pillcountingnewmodels.feature.pill_count.presentation.ScanBarCodeScreen
import com.example.pillcountingnewmodels.feature.settings.presentation.SettingsScreen // Placeholder
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
            ScanBarCodeScreen(navController, scanType = type ?: "regular")
        }


        composable(route = Screen.Menu.route) {
            MenuScreen(navController)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(route = Screen.History.route) {
            HistoryScreen(navController)
        }
    }
}