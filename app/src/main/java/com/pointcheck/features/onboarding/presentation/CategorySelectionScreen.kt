package com.pointcheck.features.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.features.auth.presentation.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    nav: NavController,
    authVm: UserViewModel,
    vm: CategoryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("¿Cuál es tu especialidad?") }) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text(
                    "Selecciona la categoría que mejor describa tus servicios",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.categories) { category ->
                        CategoryCard(
                            name = category.name,
                            iconName = category.icon,
                            colorHex = category.color,
                            onClick = {
                                authVm.onValueChange("categoryId", category.id.toString())
                                nav.navigate("service_configuration/${category.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    name: String,
    iconName: String,
    colorHex: String,
    onClick: () -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getIconByName(iconName),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

fun getIconByName(name: String): ImageVector {
    return when (name) {
        "ContentCut" -> Icons.Default.ContentCut
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "MedicalServices" -> Icons.Default.MedicalServices
        "Brush" -> Icons.Default.Brush
        "AutoFixHigh" -> Icons.Default.AutoFixHigh
        "HomeRepairService" -> Icons.Default.HomeRepairService
        else -> Icons.Default.Star
    }
}
