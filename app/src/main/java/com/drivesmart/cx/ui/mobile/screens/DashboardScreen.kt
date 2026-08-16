package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.NumberFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DriveSmartViewModel,
    onNavigateToGastos: () -> Unit,
    onNavigateToServicios: () -> Unit,
    onNavigateToTramites: () -> Unit,
    onNavigateToEmergencias: () -> Unit,
    onNavigateToBitacora: () -> Unit,
    onNavigateToEstacionamiento: () -> Unit,
    onNavigateToSeguro: () -> Unit,
    onNavigateToPreventivos: () -> Unit,
    onNavigateToInfoVehiculo: () -> Unit,
    onNavigateToGarage: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onEditVehicle: (Long) -> Unit
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val selectedId by viewModel.selectedVehicleId.collectAsState()
    
    val currentVehicles = vehicles ?: emptyList()
    val currentVehicle = currentVehicles.find { it.id == selectedId } ?: currentVehicles.firstOrNull()

    val servicios by viewModel.currentServicios.collectAsState()
    val tramites by viewModel.currentTramites.collectAsState()

    val alerts = remember(servicios, tramites, currentVehicle) {
        val criticalItems = (servicios.filter { it.estatus == "Crítico" }.map { "Servicio: ${it.nombre} (CRÍTICO)" } +
                            tramites.filter { it.estatus == "Crítico" }.map { "Trámite: ${it.nombre} (CRÍTICO)" })
        
        val upcomingItems = mutableListOf<String>()
        val currentTime = System.currentTimeMillis()
        val oneMonthMs = 30L * 24 * 60 * 60 * 1000
        val threeMonthsMs = 90L * 24 * 60 * 60 * 1000

        tramites.filter { it.estatus == "Pendiente" && it.fechaVencimiento - currentTime < oneMonthMs }
            .forEach { upcomingItems.add("Vence pronto: ${it.nombre}") }

        currentVehicle?.let { v ->
            servicios.filter { it.estatus == "Pendiente" }.forEach { s ->
                val kmLeft = if (s.proximoKilometraje != null) s.proximoKilometraje!! - v.kilometrajeActual else Double.MAX_VALUE
                val timeLeft = if (s.proximaFecha != null) s.proximaFecha!! - currentTime else Long.MAX_VALUE
                
                if (kmLeft <= 1000 || (timeLeft > 0 && timeLeft <= threeMonthsMs)) {
                    val detail = if (kmLeft <= 1000) "${NumberFormatter.formatKm(s.proximoKilometraje!!)} KM" 
                                 else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(s.proximaFecha!!))
                    upcomingItems.add("Próximo servicio: ${s.nombre} ($detail)")
                }
            }
        }

        criticalItems + upcomingItems
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DriveSmartCX") },
                actions = {
                    IconButton(onClick = onNavigateToGarage) {
                        Icon(Icons.Default.Home, contentDescription = "Garage")
                    }
                    IconButton(onClick = onNavigateToInfoVehiculo) {
                        Icon(Icons.Default.Info, contentDescription = "Información Vehículo")
                    }
                    IconButton(onClick = onNavigateToConfig) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bienvenido, ${currentVehicle?.nombre ?: "Usuario"}",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (currentVehicle != null) {
                        val brand = com.drivesmart.cx.util.VehicleBrand.fromString(currentVehicle.marca)
                        val brandColor = if (currentVehicle.marca == "Otro" && currentVehicle.customColorHex != null) {
                            try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(currentVehicle.customColorHex)) } catch (e: Exception) { brand.color }
                        } else brand.color
                        
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = brand.iconRes),
                            contentDescription = currentVehicle.customMarca ?: brand.displayName,
                            tint = brandColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            item {
                VehicleSelectorCard(
                    vehicleName = currentVehicle?.nombre ?: "Sin Vehículo",
                    placas = currentVehicle?.placas ?: "---",
                    km = currentVehicle?.kilometrajeActual ?: 0.0,
                    onEdit = { currentVehicle?.let { onEditVehicle(it.id) } }
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Gastos",
                        icon = Icons.Default.ShoppingCart,
                        onClick = onNavigateToGastos
                    )
                    MenuActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Servicio",
                        icon = Icons.Default.Build,
                        onClick = onNavigateToServicios
                    )
                    MenuActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Trámites",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToTramites
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Bitácora",
                        icon = Icons.Default.LocationOn,
                        onClick = onNavigateToBitacora
                    )
                    MenuActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Seguro",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToSeguro
                    )
                    MenuActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Revision",
                        icon = Icons.Default.Build,
                        onClick = onNavigateToPreventivos
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Parking",
                        icon = Icons.Default.Place,
                        onClick = onNavigateToEstacionamiento
                    )
                    Spacer(modifier = Modifier.weight(2f))
                }
            }

            item {
                MenuActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Emergencias",
                    icon = Icons.Default.Warning,
                    onClick = onNavigateToEmergencias
                )
            }

            item {
                Text("Alertas y Pendientes", style = MaterialTheme.typography.titleMedium)
            }
            
            if (alerts.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        ListItem(
                            headlineContent = { Text("Sin alertas por el momento") },
                            supportingContent = { Text("Todo está al día con tu Vehículo.") },
                            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                    }
                }
            } else {
                items(alerts.size) { index ->
                    val alert = alerts[index]
                    val isCritical = alert.contains("(CRÍTICO)")
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = { Text(alert) },
                            leadingContent = { 
                                Icon(
                                    if (isCritical) Icons.Default.Warning else Icons.Default.Notifications, 
                                    contentDescription = null,
                                    tint = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                                ) 
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleSelectorCard(vehicleName: String, placas: String, km: Double, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = vehicleName, style = MaterialTheme.typography.headlineSmall)
                Text(text = "Placas: $placas", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${NumberFormatter.formatKm(km)} KM", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Vehículo")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuActionCard(modifier: Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title)
        }
    }
}
