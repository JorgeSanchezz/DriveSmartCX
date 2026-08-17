package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.LocationHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConfirmarEstacionamientoCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder("¿Deseas guardar la ubicación actual como tu lugar de estacionamiento?")
            .setTitle("Confirmar Estacionamiento")
            .setIcon(CarIcon.Builder(androidx.core.graphics.drawable.IconCompat.createWithResource(carContext, android.R.drawable.ic_dialog_map)).build())
            .addAction(
                Action.Builder()
                    .setTitle("Guardar")
                    .setOnClickListener {
                        guardarUbicacion()
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Cancelar")
                    .setOnClickListener {
                        screenManager.pop()
                    }
                    .build()
            )
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun guardarUbicacion() {
        lifecycleScope.launch {
            val vehicles = vehicleRepository.getAllVehicles().firstOrNull() ?: emptyList()
            val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
            if (vehicle != null) {
                val loc = LocationHelper.getLastKnownLocation(carContext)
                if (loc == null) {
                    CarToast.makeText(carContext, "Error: No se pudo obtener la ubicación GPS", CarToast.LENGTH_LONG).show()
                    return@launch
                }
                
                val fecha = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())
                driveSmartRepository.saveUbicacion(
                    com.drivesmart.cx.data.local.entity.UbicacionEntity(
                        vehiculoId = vehicle.id,
                        nombre = "Estacionamiento (Auto) $fecha",
                        latitud = loc.latitude,
                        longitud = loc.longitude,
                        fechaGuardado = System.currentTimeMillis()
                    )
                )
                CarToast.makeText(carContext, "Ubicación guardada", CarToast.LENGTH_SHORT).show()
                screenManager.pop()
            }
        }
    }
}

