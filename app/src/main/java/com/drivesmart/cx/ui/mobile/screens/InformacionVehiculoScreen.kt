package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.NumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformacionVehiculoScreen(viewModel: DriveSmartViewModel) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val selectedId by viewModel.selectedVehicleId.collectAsState()
    val vehicle = vehicles?.find { it.id == selectedId } ?: vehicles?.firstOrNull()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Información del Vehículo") }) }
    ) { padding ->
        if (vehicle == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No hay vehículo seleccionado")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InfoCard("Nombre", vehicle.nombre, Icons.Default.Info)
                }
                item {
                    InfoCard("Modelo / Versión", vehicle.modelo, Icons.Default.Build)
                }
                item {
                    InfoCard("Año", vehicle.anio.toString(), Icons.Default.Star)
                }
                item {
                    InfoCard("Placas", vehicle.placas, Icons.Default.Settings)
                }
                item {
                    InfoCard("VIN (Número de Serie)", vehicle.vin, Icons.Default.Info)
                }
                item {
                    InfoCard("Kilometraje Actual", "${NumberFormatter.formatKm(vehicle.kilometrajeActual)} KM", Icons.Default.Build)
                }
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(value, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(label) },
            leadingContent = { Icon(icon, contentDescription = null) }
        )
    }
}
