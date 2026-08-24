package com.drivesmart.cx.ui.mobile.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.drivesmart.cx.data.local.entity.ErrorLogEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorLogScreen(
    viewModel: DriveSmartViewModel,
    onBack: () -> Unit
) {
    val logs by viewModel.errorLogs.collectAsState()
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }
    
    var selectedLog by remember { mutableStateOf<ErrorLogEntity?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Errores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { 
                            val text = logs.joinToString("\n\n") { log ->
                                formatLogTechnical(log, sdf)
                            }
                            shareLogs(context, text)
                        }) {
                            Icon(Icons.Default.Email, contentDescription = "Enviar todo por correo")
                        }
                        IconButton(onClick = { showDeleteAllConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar todo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay errores registrados", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs) { log ->
                    ErrorLogItem(
                        log = log,
                        sdf = sdf,
                        onClick = { selectedLog = log },
                        onDelete = { viewModel.removeErrorLog(log) },
                        onCopy = { 
                            val text = formatLogTechnical(log, sdf)
                            copyToClipboard(context, text) 
                        },
                        onShare = {
                            val text = formatLogTechnical(log, sdf)
                            shareLogs(context, text, "Error Log: ${log.tag}")
                        }
                    )
                }
            }
        }
    }

    if (selectedLog != null) {
        AlertDialog(
            onDismissRequest = { selectedLog = null },
            title = { Text("Detalle Técnico (Logcat)") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Tag: ${selectedLog!!.tag}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Fecha: ${sdf.format(Date(selectedLog!!.timestamp))}", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Mensaje:", fontWeight = FontWeight.Bold)
                    Text(selectedLog!!.message)
                    
                    if (!selectedLog!!.stackTrace.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Stack Trace:", fontWeight = FontWeight.Bold)
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = selectedLog!!.stackTrace!!,
                                modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFCCCCCC)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { 
                        val text = formatLogTechnical(selectedLog!!, sdf)
                        copyToClipboard(context, text)
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copiar")
                    }
                    TextButton(onClick = { 
                        val text = formatLogTechnical(selectedLog!!, sdf)
                        shareLogs(context, text, "Error ${selectedLog!!.tag}")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLog = null }) { Text("Cerrar") }
            }
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text("¿Borrar todos los logs?") },
            text = { Text("Esta acción eliminará definitivamente el historial de errores.") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.clearAllErrorLogs()
                        showDeleteAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar Todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ErrorLogItem(
    log: ErrorLogEntity,
    sdf: SimpleDateFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = sdf.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.padding(start = 28.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.Edit, contentDescription = "Copiar", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { showConfirmDelete = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("¿Borrar este log?") },
            confirmButton = {
                TextButton(onClick = { 
                    onDelete()
                    showConfirmDelete = false
                }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun formatLogTechnical(log: ErrorLogEntity, sdf: SimpleDateFormat): String {
    return """
        --- DRIVE SMART CX TECHNICAL LOG ---
        Timestamp: ${sdf.format(Date(log.timestamp))}
        Tag: ${log.tag}
        Message: ${log.message}
        
        Stack Trace:
        ${log.stackTrace ?: "No stack trace available"}
        -------------------------------------
    """.trimIndent()
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Error Log", text)
    clipboard.setPrimaryClip(clip)
    android.widget.Toast.makeText(context, "Detalle técnico copiado", android.widget.Toast.LENGTH_SHORT).show()
}

private fun shareLogs(context: Context, text: String, subject: String = "DriveSmartCX Error Logs") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Enviar Logs"))
}
