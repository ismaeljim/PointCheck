package com.pointcheck.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
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
import com.pointcheck.features.billing.presentation.BillingListScreen
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

/**
 * Definición de las pantallas de la aplicación y sus rutas de navegación.
 * Se utiliza el formato de rutas de Jetpack Navigation.
 * 
 * NOTA DE REFACTORIZACIÓN: Todos los identificadores (IDs) han sido migrados de Long a UUID.
 * En la navegación, estos se pasan como String.
 */
sealed class Screen(val route: String) {
    /** Pantalla de carga inicial y verificación de sesión. */
    object Splash : Screen("splash")
    /** Pantalla principal con métricas y accesos rápidos según rol. */
    object Dashboard : Screen("dashboard")
    /** Pantalla de inicio de sesión. */
    object Login : Screen("login")
    /** Pantalla de registro de nuevos usuarios (Clientes y Especialistas). */
    object Register : Screen("register")
    
    /** 
     * Pantalla de reserva de servicios.
     * Permite seleccionar profesional, servicio, fecha y hora.
     */
    object Booking : Screen("booking?specialistId={specialistId}&categoryId={categoryId}") {
        /**
         * Crea la ruta para navegar a la reserva.
         * @param specialistId UUID del especialista pre-seleccionado.
         * @param categoryId UUID de la categoría para filtrar especialistas.
         */
        fun createRoute(specialistId: String? = null, categoryId: String? = null) =
            "booking?" + 
            (specialistId?.let { "specialistId=$it" } ?: "") +
            (if (specialistId != null && categoryId != null) "&" else "") +
            (categoryId?.let { "categoryId=$it" } ?: "")
    }

    /** 
     * Agenda de citas del usuario. 
     * Si es especialista, muestra tanto sus atenciones como sus propias reservas.
     */
    object Scheduled : Screen("scheduled?filter={filter}") {
        /**
         * Crea la ruta con un filtro opcional (ej: "PENDING").
         */
        fun createRoute(filter: String? = null) = "scheduled" + (filter?.let { "?filter=$it" } ?: "")
    }

    /** Gestión del perfil personal del usuario. */
    object Profile : Screen("profile")
    /** Configuración del perfil profesional (solo para especialistas). */
    object ProfessionalProfile : Screen("professional_profile")
    /** Catálogo de servicios ofrecidos por el profesional. */
    object ServiceManagement : Screen("service_management")
    /** Gestión de la suscripción del profesional. */
    object Subscription : Screen("subscription")

    /** Lista general de cobros del especialista. */
    object BillingList : Screen("billing_list")

    /** 
     * Flujo de atención activa de una cita. 
     * Permite al especialista registrar el inicio y fin del servicio.
     */
    object Attention : Screen("attention/{reservationId}") {
        /**
         * Crea la ruta para iniciar la atención de una reserva específica.
         */
        fun createRoute(reservationId: String) = "attention/$reservationId"
    }

    /** 
     * Gestión de pagos y comprobantes.
     * Consolida los servicios prestados y genera el registro de cobro.
     */
    object Billing : Screen("billing/{reservationId}?attentionId={attentionId}") {
        /**
         * Crea la ruta para facturar una reserva terminada.
         */
        fun createRoute(reservationId: String, attentionId: String?) =
            "billing/$reservationId" + (attentionId?.let { "?attentionId=$it" } ?: "")
    }

    /** Detalle público de un servicio específico. */
    object ServiceDetail : Screen("service_detail/{serviceName}") {
        /** Crea la ruta basada en el nombre del servicio. */
        fun createRoute(serviceName: String) = "service_detail/$serviceName"
    }

    /** Historial de citas pasadas y terminadas. */
    object AppointmentHistory : Screen("appointment_history/{type}") {
        /** Crea la ruta para ver el historial (cliente o especialista). */
        fun createRoute(type: String) = "appointment_history/$type"
    }

    /** Reportes semanales de rendimiento para profesionales. */
    object WeeklyReport : Screen("weekly_report")
    /** Panel de administración para gestión de usuarios. */
    object AdminUsers : Screen("admin_users")
    /** Registro de auditoría del sistema (solo Admin). */
    object AdminAudit : Screen("admin_audit")
}

/**
 * Grafo de navegación principal de la aplicación.
 * Gestiona el paso de parámetros, la instanciación de ViewModels y la lógica de protección de rutas.
 * 
 * @param snackbar Host para mostrar notificaciones rápidas en pantalla.
 * @param nav Controlador de navegación compartido (opcional).
 */
@Composable
fun AppNavigation(
    snackbar: SnackbarHostState,
    nav: NavHostController = rememberNavController()
) {
    // authVm se comparte en el grafo si es necesario (ej: registro)
    val authVm: UserViewModel = viewModel()
    
    NavHost(navController = nav, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) { SplashScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav) }
        composable(Screen.Dashboard.route) { DashboardScreen(nav) }
        composable(Screen.Register.route) { RegisterScreen(nav, authVm) }
        
        composable("category_selection") {
            CategorySelectionScreen(nav, authVm)
        }
        
        // categoryId es UUID String
        composable(
            "service_configuration/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("categoryId") ?: ""
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
        composable(Screen.BillingList.route) { BillingListScreen(nav) }

        // Módulo de Atención: Se pasa reservationId (UUID)
        composable(
            route = Screen.Attention.route,
            arguments = listOf(
                navArgument("reservationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getString("reservationId") ?: ""
            AttentionScreen(nav, resId)
        }

        // Módulo de Facturación: Se pasa reservationId y attentionId opcional (UUIDs)
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

        // --- Rutas de Administración (Protegidas por Rol) ---
        composable(Screen.AdminUsers.route) {
            val userRole = authVm.state.collectAsState().value.role
            if (userRole == "ADMIN") {
                UserManagementScreen(onBack = { nav.popBackStack() })
            } else {
                // Redirección si no es admin para evitar accesos indebidos
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
