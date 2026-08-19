package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.remote.micodus.MicodusDevice
import com.drivesmart.cx.ui.viewmodel.MicodusViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicodusVehicleScreen(
    viewModel: MicodusViewModel
) {
    val devices by viewModel.devicesState.collectAsState()
    val error by viewModel.errorState.collectAsState()
    val imei by viewModel.savedImei.collectAsState()
    
    var showImeiDialog by remember { mutableStateOf(false) }
    var tempImei by remember { mutableStateOf(imei) }

    if (showImeiDialog) {
        AlertDialog(
            onDismissRequest = { showImeiDialog = false },
            title = { Text("Configurar mi Vehículo") },
            text = {
                Column {
                    Text("Ingresa los últimos números de tu IMEI o el nombre de tu carro.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempImei,
                        onValueChange = { tempImei = it },
                        label = { Text("IMEI / Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateImei(tempImei)
                    showImeiDialog = false
                }) { Text("Guardar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telemetría") },
                actions = {
                    IconButton(onClick = { showImeiDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (error != null) {
                Column(modifier = Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error!!, color = Color.Red, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showImeiDialog = true }) {
                        Text("Configurar mi IMEI")
                    }
                }
            } else if (devices.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { 
                        Text("Viendo tu vehículo:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(devices) { device ->
                        DeviceCard(device)
                    }
                    item { 
                        TextButton(onClick = { showImeiDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cambiar de vehículo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(device: MicodusDevice) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = device.name ?: "Dodge Attitude", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(text = "🔋 ${device.battery ?: "0"}%", fontWeight = FontWeight.Bold)
            }
            Text(text = "SN: ${device.sn ?: "---"}", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            InfoRow(label = "Estatus:", value = device.status ?: "---")
            InfoRow(label = "Velocidad:", value = "${device.speed ?: "0.0"} km/h")
            InfoRow(label = "Motor:", value = if (device.acc == "1") "Encendido" else "Apagado")
            device.distance?.let { 
                val km = it.toDoubleOrNull() ?: 0.0
                InfoRow(label = "Odómetro:", value = String.format(Locale.getDefault(), "%,.1f km", km))
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
