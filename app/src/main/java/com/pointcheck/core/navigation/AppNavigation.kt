package com.pointcheck.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.pointcheck.features.onboarding.presentation.CategorySelectionScreen
import com.pointcheck.features.onboarding.presentation.ServiceConfigurationScreen
import com.pointcheck.features.admin.presentation.UserManagementScreen
import com.pointcheck.features.admin.presentation.AuditLogScreen
import com.pointcheck.features.admin.presentation.AdminViewModel
import com.pointcheck.features.auth.presentation.UserViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
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
    object Booking : Screen("booking?specialistId={specialistId}&categoryId={categoryId}") {
        fun createRoute(specialistId: String? = null, categoryId: String? = null) =
            "booking?" + 
            (specialistId?.let { "specialistId=$it" } ?: "") +
            (if (specialistId != null && categoryId != null) "&" else "") +
            (categoryId?.let { "categoryId=$it" } ?: "")
    }
    object Scheduled : Screen("scheduled?filter={filter}") {
        fun createRoute(filter: String? = null) = "scheduled" + (filter?.let { "?filter=$it" } ?: "")
    }
    object Profile : Screen("profile")
    object ProfessionalProfile : Screen("professional_profile")
    object ServiceManagement : Screen("service_management")
    object Subscription : Screen("subscription")
    object Attention : Screen("attention/{reservationId}") {
        fun createRoute(reservationId: String) = "attention/$reservationId"
    }
    object Billing : Screen("billing/{reservationId}?attentionId={attentionId}") {
        fun createRoute(reservationId: String, attentionId: String?) =
            "billing/$reservationId" + (attentionId?.let { "?attentionId=$it" } ?: "")
    }
    object ServiceDetail : Screen("service_detail/{serviceName}") {
        fun createRoute(serviceName: String) = "service_detail/$serviceName"
    }
    object AppointmentHistory : Screen("appointment_history/{type}") {
        fun createRoute(type: String) = "appointment_history/$type"
    }
    object WeeklyReport : Screen("weekly_report")
    object AdminUsers : Screen("admin_users")
    object AdminAudit : Screen("admin_audit")
}

@Composable
fun AppNavigation(snackbar: SnackbarHostState, nav: NavHostController) {
    val authVm: UserViewModel = viewModel()
    
    NavHost(navController = nav, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) { SplashScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav) }
        composable(Screen.Dashboard.route) { DashboardScreen(nav) }
        composable(Screen.Register.route) { RegisterScreen(nav, authVm) }
        
        composable("category_selection") {
            CategorySelectionScreen(nav, authVm)
        }
        
        /**
     * Rutas de Proceso (Atención y Facturación)
     * Optimizadas para requerir solo 'reservationId'. 
     * Los ViewModels recargan el contexto completo para asegurar consistencia con UserSummaryDto.
     */
    composable(
            "service_configuration/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("categoryId") ?: ""
            ServiceConfigurationScreen(catId, nav, authVm)
        }
        
        /**
     * Rutas de Proceso (Atención y Facturación)
     * Optimizadas para requerir solo 'reservationId'. 
     * Los ViewModels recargan el contexto completo para asegurar consistencia con UserSummaryDto.
     */
    composable(
            route = Screen.Booking.route,
            arguments = listOf(
                navArgument("specialistId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null
                },
                navArgument("categoryId") {
                    type = NavType.StringType // Category ID typically Long but passed as string in query params
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val specId = backStackEntry.arguments?.getString("specialistId")
            val catId = backStackEntry.arguments?.getString("categoryId")
            BookingScreen(nav, snackbar, specId, catId)
        }

        composable(
            route = Screen.Scheduled.route,
            arguments = listOf(navArgument("filter") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter")
            ScheduledScreen(nav, filter = filter)
        }
        composable(Screen.Profile.route) { ProfileScreen(nav) }
        composable(Screen.ProfessionalProfile.route) { ProfessionalProfileScreen(nav) }
        composable(Screen.ServiceManagement.route) { ServiceListScreen(nav) }
        composable(Screen.Subscription.route) { SubscriptionScreen(nav) }

        /**
     * Rutas de Proceso (Atención y Facturación)
     * Optimizadas para requerir solo 'reservationId'. 
     * Los ViewModels recargan el contexto completo para asegurar consistencia con UserSummaryDto.
     */
    composable(
            route = Screen.Attention.route,
            arguments = listOf(
                navArgument("reservationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getString("reservationId") ?: ""
            AttentionScreen(nav, resId)
        }

        /**
     * Rutas de Proceso (Atención y Facturación)
     * Optimizadas para requerir solo 'reservationId'. 
     * Los ViewModels recargan el contexto completo para asegurar consistencia con UserSummaryDto.
     */
    composable(
            route = Screen.Billing.route,
            arguments = listOf(
                navArgument("reservationId") { type = NavType.StringType },
                navArgument("attentionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getString("reservationId") ?: ""
            val attId = backStackEntry.arguments?.getString("attentionId")
            BillingScreen(nav, resId, attId)
        }

        /**
     * Rutas de Proceso (Atención y Facturación)
     * Optimizadas para requerir solo 'reservationId'. 
     * Los ViewModels recargan el contexto completo para asegurar consistencia con UserSummaryDto.
     */
    composable(
            route = Screen.ServiceDetail.route,
            arguments = listOf(navArgument("serviceName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("serviceName") ?: ""
            ServiceDetailScreen(name, nav)
        }
        /**
     * Rutas de Proceso (Atención y Facturación)
     * Optimizadas para requerir solo 'reservationId'. 
     * Los ViewModels recargan el contexto completo para asegurar consistencia con UserSummaryDto.
     */
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

        // --- Admin Routes ---
        composable(Screen.AdminUsers.route) {
            val userRole = authVm.state.collectAsState().value.role
            if (userRole == "ADMIN") {
                UserManagementScreen(onBack = { nav.popBackStack() })
            } else {
                nav.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.AdminUsers.route) { inclusive = true }
                }
            }
        }
        composable(Screen.AdminAudit.route) {
            val userRole = authVm.state.collectAsState().value.role
            if (userRole == "ADMIN") {
                AuditLogScreen(onBack = { nav.popBackStack() })
            } else {
                nav.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.AdminAudit.route) { inclusive = true }
                }
            }
        }
    }
}
