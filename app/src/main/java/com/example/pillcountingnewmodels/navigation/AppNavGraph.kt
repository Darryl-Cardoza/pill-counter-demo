package com.example.pillcountingnewmodels.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pillcountingnewmodels.feature.dashboard.presentation.DashboardScreen
import com.example.pillcountingnewmodels.feature.login.presentation.LoginScreen
import com.example.pillcountingnewmodels.feature.register.presentation.RegisterScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        //composable("home") { HomeScreen(navController) }
    }
}
