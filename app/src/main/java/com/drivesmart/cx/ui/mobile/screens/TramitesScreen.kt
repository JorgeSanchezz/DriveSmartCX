package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.local.entity.TramiteEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TramitesScreen(viewModel: DriveSmartViewModel) {
    val tramites by viewModel.currentTramites.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedTramite by remember { mutableStateOf<TramiteEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Trámites y Documentación") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedTramite = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Trámite")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tramites.isEmpty()) {
                item {
                    Text("No hay trámites registrados", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(tramites) { tramite ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                                Text(tramite.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Vence: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(tramite.fechaVencimiento))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    tramite.estatus, 
                                    color = when(tramite.estatus) {
                                        "Crítico" -> MaterialTheme.colorScheme.error
                                        "Pendiente" -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            IconButton(onClick = { 
                                selectedTramite = tramite
                                showDialog = true 
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { viewModel.removeTramite(tramite) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddTramiteDialog(
                tramite = selectedTramite,
                onDismiss = { showDialog = false },
                onConfirm = { nombre, estatus, fecha, descripcion ->
                    viewModel.saveTramite(
                        id = selectedTramite?.id ?: 0,
                        nombre = nombre,
                        fechaVencimiento = fecha,
                        estatus = estatus,
                        descripcion = descripcion
                    )
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTramiteDialog(
    tramite: TramiteEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(tramite?.nombre ?: "") }
    var estatus by remember { mutableStateOf(tramite?.estatus ?: "Pendiente") }
    var descripcion by remember { mutableStateOf(tramite?.descripcion ?: "") }
    var fechaVencimiento by remember { mutableLongStateOf(tramite?.fechaVencimiento ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fechaVencimiento)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fechaVencimiento = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tramite == null) "Nuevo Trámite" else "Editar Trámite") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Trámite") }, modifier = Modifier.fillMaxWidth())
                
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

                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaVencimiento)),
                    onValueChange = { },
                    label = { Text("Vigencia (Fecha)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Cambiar Fecha")
                        }
                    }
                )

                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción / Nota") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, estatus, fechaVencimiento, descripcion) }) {
                Text(if (tramite == null) "Guardar" else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
