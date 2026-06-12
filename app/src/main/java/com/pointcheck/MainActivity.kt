package com.pointcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pointcheck.core.network.ApiClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializamos el ApiClient con el contexto para activar el interceptor JWT
        ApiClient.init(this)

        setContent {
            PointCheckApp()
        }
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Dashboard.route, "Inicio", Icons.Default.Dashboard)
    object Agenda : BottomNavItem(Screen.Scheduled.route, "Agenda", Icons.Default.CalendarToday)
    object Billing : BottomNavItem("billing_list", "Cobros", Icons.Default.AccountBalanceWallet)
    object Profile : BottomNavItem(Screen.Profile.route, "Perfil", Icons.Default.Person)
}

@Composable
fun PointCheckApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Pantallas donde NO se muestra la barra de navegación (Auth/Splash)
    val noBottomBarScreens = listOf(Screen.Splash.route, Screen.Login.route, Screen.Register.route, "category_selection")
    val showBottomBar = currentDestination?.route !in noBottomBarScreens

    PointCheckTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        val items = listOf(
                            BottomNavItem.Home,
                            BottomNavItem.Agenda,
                            BottomNavItem.Billing,
                            BottomNavItem.Profile
                        )
                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
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
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavigation(snackbarHostState, navController)
            }
        }
    }
}
