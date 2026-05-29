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
<<<<<<< Updated upstream
import com.pointcheck.features.onboarding.presentation.CategorySelectionScreen
import com.pointcheck.features.onboarding.presentation.ServiceConfigurationScreen
import com.pointcheck.features.onboarding.presentation.CategoryViewModel
import com.pointcheck.features.auth.presentation.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.core.prefs.UserPreferences
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
=======
>>>>>>> Stashed changes

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object Login : Screen("login")
    object Register : Screen("register")
    object Booking : Screen("booking?specialistId={specialistId}&categoryId={categoryId}") {
        fun createRoute(specialistId: Long? = null, categoryId: Long? = null) = 
            "booking?" + 
            (specialistId?.let { "specialistId=$it" } ?: "") +
            (if (specialistId != null && categoryId != null) "&" else "") +
            (categoryId?.let { "categoryId=$it" } ?: "")
    }
    object Scheduled : Screen("scheduled")
    object Profile : Screen("profile")
    object ProfessionalProfile : Screen("professional_profile")
    object ServiceManagement : Screen("service_management")
    object Subscription : Screen("subscription")
    object Attention : Screen("attention/{reservationId}/{clientId}/{specialistId}") {
        fun createRoute(reservationId: String, clientId: String, specialistId: String) = "attention/$reservationId/$clientId/$specialistId"
    }
    object Billing : Screen("billing/{resId}/{cliId}/{specId}?attId={attId}") {
        fun createRoute(resId: String, cliId: String, specId: String, attId: String?) =
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
    val authVm: UserViewModel = viewModel()
    
    NavHost(navController = nav, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) { SplashScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav) }
<<<<<<< Updated upstream
        composable(Screen.Register.route) { RegisterScreen(nav, authVm) }
        
        composable("category_selection") {
            CategorySelectionScreen(nav, authVm)
        }
        
        composable(
            "service_configuration/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            ServiceConfigurationScreen(catId, nav, authVm)
        }
        composable(
            route = Screen.Booking.route,
            arguments = listOf(
                navArgument("specialistId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null
                },
                navArgument("categoryId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val specId = backStackEntry.arguments?.getString("specialistId")?.toLongOrNull()
            val catId = backStackEntry.arguments?.getString("categoryId")?.toLongOrNull()
            BookingScreen(nav, snackbar, specId, catId)
        }
=======
        composable(Screen.Dashboard.route) { DashboardScreen(nav) }
        composable(Screen.Register.route) { RegisterScreen(nav) }
        composable(Screen.Booking.route) { BookingScreen(nav, snackbar) }
>>>>>>> Stashed changes
        composable(Screen.Scheduled.route) { ScheduledScreen(nav) }
        composable(Screen.Profile.route) { ProfileScreen(nav) }
        composable(Screen.ProfessionalProfile.route) { ProfessionalProfileScreen(nav) }
        composable(Screen.ServiceManagement.route) { ServiceListScreen(nav) }
        composable(Screen.Subscription.route) { SubscriptionScreen(nav) }

        composable(
            route = Screen.Attention.route,
            arguments = listOf(
                navArgument("reservationId") { type = NavType.StringType },
                navArgument("clientId") { type = NavType.StringType },
                navArgument("specialistId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getString("reservationId") ?: ""
            val cliId = backStackEntry.arguments?.getString("clientId") ?: ""
            val specId = backStackEntry.arguments?.getString("specialistId") ?: ""
            AttentionScreen(nav, resId, cliId, specId)
        }

        composable(
            route = Screen.Billing.route,
            arguments = listOf(
                navArgument("resId") { type = NavType.StringType },
                navArgument("cliId") { type = NavType.StringType },
                navArgument("specId") { type = NavType.StringType },
                navArgument("attId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getString("resId") ?: ""
            val cliId = backStackEntry.arguments?.getString("cliId") ?: ""
            val specId = backStackEntry.arguments?.getString("specId") ?: ""
            val attId = backStackEntry.arguments?.getString("attId")
            BillingScreen(nav, resId, cliId, specId, attId)
        }
<<<<<<< Updated upstream
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
=======
>>>>>>> Stashed changes
    }
}