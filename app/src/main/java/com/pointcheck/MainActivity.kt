package com.pointcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pointcheck.core.network.ApiClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pointcheck.core.navigation.AppNavigation
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.theme.PointCheckTheme

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializamos el ApiClient con el contexto para activar el interceptor JWT
        ApiClient.init(this)

        setContent {
            PointCheckApp(UserPreferences(this))
        }
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Dashboard.route, "Inicio", Icons.Default.Home)
    object Agenda : BottomNavItem(Screen.Scheduled.route, "Agenda", Icons.Default.CalendarToday)
    object Billing : BottomNavItem(Screen.BillingList.route, "Pagos", Icons.Default.Payments)
    object MyAppointments : BottomNavItem(Screen.Scheduled.route, "Mis Citas", Icons.Default.Event)
    object AdminUsers : BottomNavItem(Screen.AdminUsers.route, "Control", Icons.Default.AdminPanelSettings)
    object AdminAudit : BottomNavItem(Screen.AdminAudit.route, "Auditoría", Icons.Default.Security)
    object Profile : BottomNavItem(Screen.Profile.route, "Perfil", Icons.Default.Person)
}

@Composable
fun PointCheckApp(userPrefs: UserPreferences) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Debounce de navegación para evitar ráfagas de transiciones
    var lastClickTime by remember { mutableLongStateOf(0L) }

    // Observamos el rol del usuario con inyección síncrona desde RAM para evitar "flicker" de rol
    val userRole by userPrefs.userRole.collectAsStateWithLifecycle(initialValue = userPrefs.cachedRole)

    // Pantallas donde NO se muestra la barra de navegación (Auth/Splash)
    val noBottomBarScreens = listOf(Screen.Splash.route, Screen.Login.route, Screen.Register.route, "category_selection")
    val showBottomBar = currentDestination?.route !in noBottomBarScreens

    PointCheckTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    // Si el rol está vacío, la sesión aún está subiendo a RAM.
                    // Ocultamos los items para evitar parpadeo de iconos (flicker).
                    if (userRole.isEmpty()) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {}
                    } else {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            val items = when (userRole.uppercase()) {
                                "ADMIN" -> listOf(
                                    BottomNavItem.Home,
                                    BottomNavItem.AdminUsers,
                                    BottomNavItem.AdminAudit,
                                    BottomNavItem.Profile
                                )
                                "SPECIALIST", "PROFESSIONAL" -> listOf(
                                    BottomNavItem.Home,
                                    BottomNavItem.Agenda,
                                    BottomNavItem.Billing,
                                    BottomNavItem.Profile
                                )
                                else -> listOf( // CLIENT
                                    BottomNavItem.Home,
                                    BottomNavItem.MyAppointments,
                                    BottomNavItem.Profile
                                )
                            }

                            items.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                                    selected = selected,
                                    onClick = {
                                        val currentTime = System.currentTimeMillis()
                                        // Evitamos navegación redundante y colisiones de ráfaga (500ms debounce)
                                        if (currentDestination?.route != item.route && currentTime - lastClickTime > 500L) {
                                            lastClickTime = currentTime
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.outline,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavigation(snackbarHostState, navController)
            }
        }
    }
}
