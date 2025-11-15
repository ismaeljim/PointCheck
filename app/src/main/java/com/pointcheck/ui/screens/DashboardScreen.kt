package com.pointcheck.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pointcheck.data.prefs.UserPreferences
import com.pointcheck.navigation.Screen
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(nav: NavController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    var isLogged by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Comprobamos el estado de la sesión una sola vez
    LaunchedEffect(Unit) {
        isLogged = prefs.isLogged.first()
    }

    val carouselContent = listOf(
        "¡Bienvenido a PointCheck!",
        "Gestiona tus citas de forma fácil y rápida.",
        "Recibe recordatorios para no olvidar ninguna cita."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PointCheck") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (isLogged) {
                            DropdownMenuItem(text = { Text("Perfil") }, onClick = { nav.navigate(Screen.Profile.route); showMenu = false })
                        } else {
                            DropdownMenuItem(text = { Text("Iniciar sesión") }, onClick = { nav.navigate(Screen.Login.route); showMenu = false })
                            DropdownMenuItem(text = { Text("Crear cuenta") }, onClick = { nav.navigate(Screen.Register.route); showMenu = false })
                        }
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            // Texto explicativo
            Text(
                text = "PointCheck te ayuda a organizar y recordar tus citas importantes. Regístrate para empezar a gestionar tu agenda.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = carouselContent[page],
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
