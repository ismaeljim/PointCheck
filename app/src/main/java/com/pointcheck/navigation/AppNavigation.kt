package com.pointcheck.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pointcheck.ui.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object Login : Screen("login")
    object Register : Screen("register")
    object Booking : Screen("booking")
    object Profile : Screen("profile")
    object ServiceDetail : Screen("service_detail")
    object Scheduled : Screen("scheduled/{reservationId}") {
        fun createRoute(reservationId: Long) = "scheduled/$reservationId"
    }
}

@Composable
fun AppNavigation(snackbarHostState: SnackbarHostState) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(nav) }
        composable(Screen.Dashboard.route) { DashboardScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav, snackbarHostState) }
        composable(Screen.Register.route) { RegisterScreen(nav, snackbarHostState) }
        composable(Screen.Booking.route) { BookingScreen(nav, snackbarHostState) }
        composable(Screen.Profile.route) { ProfileScreen(nav) }
        composable(Screen.ServiceDetail.route) { ServiceDetailScreen(nav) }
        composable(
            route = Screen.Scheduled.route,
            arguments = listOf(navArgument("reservationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getLong("reservationId")
            ScheduledScreen(nav, reservationId?.toInt())
        }
    }
}
