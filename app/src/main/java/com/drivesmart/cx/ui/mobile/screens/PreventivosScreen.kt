package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.local.entity.PreventivoEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventivosScreen(viewModel: DriveSmartViewModel) {
    val preventivos by viewModel.currentPreventivos.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<PreventivoEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Revision") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedItem = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Item")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (preventivos.isEmpty()) {
                item { Text("No hay Revision programada. Añade una para empezar.") }
            } else {
                items(preventivos) { item ->
                    PreventivoCard(
                        item = item,
                        onCheck = { viewModel.updatePreventivoRevision(item) },
                        onEdit = {
                            selectedItem = item
                            showDialog = true
                        },
                        onDelete = { viewModel.removePreventivo(item) }
                    )
                }
            }
        }

        if (showDialog) {
            AddPreventivoDialog(
                item = selectedItem,
                onDismiss = { showDialog = false },
                onConfirm = { nombre, dias, notas ->
                    viewModel.savePreventivo(
                        id = selectedItem?.id ?: 0,
                        nombre = nombre, 
                        dias = dias,
                        notas = notas
                    )
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun PreventivoCard(item: PreventivoEntity, onCheck: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val nextDate = item.ultimaRevision + (item.frecuenciaDias.toLong() * 24 * 60 * 60 * 1000)
    val isOverdue = System.currentTimeMillis() > nextDate
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, style = MaterialTheme.typography.titleMedium)
                Text("Ultima Revision: ${sdf.format(Date(item.ultimaRevision))}", style = MaterialTheme.typography.bodySmall)
                Text("Proxima Revision: ${sdf.format(Date(nextDate))}", style = MaterialTheme.typography.bodySmall, color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                if (!item.notas.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.notas, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            IconButton(onClick = onCheck) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Marcar como hecho", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddPreventivoDialog(item: PreventivoEntity? = null, onDismiss: () -> Unit, onConfirm: (String, Int, String?) -> Unit) {
    var nombre by remember { mutableStateOf(item?.nombre ?: "") }
    var dias by remember { mutableStateOf(item?.frecuenciaDias?.toString() ?: "30") }
    var notas by remember { mutableStateOf(item?.notas ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Nueva Revision" else "Editar Revision") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("¿Qué revisar? (Ej: Llantas)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = dias, 
                    onValueChange = { dias = it }, 
                    label = { Text("Frecuencia de Revision (Días)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas / Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, dias.toIntOrNull() ?: 30, notas.ifBlank { null }) }, enabled = nombre.isNotBlank()) {
                Text(if (item == null) "Añadir" else "Actualizar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
