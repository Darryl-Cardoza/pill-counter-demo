package com.example.pillcountingnewmodels.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pillcountingnewmodels.feature.dashboard.presentation.DashboardScreen
import com.example.pillcountingnewmodels.feature.forgot_password.presentation.ForgotPasswordScreen
import com.example.pillcountingnewmodels.feature.login.presentation.LoginScreen
import com.example.pillcountingnewmodels.feature.pill_count.presentation.ScanBarCodeScreen
import com.example.pillcountingnewmodels.feature.register.presentation.OTPScreen
import com.example.pillcountingnewmodels.feature.register.presentation.RegisterScreen
import com.example.pillcountingnewmodels.navigation.Routes.DASHBOARD
import com.example.pillcountingnewmodels.navigation.Routes.FORGOT_PASSWORD
import com.example.pillcountingnewmodels.navigation.Routes.LOGIN
import com.example.pillcountingnewmodels.navigation.Routes.OTP_VERIFY
import com.example.pillcountingnewmodels.navigation.Routes.REGISTER
import com.example.pillcountingnewmodels.navigation.Routes.SCAN_BARCODE

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = LOGIN
    ) {
        composable(LOGIN) { LoginScreen(navController) }
        composable(REGISTER) { RegisterScreen(navController) }
        composable(DASHBOARD) { DashboardScreen(navController) }

        composable("${OTP_VERIFY}/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OTPScreen(navController, email)
        }

        composable(FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }

        composable(
            route = "$SCAN_BARCODE/{type}"
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            ScanBarCodeScreen(navController, scanType = type ?: "regular")
        }
    }
}
