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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drivesmart.cx.data.local.entity.TramiteEntity
import com.drivesmart.cx.ui.mobile.components.FullScreenImageDialog
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.DateTimeUtils
import com.drivesmart.cx.util.FileHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TramitesScreen(viewModel: DriveSmartViewModel) {
    val tramites by viewModel.currentTramites.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedTramite by remember { mutableStateOf<TramiteEntity?>(null) }
    
    val sdf = remember { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp)
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
                            if (tramite.photoUri != null) {
                                AsyncImage(
                                    model = tramite.photoUri,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp).padding(4.dp).align(Alignment.Top),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                                Text(tramite.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Vence: ${sdf.format(Date(tramite.fechaVencimiento))}",
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
                onConfirm = { nombre, estatus, fecha, descripcion, photo ->
                    viewModel.saveTramite(
                        id = selectedTramite?.id ?: 0,
                        nombre = nombre,
                        fechaVencimiento = fecha,
                        estatus = estatus,
                        descripcion = descripcion,
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
fun AddTramiteDialog(
    tramite: TramiteEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, String?, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(tramite?.nombre ?: "") }
    var estatus by remember { mutableStateOf(tramite?.estatus ?: "Pendiente") }
    var descripcion by remember { mutableStateOf(tramite?.descripcion ?: "") }
    var fechaVencimiento by remember { mutableLongStateOf(tramite?.fechaVencimiento ?: System.currentTimeMillis()) }
    var photoUri by remember { mutableStateOf(tramite?.photoUri) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sdf = remember { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val internalPath = FileHelper.copyFileToInternalStorage(context, it, "tramites")
            if (internalPath != null) {
                photoUri = internalPath
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = DateTimeUtils.localToUtc(fechaVencimiento))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fechaVencimiento = DateTimeUtils.utcToLocal(it) }
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
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Trámite") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                
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
                    value = sdf.format(Date(fechaVencimiento)),
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

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Nota") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

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
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, estatus, fechaVencimiento, descripcion, photoUri) }) {
                Text(if (tramite == null) "Guardar" else "Actualizar")
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
