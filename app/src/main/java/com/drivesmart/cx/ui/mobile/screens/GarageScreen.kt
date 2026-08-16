package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.VehicleBrand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    viewModel: DriveSmartViewModel,
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val selectedId by viewModel.selectedVehicleId.collectAsState()

    val currentVehicles = vehicles ?: emptyList()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Garage") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToRegister) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentVehicles) { vehicle ->
                val isSelected = vehicle.id == (selectedId ?: currentVehicles.firstOrNull()?.id)
                val brand = VehicleBrand.fromString(vehicle.marca)
                
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { 
                        viewModel.selectVehicle(vehicle.id)
                        onBack()
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) brand.color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) AssistChipDefaults.assistChipBorder(enabled = true, borderColor = brand.color) else null
                ) {
                    ListItem(
                        headlineContent = { Text(vehicle.nombre, style = MaterialTheme.typography.titleLarge) },
                        supportingContent = { Text("${vehicle.marca} ${vehicle.modelo} (${vehicle.tipo})") },
                        trailingContent = {
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionado", tint = brand.color)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
