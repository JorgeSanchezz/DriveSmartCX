package com.drivesmart.cx.ui.mobile.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.local.entity.UbicacionEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.drivesmart.cx.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstacionamientoScreen(viewModel: DriveSmartViewModel) {
    val ubicaciones by viewModel.currentUbicaciones.collectAsState()
    val context = LocalContext.current
    
    val sdf = remember { 
        SimpleDateFormat("dd/MM hh:mm a", Locale.getDefault())
    }
    val sdfFull = remember { 
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ubicaciones de Estacionamiento") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val loc = LocationHelper.getLastKnownLocation(context)
                if (loc == null) {
                    android.widget.Toast.makeText(context, "No se pudo obtener la ubicación GPS", android.widget.Toast.LENGTH_LONG).show()
                    return@FloatingActionButton
                }
                
                val fecha = sdf.format(Date())
                viewModel.saveUbicacion(
                    nombre = "Estacionamiento $fecha",
                    lat = loc.latitude,
                    lng = loc.longitude
                )
            }) {
                Icon(Icons.Default.Place, contentDescription = "Guardar Ubicación Actual")
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
            if (ubicaciones.isEmpty()) {
                item {
                    Text("No hay ubicaciones guardadas", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(ubicaciones) { ubicacion ->
                    UbicacionItem(
                        ubicacion = ubicacion,
                        sdfFull = sdfFull,
                        onDelete = { viewModel.removeUbicacion(ubicacion) },
                        onShowMap = {
                            val uri = "geo:${ubicacion.latitud},${ubicacion.longitud}?q=${ubicacion.latitud},${ubicacion.longitud}(${ubicacion.nombre})"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UbicacionItem(ubicacion: UbicacionEntity, sdfFull: SimpleDateFormat, onDelete: () -> Unit, onShowMap: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                Text(ubicacion.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = sdfFull.format(Date(ubicacion.fechaGuardado)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onShowMap) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ver en mapa", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
