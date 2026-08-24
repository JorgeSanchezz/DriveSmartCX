package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.local.entity.ContactoEmergenciaEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergenciasSOSScreen(viewModel: DriveSmartViewModel) {
    val sosContacts by viewModel.currentSOSContacts.collectAsState()
    val sosMessage by viewModel.sosMessage.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var countdownSeconds by remember { mutableIntStateOf(0) }
    var isCountdownActive by remember { mutableStateOf(false) }

    LaunchedEffect(isCountdownActive, countdownSeconds) {
        if (isCountdownActive && countdownSeconds > 0) {
            delay(1000)
            countdownSeconds -= 1
            if (countdownSeconds == 0) {
                isCountdownActive = false
                NotificationHelper.sendSOS(context, sosContacts, sosMessage)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configurar Botón SOS") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("BOTÓN DE PÁNICO", style = MaterialTheme.typography.titleLarge, color = Color.Red)
                        Text(
                            "Al presionar este botón, se enviará un SMS con tu ubicación real a tus contactos seleccionados.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                countdownSeconds = 5
                                isCountdownActive = true 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            enabled = sosContacts.isNotEmpty() && !isCountdownActive
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("ENVIAR SOS AHORA")
                        }
                    }
                }
            }

            item {
                Text("Mensaje Predeterminado", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = sosMessage,
                    onValueChange = { viewModel.setSosMessage(it) },
                    label = { Text("Mensaje a enviar") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Contactos SOS (${sosContacts.size}/5)", style = MaterialTheme.typography.titleMedium)
                    if (sosContacts.size < 5) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir contacto")
                        }
                    }
                }
            }

            items(sosContacts) { contacto ->
                ListItem(
                    headlineContent = { Text(contacto.nombre) },
                    supportingContent = { Text(contacto.telefono) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removeSOSContact(contacto) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddSOSContactDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { nombre, tel ->
                    viewModel.saveSOSContact(nombre, tel)
                    showAddDialog = false
                }
            )
        }

        if (isCountdownActive) {
            AlertDialog(
                onDismissRequest = { /* No cerrar al tocar fuera */ },
                confirmButton = {
                    TextButton(
                        onClick = { isCountdownActive = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("CANCELAR ENVÍO")
                    }
                },
                title = { Text("Enviando SOS...") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = countdownSeconds.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.Red
                        )
                        Text("El mensaje se enviará automáticamente en $countdownSeconds segundos.")
                    }
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) }
            )
        }
    }
}

@Composable
fun AddSOSContactDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var tel by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(nombre, tel) }, enabled = nombre.isNotBlank() && tel.isNotBlank()) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Añadir Contacto SOS") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = tel,
                    onValueChange = { tel = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
