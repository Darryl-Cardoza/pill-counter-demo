package com.example.pillcountingnewmodels.navigatio

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.pillcountingnewmodels.feature.fixedPillCountScan.presentation.FixedCountPillScanningScreen
import com.example.pillcountingnewmodels.feature.forgotPassword.presentation.compose.ForgotPasswordScreen
import com.example.pillcountingnewmodels.feature.otp.presentation.compose.OTPScreen
import com.example.pillcountingnewmodels.feature.register.presentation.RegisterScreen
import com.example.pillcountingnewmodels.navigation.AUTH_GRAPH_ROUTE

/**
 * Encapsulates the authentication flow (Login, Register, OTP, etc.)
 * in a nested navigation graph.
 */
/**
 * Encapsulates the authentication flow (Login, Register, OTP, etc.)
 * in a nested navigation graph.
 */
fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Login.route,
        route = AUTH_GRAPH_ROUTE
    ) {
        composable(route = Screen.Login.route) {
//            LoginScreen(navController)
            FixedCountPillScanningScreen(navController)
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(
            route = Screen.OtpVerify.route,
            arguments = Screen.OtpVerify.navArguments
        ) { backStackEntry ->
            // Use the type-safe argument key from the Screen object
            val email = backStackEntry.arguments?.getString(Screen.OtpVerify.ARG_EMAIL) ?: ""
            OTPScreen(navController, email)
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
    }
}