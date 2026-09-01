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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckCard
import com.pointcheck.core.utils.CategoryIdentityMapper
import com.pointcheck.features.dashboard.data.dto.FavoriteSpecialistDto
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto

@Composable
fun ClientDashboard(s: DashboardUiState.Success, nav: NavController) {
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
                items(d.favoriteSpecialists, key = { it.specialistProfileId ?: it.name }) { specialist ->
                    FavoriteSpecialistCard(specialist) {
                        if (!specialist.specialistProfileId.isNullOrBlank()) {
                            nav.navigate(Screen.Booking.createRoute(specialist.specialistProfileId))
                        }
                    }
                }
            }
        } else {
            PointCheckCard(
                title = "Aún no tienes favoritos",
                subtitle = "Encuentra y guarda a tus especialistas de confianza",
                icon = Icons.Default.FavoriteBorder
            ) {
                Text(
                    "Explora las categorías abajo para empezar a personalizar tu experiencia.", 
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        Text("Explorar Servicios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ServiceCategoryGrid(s.categories, false, null, nav)

        Spacer(Modifier.height(24.dp))
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

    val displayDateTime = try {
        appointment.reservationStart.replace("T", " ").substringBeforeLast(":")
    } catch (_: Exception) {
        "Fecha no disponible"
    }

    PointCheckCard(
        title = appointment.serviceName ?: "Tu Cita",
        subtitle = "$displayDateTime con ${appointment.specialist.name}",
        icon = Icons.Default.Event,
        onClick = { nav.navigate(Screen.Scheduled.route) }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    val suggestion = when {
                        weather?.weather?.firstOrNull()?.description?.contains("rain", ignoreCase = true) == true ->
                            "¡Va a llover! No olvides tu paraguas."
                        (weather?.main?.temp ?: 20.0) > 28.0 -> 
                            "¡Día caluroso! Mantente hidratado."
                        else -> "¡Día ideal para tu cita!"
                    }
                    
                    Text(
                        suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (weather != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${weather.main.temp.toInt()}°C", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Text(weather.name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PointCheckButton(
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
            }
        }
    }
}

@Composable
fun FavoriteSpecialistCard(specialist: FavoriteSpecialistDto, onClick: () -> Unit) {
    PointCheckCard(
        title = specialist.name,
        modifier = Modifier.width(160.dp),
        subtitle = specialist.specialty ?: "Especialista",
        icon = Icons.Default.Person,
        onClick = onClick
    )
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
            modifier = Modifier.fillMaxWidth().height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null && categories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(140.dp),
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
            modifier = Modifier.fillMaxWidth().height(140.dp),
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

    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        items(categories, key = { it.id ?: it.name }) { cat ->
            val icon = CategoryIdentityMapper.mapIcon(cat.icon)
            
            CategoryCard(
                name = cat.name, 
                icon = icon, 
                modifier = Modifier.width(145.dp) // Ajustado para visibilidad de scroll
            ) {
                if (!cat.id.isNullOrBlank()) {
                    nav.navigate(Screen.Booking.createRoute(null, cat.id))
                }
            }
        }
    }
}

@Composable
fun CategoryCard(name: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    PointCheckCard(
        title = name,
        modifier = modifier.height(125.dp), // Aumentado para dar aire al texto
        icon = icon,
        isVertical = true,
        titleMaxLines = 2,
        onClick = onClick
    )
}
