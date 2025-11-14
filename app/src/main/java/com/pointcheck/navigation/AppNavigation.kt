package com.pointcheck.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pointcheck.ui.screens.*

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Login : Screen("login")
    object Register : Screen("register")
    object Booking : Screen("booking")
    object Profile : Screen("profile")
    object ServiceDetail : Screen("service_detail")
}

@Composable
fun AppNavigation(snackbarHostState: SnackbarHostState) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) { DashboardScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav, snackbarHostState) }
        composable(Screen.Register.route) { RegisterScreen(nav, snackbarHostState) }
        composable(Screen.Booking.route) { BookingScreen(nav, snackbarHostState) }
        composable(Screen.Profile.route) { ProfileScreen(nav, snackbarHostState) }
        composable(Screen.ServiceDetail.route) { ServiceDetailScreen(nav) }
    }
}
