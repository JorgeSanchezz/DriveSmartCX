package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.NumberFormatter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class InformacionVehiculoCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository
) : Screen(carContext) {

    private var vehicle: VehiculoEntity? = null

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().collect { vehicles ->
                vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        if (vehicle == null) {
            paneBuilder.addRow(Row.Builder().setTitle("Cargando información...").build())
        } else {
            val v = vehicle!!
            paneBuilder.addRow(Row.Builder().setTitle("Nombre").addText(v.nombre).build())
            paneBuilder.addRow(Row.Builder().setTitle("Modelo / Año").addText("${v.modelo} ${v.anio}").build())
            paneBuilder.addRow(Row.Builder().setTitle("Placas").addText(v.placas).build())
            paneBuilder.addRow(Row.Builder().setTitle("VIN").addText(v.vin).build())
            paneBuilder.addRow(Row.Builder().setTitle("Kilometraje").addText("${NumberFormatter.formatKm(v.kilometrajeActual)} KM").build())
        }

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Información del Vehículo")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

