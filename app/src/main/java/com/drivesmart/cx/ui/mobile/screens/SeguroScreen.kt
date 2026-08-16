package com.drivesmart.cx.ui.mobile.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.drivesmart.cx.data.local.entity.SeguroEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguroScreen(viewModel: DriveSmartViewModel) {
    val seguro by viewModel.currentSeguro.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información del Seguro") },
                actions = {
                    if (seguro != null && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (seguro == null || isEditing) {
            SeguroForm(
                seguro = seguro,
                onSave = {
                    viewModel.saveSeguro(
                        id = seguro?.id ?: 0,
                        aseguradora = it.aseguradora,
                        poliza = it.numeroPoliza,
                        inicio = it.fechaInicio,
                        vencimiento = it.fechaVencimiento,
                        tel = it.telefonoSiniestros,
                        cobertura = it.tipoCobertura,
                        notas = it.notas,
                        documentUri = it.documentUri
                    )
                    isEditing = false
                },
                onCancel = { isEditing = false },
                modifier = Modifier.padding(padding)
            )
        } else {
            SeguroDetail(
                seguro = seguro!!, 
                modifier = Modifier.padding(padding),
                onUpdate = { viewModel.saveSeguro(
                    id = it.id,
                    aseguradora = it.aseguradora,
                    poliza = it.numeroPoliza,
                    inicio = it.fechaInicio,
                    vencimiento = it.fechaVencimiento,
                    tel = it.telefonoSiniestros,
                    cobertura = it.tipoCobertura,
                    notas = it.notas,
                    documentUri = it.documentUri
                )}
            )
        }
    }
}

@Composable
fun SeguroDetail(seguro: SeguroEntity, modifier: Modifier = Modifier, onUpdate: (SeguroEntity) -> Unit) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onUpdate(seguro.copy(documentUri = it.toString())) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(seguro.aseguradora, style = MaterialTheme.typography.headlineMedium)
                    Text("Póliza: ${seguro.numeroPoliza}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        item {
            ListItem(
                headlineContent = { Text(seguro.tipoCobertura) },
                supportingContent = { Text("Tipo de Cobertura") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Vence el:", style = MaterialTheme.typography.labelSmall)
                        Text(sdf.format(Date(seguro.fechaVencimiento)), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clickable {
                                if (seguro.telefonoSiniestros.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${seguro.telefonoSiniestros}")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                    ) {
                        Text("Emergencias:", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(seguro.telefonoSiniestros, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        item {
            Text("Póliza Digital", style = MaterialTheme.typography.titleSmall)
            if (seguro.documentUri != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (seguro.documentUri.contains("image")) {
                            AsyncImage(
                                model = seguro.documentUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(seguro.documentUri), "image/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp))
                            Text("Documento Guardado", style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(seguro.documentUri), "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Manejar si no hay app para PDF
                                }
                            }) {
                                Text("Ver Documento")
                            }
                        }
                        
                        TextButton(onClick = { onUpdate(seguro.copy(documentUri = null)) }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Eliminar Documento")
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { documentPickerLauncher.launch(arrayOf("image/*", "application/pdf")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cargar Póliza (PDF o Imagen)")
                }
            }
        }

        if (!seguro.notas.isNullOrBlank()) {
            item {
                Text("Notas", style = MaterialTheme.typography.titleSmall)
                Text(seguro.notas, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguroForm(
    seguro: SeguroEntity?,
    onSave: (SeguroEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var aseguradora by remember { mutableStateOf(seguro?.aseguradora ?: "") }
    var poliza by remember { mutableStateOf(seguro?.numeroPoliza ?: "") }
    var tel by remember { mutableStateOf(seguro?.telefonoSiniestros ?: "") }
    var cobertura by remember { mutableStateOf(seguro?.tipoCobertura ?: "Amplia") }
    var notas by remember { mutableStateOf(seguro?.notas ?: "") }
    var documentUri by remember { mutableStateOf(seguro?.documentUri) }
    
    var vencimiento by remember { mutableStateOf(seguro?.fechaVencimiento ?: System.currentTimeMillis()) }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = vencimiento)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { vencimiento = it }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { OutlinedTextField(value = aseguradora, onValueChange = { aseguradora = it }, label = { Text("Aseguradora") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = poliza, onValueChange = { poliza = it }, label = { Text("Número de Póliza") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = tel, onValueChange = { tel = it }, label = { Text("Teléfono de Siniestros") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = cobertura, onValueChange = { cobertura = it }, label = { Text("Tipo de Cobertura") }, modifier = Modifier.fillMaxWidth()) }
        
        item {
            OutlinedTextField(
                value = sdf.format(Date(vencimiento)),
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha de Vencimiento") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { 
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = null) 
                    }
                }
            )
        }

        item { OutlinedTextField(value = notas, onValueChange = { notas = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (seguro != null) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                }
                Button(
                    onClick = {
                        onSave(
                            SeguroEntity(
                                id = seguro?.id ?: 0,
                                vehiculoId = seguro?.vehiculoId ?: 0,
                                aseguradora = aseguradora,
                                numeroPoliza = poliza,
                                fechaInicio = seguro?.fechaInicio ?: System.currentTimeMillis(),
                                fechaVencimiento = vencimiento,
                                telefonoSiniestros = tel,
                                tipoCobertura = cobertura,
                                notas = notas,
                                documentUri = documentUri
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = aseguradora.isNotBlank() && poliza.isNotBlank()
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
