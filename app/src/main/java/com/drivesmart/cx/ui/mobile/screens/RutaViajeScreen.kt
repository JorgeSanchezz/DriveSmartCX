package com.drivesmart.cx.ui.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drivesmart.cx.data.local.entity.BitacoraPuntoEntity
import com.drivesmart.cx.ui.viewmodel.DriveSmartViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutaViajeScreen(
    viajeId: Long,
    viewModel: DriveSmartViewModel,
    onBack: () -> Unit
) {
    val puntosList by viewModel.getPuntosByViaje(viajeId).collectAsState(initial = emptyList<BitacoraPuntoEntity>())
    val bitacoraList by viewModel.currentBitacora.collectAsState()
    val viaje = remember(bitacoraList, viajeId) { bitacoraList.find { it.id == viajeId } }

    val sdfDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val latLngList = remember(puntosList) {
        puntosList.map { LatLng(it.latitud, it.longitud) }
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(latLngList, viaje) {
        val distinctPoints = latLngList.distinct()
        if (distinctPoints.isNotEmpty()) {
            if (distinctPoints.size == 1) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(distinctPoints.first(), 15f)
            } else {
                try {
                    val builder = LatLngBounds.Builder()
                    distinctPoints.forEach { builder.include(it) }
                    val bounds = builder.build()
                    if (bounds.northeast == bounds.southwest) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(bounds.northeast, 15f)
                    } else {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120))
                    }
                } catch (e: Exception) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(distinctPoints.first(), 15f)
                }
            }
        } else if (viaje != null) {
            val startLatLng = LatLng(viaje.latInicio, viaje.lngInicio)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(startLatLng, 15f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruta Real del Viaje") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val polylinePoints = remember(latLngList, viaje) {
                if (latLngList.isNotEmpty()) {
                    latLngList
                } else if (viaje != null) {
                    listOfNotNull(
                        LatLng(viaje.latInicio, viaje.lngInicio),
                        if (viaje.latFin != null && viaje.lngFin != null) LatLng(viaje.latFin, viaje.lngFin) else null
                    )
                } else emptyList()
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                if (polylinePoints.size >= 2) {
                    Polyline(
                        points = polylinePoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 12f
                    )
                }

                polylinePoints.firstOrNull()?.let { startPos ->
                    Marker(
                        state = MarkerState(position = startPos),
                        title = "Inicio (A)",
                        snippet = if (viaje != null) sdfTime.format(Date(viaje.fechaInicio)) else "",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }

                if (polylinePoints.size > 1) {
                    polylinePoints.lastOrNull()?.let { endPos ->
                        Marker(
                            state = MarkerState(position = endPos),
                            title = "Fin (B)",
                            snippet = if (viaje?.fechaFin != null) sdfTime.format(Date(viaje.fechaFin)) else "",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }
            }

            // Tarjeta flotante inferior con resumen
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen del Recorrido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Distancia Real", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f km", viaje?.distancia ?: 0.0),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text("Duración", style = MaterialTheme.typography.labelSmall)
                            val durationMs = viaje?.duracion ?: 0L
                            val mins = durationMs / 60000
                            val hrs = mins / 60
                            val remMins = mins % 60
                            val durationText = if (hrs > 0) "${hrs}h ${remMins}m" else "${mins} min"
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Fecha", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (viaje != null) sdfDate.format(Date(viaje.fechaInicio)) else "--",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
