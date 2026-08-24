package com.drivesmart.cx.ui.mobile.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import java.util.Locale

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

    var showVehicleSelectionDialog by remember { mutableStateOf(false) }
    val vehicles by viewModel.allVehicles.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configuración") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sección: Personalización
            item {
                ConfigSection(title = "Personalización", icon = Icons.Default.Settings) {
                    Text(
                        "Color de la App",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colors) { hex ->
                            val color = if (hex == null) MaterialTheme.colorScheme.surfaceVariant else Color(android.graphics.Color.parseColor(hex))
                            val isSelected = appColorHex == hex
                            
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAppPrimaryColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (hex == null) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Auto", modifier = Modifier.size(18.dp))
                                } else if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Sección: Alertas
            item {
                val tramiteAlertDays by viewModel.tramiteAlertDays.collectAsState()
                val servicioAlertKm by viewModel.servicioAlertKm.collectAsState()
                val servicioAlertDays by viewModel.servicioAlertDays.collectAsState()

                ConfigSection(title = "Gestión de Alertas", icon = Icons.Default.Notifications) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                            subtitle = "Días antes de la fecha",
                            value = servicioAlertDays,
                            onValueChange = { viewModel.setServicioAlertDays(it) },
                            range = 7..365
                        )
                    }
                }
            }

            // Sección: Datos y Reportes
            item {
                ConfigSection(title = "Datos y Reportes", icon = Icons.Default.Info) {
                    Column {
                        SettingsActionItem(
                            title = "Ver Reporte de Mantenimiento",
                            description = "Generar PDF detallado por vehículo",
                            icon = Icons.Default.Share,
                            onClick = { 
                                val currentVehicles = vehicles ?: emptyList()
                                if (currentVehicles.size <= 1) {
                                    viewModel.exportPdf(context, currentVehicles.firstOrNull()?.id) { uri ->
                                        uri?.let { openPdf(context, it) }
                                    }
                                } else {
                                    showVehicleSelectionDialog = true 
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                        SettingsActionItem(
                            title = "Exportar a Excel (CSV)",
                            description = "Lista de gastos y servicios",
                            icon = Icons.Default.Menu,
                            onClick = { exportCsvLauncher.launch("DriveSmartCX_Gastos.csv") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                        SettingsActionItem(
                            title = "Crear Copia de Seguridad",
                            description = "Exportar todos los datos (JSON)",
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = { exportJsonLauncher.launch("DriveSmartCX_Backup.json") }
                        )
                        SettingsActionItem(
                            title = "Restaurar Copia de Seguridad",
                            description = "Importar datos desde archivo JSON",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { importJsonLauncher.launch(arrayOf("application/json", "application/octet-stream")) }
                        )
                    }
                }
            }

            // Sección: Seguridad
            item {
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
                ConfigSection(title = "Seguridad", icon = Icons.Default.Lock) {
                    ListItem(
                        headlineContent = { Text("Autenticación Biométrica", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text("Solicitar huella o rostro al abrir la app") },
                        trailingContent = { 
                            Switch(
                                checked = isBiometricEnabled, 
                                onCheckedChange = { viewModel.setBiometricEnabled(it) }
                            ) 
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "DriveSmartCX v1.4",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    if (showVehicleSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showVehicleSelectionDialog = false },
            title = { Text("Seleccionar Vehículo") },
            text = {
                Column {
                    Text("¿Para qué vehículo deseas generar el reporte?")
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column {
                            TextButton(
                                onClick = {
                                    viewModel.exportPdf(context, null) { uri ->
                                        uri?.let { openPdf(context, it) }
                                    }
                                    showVehicleSelectionDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Todos los Vehículos", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
                            }
                            vehicles?.forEach { vehicle ->
                                TextButton(
                                    onClick = {
                                        viewModel.exportPdf(context, vehicle.id) { uri ->
                                            uri?.let { openPdf(context, it) }
                                        }
                                        showVehicleSelectionDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(vehicle.nombre, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVehicleSelectionDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ConfigSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(description, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp)) },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
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
                modifier = Modifier.width(54.dp),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun openPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Ver Reporte PDF"))
    } catch (e: Exception) {
        com.drivesmart.cx.util.AppLogger.error("ConfigScreen", "Error al abrir visor PDF", e)
        android.widget.Toast.makeText(context, "No hay una aplicación para ver PDFs instalada", android.widget.Toast.LENGTH_SHORT).show()
    }
}
