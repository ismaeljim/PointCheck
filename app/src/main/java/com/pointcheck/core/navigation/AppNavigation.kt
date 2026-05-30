package com.pointcheck.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.attentions.presentation.AttentionScreen
import com.pointcheck.features.auth.presentation.LoginScreen
import com.pointcheck.features.auth.presentation.RegisterScreen
import com.pointcheck.features.auth.presentation.UserViewModel
import com.pointcheck.features.billing.presentation.BillingScreen
import com.pointcheck.features.dashboard.presentation.DashboardScreen
import com.pointcheck.features.dashboard.presentation.WeeklyReportScreen
import com.pointcheck.features.external.presentation.ServiceDetailScreen
import com.pointcheck.features.onboarding.presentation.CategorySelectionScreen
import com.pointcheck.features.onboarding.presentation.ServiceConfigurationScreen
import com.pointcheck.features.profile.presentation.ProfessionalProfileScreen
import com.pointcheck.features.profile.presentation.ProfileScreen
import com.pointcheck.features.reservation.presentation.AppointmentHistoryScreen
import com.pointcheck.features.reservation.presentation.BookingScreen
import com.pointcheck.features.reservation.presentation.ScheduledScreen
import com.pointcheck.features.services.presentation.ServiceListScreen
import com.pointcheck.features.subscriptions.presentation.SubscriptionScreen
import com.pointcheck.features.admin.presentation.UserManagementScreen
import com.pointcheck.features.admin.presentation.AuditLogScreen
import com.pointcheck.core.navigation.SplashScreen
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
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
    object AdminUsers : Screen("admin_users")
    object AdminAudit : Screen("admin_audit")
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val authVm: UserViewModel = viewModel()
    val snackbar = remember { SnackbarHostState() }

    NavHost(navController = nav, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(nav) }
        composable(Screen.Login.route) { LoginScreen(nav) }
        composable(Screen.Register.route) { RegisterScreen(nav, authVm) }
        
        composable("category_selection") {
            CategorySelectionScreen(nav, authVm)
        }
        
        composable(
            "service_configuration/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("categoryId") ?: ""
            ServiceConfigurationScreen(catId, nav, authVm)
        }

        composable(Screen.Main.route) {
            MainContainer(nav, snackbar)
        }
    }
}

@Composable
fun MainContainer(rootNav: NavController, snackbar: SnackbarHostState) {
    val nav = rememberNavController()
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var userRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userRole = prefs.role.first()
    }

    val hideBottomBar = listOf(
        Screen.Booking.route,
        Screen.Attention.route,
        Screen.Billing.route,
        Screen.ServiceDetail.route,
        Screen.ProfessionalProfile.route,
        Screen.Subscription.route,
        Screen.WeeklyReport.route,
        Screen.AdminUsers.route,
        Screen.AdminAudit.route
    ).any { it == currentDestination?.route }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { 
            if (!hideBottomBar) {
                BottomNavigationBar(nav, userRole)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(nav) }
            composable(Screen.Scheduled.route) { ScheduledScreen(nav) }
            
            composable(
                route = Screen.AppointmentHistory.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "upcoming"
                AppointmentHistoryScreen(type, nav)
            }
            
            composable(Screen.ServiceManagement.route) { ServiceListScreen(nav) }
            composable(Screen.Profile.route) { ProfileScreen(nav = nav, rootNav = rootNav) }
            
            composable(Screen.WeeklyReport.route) { WeeklyReportScreen(nav) }
            composable(Screen.ProfessionalProfile.route) { ProfessionalProfileScreen(nav) }
            composable(Screen.Subscription.route) { SubscriptionScreen(nav) }
            
            composable(
                route = Screen.Booking.route,
                arguments = listOf(
                    navArgument("specialistId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("categoryId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val specId = backStackEntry.arguments?.getString("specialistId")
                val catId = backStackEntry.arguments?.getString("categoryId")
                BookingScreen(nav, snackbar, specId, catId)
            }
            
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
                    navArgument("attId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val resId = backStackEntry.arguments?.getString("resId") ?: ""
                val cliId = backStackEntry.arguments?.getString("cliId") ?: ""
                val specId = backStackEntry.arguments?.getString("specId") ?: ""
                val attId = backStackEntry.arguments?.getString("attId")
                BillingScreen(nav, resId, cliId, specId, attId)
            }

            composable(
                route = Screen.ServiceDetail.route,
                arguments = listOf(navArgument("serviceName") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("serviceName") ?: ""
                ServiceDetailScreen(name, nav)
            }

            // --- Admin Routes ---
            composable(Screen.AdminUsers.route) {
                UserManagementScreen(onBack = { nav.popBackStack() })
            }
            composable(Screen.AdminAudit.route) {
                AuditLogScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(nav: NavController, userRole: String?) {
    val items = mutableListOf(
        Triple(Screen.Dashboard.route, "Inicio", Icons.Outlined.Home),
        Triple(Screen.Scheduled.route, "Agenda", Icons.Outlined.CalendarMonth)
    )

    if (userRole?.uppercase() == "SPECIALIST" || userRole?.uppercase() == "ADMIN") {
        items.add(Triple(Screen.ServiceManagement.route, "Servicios", Icons.Outlined.Handyman))
    } else {
        items.add(Triple(Screen.Booking.route, "Reservar", Icons.Outlined.AddCircleOutline))
    }
    
    items.add(Triple(Screen.Profile.route, "Perfil", Icons.Outlined.Person))

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by nav.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { (route, label, icon) ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                selected = isSelected,
                onClick = {
                    nav.navigate(route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
