package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.local.entity.ContactoEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.NotificationHelper
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.automirrored.filled.Send

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergenciasScreen(
    viewModel: DriveSmartViewModel,
    onNavigateToSOSConfig: () -> Unit
) {
    val contactos by viewModel.currentContactos.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedContacto by remember { mutableStateOf<ContactoEntity?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergencias y Asistencia") },
                actions = {
                    IconButton(onClick = onNavigateToSOSConfig) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar SOS", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedContacto = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Contacto")
            }
        }
    ) { padding ->
        val sosContacts by viewModel.currentSOSContacts.collectAsState()
        val sosMessage by viewModel.sosMessage.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text("Modo SOS", style = MaterialTheme.typography.titleLarge, color = Color.Red)
                        }
                        Text(
                            "Envía un mensaje de emergencia con tu ubicación a tus contactos configurados.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { NotificationHelper.sendSOS(context, sosContacts, sosMessage) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = sosContacts.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("ENVIAR SOS")
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "En caso de siniestro, contacta a tu seguro inmediatamente.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            item {
                Text("Contactos de Auxilio", style = MaterialTheme.typography.titleMedium)
            }

            if (contactos.isEmpty()) {
                item {
                    ListItem(
                        headlineContent = { Text("No hay contactos registrados") },
                        supportingContent = { Text("Agrega números de asistencia vial o seguros.") }
                    )
                }
            } else {
                items(contactos) { contacto ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                                Text(contacto.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(contacto.tipo, style = MaterialTheme.typography.bodySmall)
                                Text(contacto.telefono, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { 
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacto.telefono}"))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Call, contentDescription = "Llamar", tint = Color(0xFF4CAF50))
                            }
                            IconButton(onClick = { 
                                selectedContacto = contacto
                                showDialog = true 
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { viewModel.removeContacto(contacto) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddContactoDialog(
                contacto = selectedContacto,
                onDismiss = { showDialog = false },
                onConfirm = { nombre, tipo, telefono ->
                    viewModel.saveContacto(
                        id = selectedContacto?.id ?: 0,
                        nombre = nombre,
                        tipo = tipo,
                        telefono = telefono
                    )
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddContactoDialog(
    contacto: ContactoEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(contacto?.nombre ?: "") }
    var tipo by remember { mutableStateOf(contacto?.tipo ?: "Seguro") }
    var telefono by remember { mutableStateOf(contacto?.telefono ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contacto == null) "Nuevo Contacto" else "Editar Contacto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (Seguro/Mecánico/etc)") })
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre, tipo, telefono) }) {
                Text(if (contacto == null) "Guardar" else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
