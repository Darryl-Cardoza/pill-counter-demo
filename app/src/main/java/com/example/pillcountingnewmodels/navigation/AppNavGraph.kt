package com.example.pillcountingnewmodels.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pillcountingnewmodels.feature.dashboard.presentation.DashboardScreen
import com.example.pillcountingnewmodels.feature.forgot_password.presentation.ForgotPasswordScreen
import com.example.pillcountingnewmodels.feature.login.presentation.LoginScreen
import com.example.pillcountingnewmodels.feature.register.presentation.OTPScreen
import com.example.pillcountingnewmodels.feature.register.presentation.RegisterScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }
        composable(Routes.DASHBOARD) { DashboardScreen(navController) }
        composable("${Routes.OTP_VERIFY}/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OTPScreen(navController, email)
        }
        composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }
        //composable("home") { HomeScreen(navController) }
    }
}
