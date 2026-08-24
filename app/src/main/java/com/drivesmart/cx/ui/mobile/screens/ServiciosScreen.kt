package com.drivesmart.cx.ui.mobile.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drivesmart.cx.data.local.entity.ServicioEntity
import com.drivesmart.cx.ui.mobile.components.FullScreenImageDialog
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.DateTimeUtils
import com.drivesmart.cx.util.FileHelper
import com.drivesmart.cx.util.NumberFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosScreen(viewModel: DriveSmartViewModel) {
    val servicios by viewModel.currentServicios.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedServicio by remember { mutableStateOf<ServicioEntity?>(null) }

    val sdf = remember { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mantenimiento y Servicios") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedServicio = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            if (servicios.isEmpty()) {
                item {
                    Text("No hay registros", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(servicios) { servicio ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (servicio.photoUri != null) {
                                AsyncImage(
                                    model = servicio.photoUri,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp).padding(4.dp).align(Alignment.Top),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${servicio.tipo}: ${servicio.nombre}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    if (servicio.monto != null) {
                                        Text(
                                            "$${servicio.monto}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                if (servicio.tipo == "Servicio") {
                                    Text(
                                        "Último: ${sdf.format(Date(servicio.ultimaFecha))} - ${NumberFormatter.formatKm(servicio.ultimoKilometraje)} KM",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                if (servicio.proximoKilometraje != null || servicio.proximaFecha != null) {
                                    val nextKm = servicio.proximoKilometraje?.let { "${NumberFormatter.formatKm(it)} KM" } ?: ""
                                    val nextDate = servicio.proximaFecha?.let { sdf.format(Date(it)) } ?: ""
                                    val separator = if (nextKm.isNotEmpty() && nextDate.isNotEmpty()) " - " else ""
                                    val label = if (servicio.tipo == "Componente") "Instalación: " else "Próximo: "
                                    Text(
                                        "$label$nextDate$separator$nextKm",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                if (servicio.componentesIncluidos != null) {
                                    Text("Incluye: ${servicio.componentesIncluidos}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    servicio.estatus, 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    color = when(servicio.estatus) {
                                        "Crítico" -> MaterialTheme.colorScheme.error
                                        "Pendiente" -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            IconButton(onClick = { 
                                selectedServicio = servicio
                                showDialog = true 
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { viewModel.removeServicio(servicio) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddServicioDialog(
                servicio = selectedServicio,
                onDismiss = { showDialog = false },
                onConfirm = { tipo, nombre, ultimoKm, proximoKm, ultimaFecha, proximaFecha, componentes, estatus, monto, photo ->
                    viewModel.saveServicio(
                        id = selectedServicio?.id ?: 0,
                        tipo = tipo,
                        nombre = nombre,
                        ultimoKm = ultimoKm,
                        proximoKm = proximoKm,
                        ultimaFecha = ultimaFecha,
                        proximaFecha = proximaFecha,
                        componentes = componentes,
                        estatus = estatus,
                        monto = monto,
                        photoUri = photo
                    )
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServicioDialog(
    servicio: ServicioEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double?, Long, Long?, String?, String, Double?, String?) -> Unit
) {
    var tipo by remember { mutableStateOf(servicio?.tipo ?: "Servicio") }
    var nombre by remember { mutableStateOf(servicio?.nombre ?: "") }
    var ultimoKm by remember { mutableStateOf(servicio?.ultimoKilometraje?.toString() ?: "") }
    var proximoKm by remember { mutableStateOf(servicio?.proximoKilometraje?.toString() ?: "") }
    var ultimaFecha by remember { mutableLongStateOf(servicio?.ultimaFecha ?: System.currentTimeMillis()) }
    var proximaFecha by remember { mutableLongStateOf(servicio?.proximaFecha ?: (System.currentTimeMillis() + 15552000000L)) }
    var componentes by remember { mutableStateOf(servicio?.componentesIncluidos ?: "") }
    var estatus by remember { mutableStateOf(servicio?.estatus ?: "Pendiente") }
    var monto by remember { mutableStateOf(servicio?.monto?.toString() ?: "") }
    var photoUri by remember { mutableStateOf(servicio?.photoUri) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val internalPath = FileHelper.copyFileToInternalStorage(context, it, "servicios")
            if (internalPath != null) {
                photoUri = internalPath
            }
        }
    }

    var showUltimaDatePicker by remember { mutableStateOf(false) }
    var showProximaDatePicker by remember { mutableStateOf(false) }

    if (showUltimaDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = DateTimeUtils.localToUtc(ultimaFecha))
        DatePickerDialog(
            onDismissRequest = { showUltimaDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ultimaFecha = DateTimeUtils.utcToLocal(it) }
                    showUltimaDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showProximaDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = DateTimeUtils.localToUtc(proximaFecha))
        DatePickerDialog(
            onDismissRequest = { showProximaDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { proximaFecha = DateTimeUtils.utcToLocal(it) }
                    showProximaDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    val sdf = remember { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (servicio == null) "Nuevo Registro" else "Editar Registro") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Tipo de Registro", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Componente", "Servicio").forEach { option ->
                            FilterChip(
                                selected = tipo == option,
                                onClick = { tipo = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(if (tipo == "Componente") "Nombre del Componente" else "Nombre del Servicio") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }

                item {
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text(if (tipo == "Componente") "Costo del Componente ($)" else "Costo del Servicio ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                if (tipo == "Servicio") {
                    item {
                        OutlinedTextField(
                            value = ultimoKm,
                            onValueChange = { ultimoKm = it },
                            label = { Text("KM del último servicio") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = componentes,
                            onValueChange = { componentes = it },
                            label = { Text("Componentes (aceite, filtros, etc.)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = sdf.format(Date(ultimaFecha)),
                            onValueChange = { },
                            label = { Text("Fecha del último servicio") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showUltimaDatePicker = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Cambiar Fecha")
                                }
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = proximoKm,
                            onValueChange = { proximoKm = it },
                            label = { Text("Próximo cambio (KM)") },
                            placeholder = { Text("Opcional") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = sdf.format(Date(proximaFecha)),
                        onValueChange = { },
                        label = { Text(if (tipo == "Componente") "Próxima fecha de instalación (Opcional)" else "Próxima fecha de servicio (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showProximaDatePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Fecha")
                            }
                        }
                    )
                }

                item {
                    Text("Estatus", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Pendiente", "Realizado", "Crítico").forEach { option ->
                            FilterChip(
                                selected = estatus == option,
                                onClick = { estatus = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }

                item {
                    Text("Evidencia / Foto", style = MaterialTheme.typography.labelLarge)
                    if (photoUri != null) {
                        Box {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clickable { showFullScreenImage = true },
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Cambiar foto", tint = Color.White)
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = { photoUri = null },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.Red)
                                }
                            }
                        }
                    } else {
                        OutlinedButton(onClick = { photoPickerLauncher.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir Foto")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(
                    tipo, 
                    nombre, 
                    ultimoKm.toDoubleOrNull() ?: 0.0, 
                    proximoKm.toDoubleOrNull(),
                    ultimaFecha,
                    proximaFecha,
                    if (tipo == "Servicio") componentes else null,
                    estatus,
                    monto.toDoubleOrNull(),
                    photoUri
                ) 
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showFullScreenImage && photoUri != null) {
        FullScreenImageDialog(photoUri = photoUri!!, onDismiss = { showFullScreenImage = false })
    }
}
