package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.components.PCButton
import com.pointcheck.core.ui.components.PCCard
import com.pointcheck.core.ui.components.PCOutlinedButton
import com.pointcheck.core.utils.CategoryIdentityMapper
import com.pointcheck.features.dashboard.data.dto.FavoriteSpecialistDto
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto

@Composable
fun ClientDashboardV2(s: DashboardUiState, nav: NavController) {
    val d = s.clientDashboard
    val weather = s.weather
    Column(Modifier.fillMaxWidth()) {
        if (d?.nextAppointment != null) {
            Text("Tu Próxima Cita", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            FeaturedAppointmentCard(d.nextAppointment, weather, nav)
            Spacer(Modifier.height(24.dp))
        }

        Text("Tus Especialistas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (d?.favoriteSpecialists?.isNotEmpty() == true) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(d.favoriteSpecialists) { specialist ->
                    FavoriteSpecialistCard(specialist) {
                        nav.navigate(Screen.Booking.createRoute(specialist.specialistId))
                    }
                }
            }
        } else {
            PCCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Aún no tienes favoritos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Explora las categorías abajo para encontrar y guardar a tus especialistas de confianza.", 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Text("Explorar Servicios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ServiceCategoryGrid(s.categories, s.isLoading, s.error, nav)

        Spacer(Modifier.height(24.dp))
        PCOutlinedButton(
            text = "Historial Completo",
            icon = Icons.Default.History,
            onClick = { nav.navigate(Screen.AppointmentHistory.createRoute("all")) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FeaturedAppointmentCard(
    appointment: ReservationResponseDto?,
    weather: WeatherResponseDto?,
    nav: NavController
) {
    if (appointment == null) return
    val context = LocalContext.current

    val isPast = try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(appointment.reservationStart)
        date?.before(java.util.Date()) ?: false
    } catch (e: Exception) {
        false
    }

    PCCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (isPast) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isPast) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            if (isPast) Icons.Default.EventBusy else Icons.Default.Event,
                            null,
                            modifier = Modifier.padding(12.dp),
                            tint = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            if (isPast) "Cita Expirada" else (appointment.serviceName ?: "Tu Cita"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        
                        val displayDateTime = try {
                            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            val date = inputFormat.parse(appointment.reservationStart)
                            val outputFormat = java.text.SimpleDateFormat("EEE d 'de' MMM, HH:mm", java.util.Locale("es", "CL"))
                            date?.let { outputFormat.format(it).replaceFirstChar { it.uppercase() } } ?: "Fecha no disponible"
                        } catch (_: Exception) {
                            "Fecha no disponible"
                        }

                        Text(
                            displayDateTime,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!isPast) {
                            Text(
                                "con ${appointment.specialist.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Widget de clima real (solo si no es pasado)
                if (weather != null && !isPast) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${weather.main.temp.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            val suggestion = when {
                isPast -> "Esta cita ha expirado. Por favor, agenda una nueva o contacta al especialista si hubo un inconveniente."
                weather?.weather?.firstOrNull()?.description?.contains("rain", ignoreCase = true) == true -> 
                    "¡Va a llover! No olvides tu paraguas para tu cita."
                (weather?.main?.temp ?: 20.0) > 28.0 -> 
                    "¡Día caluroso! Mantente hidratado para tu cita."
                else -> "¡Día ideal para tu cita! Recuerda llegar 5 minutos antes."
            }
            
            Text(
                suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isPast) {
                    PCButton(
                        text = "Agendar Nueva",
                        onClick = { nav.navigate(Screen.Booking.route) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PCButton(
                        text = "Cómo llegar",
                        onClick = {
                            val address = appointment.address
                            if (!address.isNullOrBlank()) {
                                val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(address)}")
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                    context.startActivity(fallbackIntent)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !appointment.address.isNullOrBlank(),
                        icon = Icons.Default.Map
                    )
                    PCOutlinedButton(
                        text = "Detalles",
                        onClick = { nav.navigate(Screen.Scheduled.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteSpecialistCard(specialist: FavoriteSpecialistDto, onClick: () -> Unit) {
    PCCard(
        modifier = Modifier.width(140.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                specialist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                specialist.specialty ?: "Especialista",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ServiceCategoryGrid(
    categories: List<com.pointcheck.features.onboarding.presentation.dto.CategoryDto>,
    isLoading: Boolean,
    error: String?,
    nav: NavController
) {
    if (isLoading && categories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null && categories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Error al cargar servicios", color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    if (categories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No hay servicios disponibles", color = MaterialTheme.colorScheme.secondary)
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { cat ->
                    val icon = CategoryIdentityMapper.mapIcon(cat.icon)
                    
                    CategoryCard(cat.name, icon, Modifier.weight(1f)) {
                        nav.navigate(Screen.Booking.createRoute(null, cat.id))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(name: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    PCCard(
        modifier = modifier.height(110.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
