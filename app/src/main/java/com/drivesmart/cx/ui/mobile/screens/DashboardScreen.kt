package com.drivesmart.cx.ui.mobile.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.data.local.entity.ServicioEntity
import com.drivesmart.cx.data.local.entity.TramiteEntity
import com.drivesmart.cx.util.NumberFormatter
import com.drivesmart.cx.util.VehicleBrand
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateToErrorLogs: () -> Unit,
    onEditVehicle: (Long) -> Unit
) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val selectedId by viewModel.selectedVehicleId.collectAsState()
    
    val currentVehicles = vehicles ?: emptyList()
    val currentVehicle = currentVehicles.find { it.id == selectedId } ?: currentVehicles.firstOrNull()

    val servicios by viewModel.currentServicios.collectAsState()
    val tramites by viewModel.currentTramites.collectAsState()
    
    val tramiteAlertDays by viewModel.tramiteAlertDays.collectAsState()
    val servicioAlertKm by viewModel.servicioAlertKm.collectAsState()
    val servicioAlertDays by viewModel.servicioAlertDays.collectAsState()

    val alerts = remember(servicios, tramites, currentVehicle, tramiteAlertDays, servicioAlertKm, servicioAlertDays) {
        val criticalItems = (servicios.filter { it.estatus == "Crítico" }.map { "Servicio: ${it.nombre} (CRÍTICO)" } +
                            tramites.filter { it.estatus == "Crítico" }.map { "Trámite: ${it.nombre} (CRÍTICO)" })
        
        val upcomingItems = mutableListOf<String>()
        val currentTime = System.currentTimeMillis()
        val oneMonthMs = tramiteAlertDays.toLong() * 24 * 60 * 60 * 1000
        val serviceDaysMs = servicioAlertDays.toLong() * 24 * 60 * 60 * 1000

        tramites.filter { it.estatus == "Pendiente" && it.fechaVencimiento - currentTime < oneMonthMs }
            .forEach { upcomingItems.add("Vence pronto: ${it.nombre}") }

        currentVehicle?.let { v ->
            servicios.filter { it.estatus == "Pendiente" }.forEach { s ->
                val kmLeft = if (s.proximoKilometraje != null) s.proximoKilometraje!! - v.kilometrajeActual else Double.MAX_VALUE
                val timeLeft = if (s.proximaFecha != null) s.proximaFecha!! - currentTime else Long.MAX_VALUE
                
                if (kmLeft <= servicioAlertKm || (timeLeft > 0 && timeLeft <= serviceDaysMs)) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val detail = if (kmLeft <= servicioAlertKm) "${NumberFormatter.formatKm(s.proximoKilometraje!!)} KM" 
                                 else sdf.format(Date(s.proximaFecha!!))
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
                    IconButton(onClick = onNavigateToErrorLogs) {
                        Icon(Icons.Default.Warning, contentDescription = "Registro de Errores", tint = Color.Red)
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
                        val brand = VehicleBrand.fromString(currentVehicle.marca)
                        val brandColor = if (currentVehicle.marca == "Otro" && currentVehicle.customColorHex != null) {
                            try { Color(AndroidColor.parseColor(currentVehicle.customColorHex)) } catch (e: Exception) { brand.color }
                        } else brand.color
                        
                        Icon(
                            painter = painterResource(id = brand.iconRes),
                            contentDescription = currentVehicle.customMarca ?: brand.displayName,
                            tint = brandColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Banner de Alerta Prioritaria
            if (alerts.isNotEmpty()) {
                item {
                    val priorityAlert = alerts.first()
                    val isCritical = priorityAlert.contains("(CRÍTICO)")
                    
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        ),
                        onClick = {
                            if (priorityAlert.contains("Servicio")) onNavigateToServicios()
                            else if (priorityAlert.contains("Trámite") || priorityAlert.contains("Vence")) onNavigateToTramites()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (isCritical) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isCritical) "ATENCIÓN REQUERIDA" else "RECORDATORIO",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isCritical) MaterialTheme.colorScheme.onError.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = priorityAlert,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCritical) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onTertiary
                                )
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isCritical) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onTertiary
                            )
                        }
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
                                containerColor = Color.Transparent
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
                modifier = Modifier.align(Alignment.TopEnd)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title)
        }
    }
}
