package com.drivesmart.cx.ui.mobile.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.local.entity.BitacoraEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraScreen(viewModel: DriveSmartViewModel) {
    val bitacora by viewModel.currentBitacora.collectAsState()
    val activeViaje by viewModel.activeViaje.collectAsState()
    val context = LocalContext.current
    
    val sdfFull = remember { 
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    }
    val sdfTime = remember { 
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }
    val sdfDate = remember { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bitácora de Viajes") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Botón Iniciar/Terminar
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeViaje == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeViaje == null) "Listo para iniciar" else "Viaje en curso",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (activeViaje != null) {
                            Text(
                                text = "Iniciado: ${sdfTime.format(Date(activeViaje!!.fechaInicio))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val loc = LocationHelper.getLastKnownLocation(context)
                            if (loc == null) {
                                android.widget.Toast.makeText(context, "Sin señal GPS: Se usará ubicación 0,0", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            if (activeViaje == null) {
                                viewModel.startViaje(loc?.latitude ?: 0.0, loc?.longitude ?: 0.0)
                            } else {
                                viewModel.endViaje(loc?.latitude ?: 0.0, loc?.longitude ?: 0.0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeViaje == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(if (activeViaje == null) Icons.Default.PlayArrow else Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (activeViaje == null) "Iniciar" else "Terminar")
                    }
                }
            }

            Text("Historial de Viajes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                val filteredBitacora = bitacora.filter { it.fechaFin != null }
                items(filteredBitacora) { viaje ->
                    ViajeItem(viaje, sdfDate, sdfTime, onDelete = { viewModel.removeViaje(viaje) }, onShowMap = {
                        val uri = "http://maps.google.com/maps?saddr=${viaje.latInicio},${viaje.lngInicio}&daddr=${viaje.latFin},${viaje.lngFin}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        context.startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun ViajeItem(viaje: BitacoraEntity, sdfDate: SimpleDateFormat, sdfTime: SimpleDateFormat, onDelete: () -> Unit, onShowMap: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                Text(
                    text = sdfDate.format(Date(viaje.fechaInicio)),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${sdfTime.format(Date(viaje.fechaInicio))} - ${sdfTime.format(Date(viaje.fechaFin ?: 0))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (viaje.duracion != null) {
                    val mins = viaje.duracion / 60000
                    Text("Duración: $mins min", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onShowMap) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ver ruta", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
