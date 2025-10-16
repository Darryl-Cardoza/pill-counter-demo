package com.rite.pillcounting.navigation

import Screen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.rite.pillcounting.feature.forgotPassword.presentation.compose.ForgotPasswordScreen
import com.rite.pillcounting.feature.login.presentation.LoginScreen
import com.rite.pillcounting.feature.otp.presentation.compose.OTPScreen
import com.rite.pillcounting.feature.register.presentation.compose.RegisterScreen

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
            LoginScreen(navController)
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(
            route = Screen.OtpVerify.route,
            arguments = Screen.OtpVerify.navArguments
        ) { backStackEntry ->

            val email = backStackEntry.arguments?.getString(Screen.OtpVerify.ARG_EMAIL) ?: ""

            val rememberMe =
                backStackEntry.arguments?.getBoolean(Screen.OtpVerify.ARG_REMEMBER_ME) ?: false

            // Pass both to OTPScreen
            OTPScreen(navController, email, rememberMe)
        }


        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
    }
}