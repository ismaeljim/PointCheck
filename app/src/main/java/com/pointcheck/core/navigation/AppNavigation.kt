package com.pointcheck.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pointcheck.features.auth.presentation.LoginScreen
import com.pointcheck.features.auth.presentation.RegisterScreen
import com.pointcheck.features.reservation.presentation.BookingScreen
import com.pointcheck.features.reservation.presentation.ScheduledScreen
import com.pointcheck.features.dashboard.presentation.DashboardScreen
import com.pointcheck.features.dashboard.presentation.WeeklyReportScreen
import com.pointcheck.features.reservation.presentation.AppointmentHistoryScreen
import com.pointcheck.features.external.presentation.ServiceDetailScreen
import com.pointcheck.features.profile.presentation.ProfileScreen
import com.pointcheck.features.profile.presentation.ProfessionalProfileScreen
import com.pointcheck.features.services.presentation.ServiceListScreen
import com.pointcheck.features.attentions.presentation.AttentionScreen
import com.pointcheck.features.billing.presentation.BillingScreen
import com.pointcheck.features.subscriptions.presentation.SubscriptionScreen
import com.pointcheck.core.prefs.UserPreferences
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object Login : Screen("login")
    object Register : Screen("register")
    object Booking : Screen("booking?specialistId={specialistId}") {
        fun createRoute(specialistId: Long? = null) = 
            "booking" + (specialistId?.let { "?specialistId=$it" } ?: "")
    }
    object Scheduled : Screen("scheduled")
    object Profile : Screen("profile")
    object ProfessionalProfile : Screen("professional_profile")
    object ServiceManagement : Screen("service_management")
    object Subscription : Screen("subscription")
    object Attention : Screen("attention/{reservationId}/{clientId}/{specialistId}") {
        fun createRoute(reservationId: Long, clientId: Long, specialistId: Long) = "attention/$reservationId/$clientId/$specialistId"
    }
    object Billing : Screen("billing/{resId}/{cliId}/{specId}?attId={attId}") {
        fun createRoute(resId: Long, cliId: Long, specId: Long, attId: Long?) = 
            "billing/$resId/$cliId/$specId" + (attId?.let { "?attId=$it" } ?: "")
    }
    object ServiceDetail : Screen("service_detail/{serviceName}") {
        fun createRoute(serviceName: String) = "service_detail/$serviceName"
    }
    object AppointmentHistory : Screen("appointment_history/{type}") {
        fun createRoute(type: String) = "appointment_history/$type"
    }
    object WeeklyReport : Screen("weekly_report")
}

@Composable
fun AppNavigation(snackbar: SnackbarHostState) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(nav) }
        composable(Screen.Dashboard.route) { DashboardScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav) }
        composable(Screen.Register.route) { RegisterScreen(nav) }
        composable(
            route = Screen.Booking.route,
            arguments = listOf(
                navArgument("specialistId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val specId = backStackEntry.arguments?.getString("specialistId")?.toLongOrNull()
            BookingScreen(nav, snackbar, specId)
        }
        composable(Screen.Scheduled.route) { ScheduledScreen(nav) }
        composable(Screen.Profile.route) { ProfileScreen(nav) }
        composable(Screen.ProfessionalProfile.route) { ProfessionalProfileScreen(nav) }
        composable(Screen.ServiceManagement.route) { ServiceListScreen(nav) }
        composable(Screen.Subscription.route) { SubscriptionScreen(nav) }
        composable(
            route = Screen.Attention.route,
            arguments = listOf(
                navArgument("reservationId") { type = NavType.LongType },
                navArgument("clientId") { type = NavType.LongType },
                navArgument("specialistId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getLong("reservationId") ?: 0L
            val cliId = backStackEntry.arguments?.getLong("clientId") ?: 0L
            val specId = backStackEntry.arguments?.getLong("specialistId") ?: 0L
            AttentionScreen(nav, resId, cliId, specId)
        }
        composable(
            route = "billing/{resId}/{cliId}/{specId}?attId={attId}",
            arguments = listOf(
                navArgument("resId") { type = NavType.LongType },
                navArgument("cliId") { type = NavType.LongType },
                navArgument("specId") { type = NavType.LongType },
                navArgument("attId") { 
                    type = NavType.StringType // Se pasa como String para manejar nulos fácilmente
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getLong("resId") ?: 0L
            val cliId = backStackEntry.arguments?.getLong("cliId") ?: 0L
            val specId = backStackEntry.arguments?.getLong("specId") ?: 0L
            val attId = backStackEntry.arguments?.getString("attId")?.toLongOrNull()
            BillingScreen(nav, resId, cliId, specId, attId)
        }
        composable(
            route = Screen.ServiceDetail.route,
            arguments = listOf(navArgument("serviceName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("serviceName") ?: ""
            ServiceDetailScreen(name, nav)
        }
        composable(
            route = Screen.AppointmentHistory.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "recent"
            AppointmentHistoryScreen(type, nav)
        }
        composable(Screen.WeeklyReport.route) {
            WeeklyReportScreen(nav)
        }
    }
}
