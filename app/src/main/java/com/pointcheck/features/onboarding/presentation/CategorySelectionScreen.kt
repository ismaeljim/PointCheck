package com.pointcheck.features.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.*
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
        topBar = { 
            AppTopBar(
                title = "Tu Especialidad",
                onBack = { nav.popBackStack() }
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp)
            ) {
                Text(
                    "Selecciona la categoría que mejor describa lo que haces.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.categories) { category ->
                        CategoryCardV2(
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
fun CategoryCardV2(
    name: String,
    iconName: String,
    colorHex: String,
    onClick: () -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    
    AppCard(onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = getIconByName(iconName),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
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
