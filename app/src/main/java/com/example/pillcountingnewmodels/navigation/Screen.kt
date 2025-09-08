

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * A sealed interface to represent all navigable screens in the app.
 * This approach provides type safety and autocompletion for routes and arguments,
 * preventing common errors associated with string-based navigation.
 */
sealed interface Screen {
    val route: String

    // For screens without arguments
    data object Login : Screen {
        override val route: String = "login"
    }

    data object Register : Screen {
        override val route: String = "register"
    }

    data object ForgotPassword : Screen {
        override val route: String = "forgot_password"
    }

    data object Dashboard : Screen {
        override val route: String = "dashboard"
    }

    data object Menu : Screen {
        override val route: String = "menu"
    }

    data object Settings : Screen {
        override val route: String = "settings"
    }

    data object History : Screen {
        override val route: String = "history"
    }


    // For screens with arguments
    data object OtpVerify : Screen {
        private const val ROUTE_PREFIX = "otp_verify"
        const val ARG_EMAIL = "email"

        // The route definition with a placeholder for the argument
        override val route: String = "$ROUTE_PREFIX/{$ARG_EMAIL}"

        // List of arguments for the NavHost composable builder
        val navArguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_EMAIL) { type = NavType.StringType }
        )

        // Helper function to build the navigation path with a real value
        fun createRoute(email: String) = "$ROUTE_PREFIX/$email"
    }

    data object ScanBarcode : Screen {
        private const val ROUTE_PREFIX = "scan_barcode"
        const val ARG_TYPE = "type"

        override val route: String = "$ROUTE_PREFIX/{$ARG_TYPE}"

        val navArguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_TYPE) { type = NavType.StringType }
        )

        fun createRoute(scanType: String) = "$ROUTE_PREFIX/$scanType"
    }

    data object PillCount : Screen {
        private const val ROUTE_PREFIX = "pill_count"
        const val ARG_TYPE = "type"

        override val route: String = "$ROUTE_PREFIX/{$ARG_TYPE}"

        val navArguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_TYPE) { type = NavType.StringType }
        )

        fun createRoute(scanType: String) = "$ROUTE_PREFIX/$scanType"
    }

    data object ResumeFixedCounts : Screen {
        private const val ROUTE_PREFIX = "resume_fixed_counts"
        const val ARG_TYPE = "type"

        override val route: String = "$ROUTE_PREFIX/{$ARG_TYPE}"

        val navArguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_TYPE) { type = NavType.StringType }
        )

        fun createRoute(type: String) = "$ROUTE_PREFIX/$type"
    }

    data object ResumeRegularCounts : Screen {
        private const val ROUTE_PREFIX = "resume_regular_counts"
        const val ARG_TYPE = "type"

        override val route: String = "$ROUTE_PREFIX/{$ARG_TYPE}"

        val navArguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_TYPE) { type = NavType.StringType }
        )

        fun createRoute(type: String) = "$ROUTE_PREFIX/$type"
    }

}

