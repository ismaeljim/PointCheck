package com.pointcheck.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pointcheck.features.auth.presentation.LoginScreen
import com.pointcheck.features.auth.presentation.RegisterScreen
import com.pointcheck.features.booking.presentation.BookingScreen
import com.pointcheck.features.booking.presentation.ScheduledScreen
import com.pointcheck.features.dashboard.presentation.DashboardScreen
import com.pointcheck.features.external.presentation.ServiceDetailScreen
import com.pointcheck.features.profile.presentation.ProfileScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object Login : Screen("login")
    object Register : Screen("register")
    object Booking : Screen("booking")
    object Scheduled : Screen("scheduled")
    object Profile : Screen("profile")
    object ServiceDetail : Screen("service_detail/{serviceName}") {
        fun createRoute(serviceName: String) = "service_detail/$serviceName"
    }
}

@Composable
fun AppNavigation(snackbar: SnackbarHostState) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(nav) }
        composable(Screen.Dashboard.route) { DashboardScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav) }
        composable(Screen.Register.route) { RegisterScreen(nav) }
        composable(Screen.Booking.route) { BookingScreen(nav, snackbar) }
        composable(Screen.Scheduled.route) { ScheduledScreen(nav) }
        composable(Screen.Profile.route) { ProfileScreen(nav) }
        composable(
            route = Screen.ServiceDetail.route,
            arguments = listOf(navArgument("serviceName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("serviceName") ?: ""
            ServiceDetailScreen(name, nav)
        }
    }
}
