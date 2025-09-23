// file: navigation/AppNavGraph.kt

package com.example.pillcountingnewmodels.navigation

import Screen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pillcountingnewmodels.feature.barcodeScan.presentation.ScanBarCodeScreen
import com.example.pillcountingnewmodels.feature.countResume.presentation.FixedCountResumeScreen
import com.example.pillcountingnewmodels.feature.countResume.presentation.RegularCountResumeScreen
import com.example.pillcountingnewmodels.feature.countResume.presentation.viewmodel.CountsViewModel
import com.example.pillcountingnewmodels.feature.dashboard.presentation.DashboardScreen
import com.example.pillcountingnewmodels.feature.history.presentation.HistoryScreen
import com.example.pillcountingnewmodels.feature.menu.presentation.MenuScreen
import com.example.pillcountingnewmodels.feature.pillCountScan.presentation.PillScanningScreen
import com.example.pillcountingnewmodels.feature.profile.presentation.ProfileScreen
import com.example.pillcountingnewmodels.feature.settings.presentation.SettingsScreen
import com.example.pillcountingnewmodels.navigatio.authGraph

// Define constants for nested graph routes for better organization
const val AUTH_GRAPH_ROUTE = "auth"

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
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
            val scanType = backStackEntry.arguments?.getString(Screen.ScanBarcode.ARG_TYPE) ?: ""
            ScanBarCodeScreen(navController, scanType)
        }

        composable(
            route = Screen.PillCount.route,
            arguments = Screen.PillCount.navArguments
        ) { backStackEntry ->
            val scanType = backStackEntry.arguments?.getString(Screen.PillCount.ARG_TYPE) ?: ""
            PillScanningScreen(navController, scanType)
        }


        composable(route = Screen.Menu.route) {
            MenuScreen(navController)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(route = Screen.ResumeFixedCounts.route) { backStackEntry ->
            FixedCountResumeScreen(
                navController = navController
            )
        }

        composable(route = Screen.ResumeRegularCounts.route) { backStackEntry ->
            RegularCountResumeScreen(
                navController = navController
            )
        }

        composable(route = Screen.History.route) {
            HistoryScreen(
                navController = navController,
                onBackClick = {
                    navController.popBackStack()
                })
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                onBackClick = {
                    navController.popBackStack()
                })
        }
    }
}