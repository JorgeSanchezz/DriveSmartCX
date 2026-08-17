package com.drivesmart.cx.ui.mobile.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(viewModel: DriveSmartViewModel) {
    val context = LocalContext.current
    val appColorHex by viewModel.appPrimaryColor.collectAsState()

    val colors = listOf(
        null, // Automático
        "#607D8B", // Default
        "#E31837", // Rojo
        "#003478", // Azul
        "#4CAF50", // Verde
        "#FF9800", // Naranja
        "#9C27B0", // Morado
        "#009688", // Teal
        "#000000"  // Negro
    )

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(context, it) }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(context, it) }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.exportCsv(context, it) }
    }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { viewModel.exportPdf(context, it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configuración") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Personalización", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Color de la App", style = MaterialTheme.typography.bodySmall)
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    items(colors) { hex ->
                        val color = if (hex == null) MaterialTheme.colorScheme.surfaceVariant else Color(android.graphics.Color.parseColor(hex))
                        val isSelected = appColorHex == hex
                        
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setAppPrimaryColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (hex == null) {
                                Icon(Icons.Default.Refresh, contentDescription = "Auto", modifier = Modifier.size(20.dp))
                            } else if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Text("Configuración de Alertas", style = MaterialTheme.typography.titleMedium)
                
                val tramiteAlertDays by viewModel.tramiteAlertDays.collectAsState()
                val servicioAlertKm by viewModel.servicioAlertKm.collectAsState()
                val servicioAlertDays by viewModel.servicioAlertDays.collectAsState()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AlertSettingItem(
                        title = "Anticipación para Trámites",
                        subtitle = "Días antes del vencimiento",
                        value = tramiteAlertDays,
                        onValueChange = { viewModel.setTramiteAlertDays(it) },
                        range = 1..180
                    )
                    
                    AlertSettingItem(
                        title = "Anticipación para Servicios (KM)",
                        subtitle = "Kilómetros antes del servicio",
                        value = servicioAlertKm,
                        onValueChange = { viewModel.setServicioAlertKm(it) },
                        range = 100..5000,
                        step = 100
                    )

                    AlertSettingItem(
                        title = "Anticipación para Servicios (Tiempo)",
                        subtitle = "Días antes de la fecha programada",
                        value = servicioAlertDays,
                        onValueChange = { viewModel.setServicioAlertDays(it) },
                        range = 7..365
                    )
                }
            }

            item { HorizontalDivider() }

            item {
                Text("Respaldo y Exportación", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { exportJsonLauncher.launch("DriveSmartCX_Backup.json") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Backup (JSON)")
                    }

                    Button(
                        onClick = { importJsonLauncher.launch(arrayOf("application/json", "application/octet-stream")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importar Backup (JSON)")
                    }

                    Button(
                        onClick = { exportCsvLauncher.launch("DriveSmartCX_Gastos.csv") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Gastos y Servicios (CSV)")
                    }

                    Button(
                        onClick = { exportPdfLauncher.launch("Reporte_DriveSmartCX.pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Reporte de Mantenimiento (PDF)")
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Text("Seguridad", style = MaterialTheme.typography.titleMedium)
                
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

                ListItem(
                    headlineContent = { Text("Autenticación Biométrica") },
                    supportingContent = { Text("Solicitar huella al abrir la app") },
                    trailingContent = { 
                        Switch(
                            checked = isBiometricEnabled, 
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        ) 
                    }
                )
            }
        }
    }
}

@Composable
fun AlertSettingItem(
    title: String,
    subtitle: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1
) {
    val steps = if (step > 1) (range.last - range.first) / step - 1 else 0
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = steps,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.End
            )
        }
    }
}
