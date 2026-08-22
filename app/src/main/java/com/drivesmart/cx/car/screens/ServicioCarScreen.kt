package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.car.app.model.CarColor
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.ServicioEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.NumberFormatter
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalCoroutinesApi::class)
class ServicioCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var servicios: List<ServicioEntity> = emptyList()

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().flatMapLatest { vehicles ->
                val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                val id = vehicle?.id
                
                vehicle?.let {
                    val colorInt = VehicleBrand.fromString(it.marca).color.toArgb()
                    brandColor = CarColor.createCustom(colorInt, colorInt)
                }

                if (id != null) driveSmartRepository.getServicios(id)
                else flowOf(emptyList())
            }.collect {
                servicios = it
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        if (servicios.isEmpty()) {
            listBuilder.addItem(Row.Builder().setTitle("No hay servicios registrados").build())
        } else {
            servicios.forEach { servicio ->
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val lastInfo = "Último: ${NumberFormatter.formatKm(servicio.ultimoKilometraje)} KM"
                val nextKm = servicio.proximoKilometraje?.let { NumberFormatter.formatKm(it) } ?: "---"
                val nextDate = servicio.proximaFecha?.let { sdf.format(java.util.Date(it)) } ?: "---"
                val nextInfo = "Próximo: $nextDate / $nextKm KM"
                
                val statusColor = when (servicio.estatus) {
                    "Crítico" -> CarColor.RED
                    "Pendiente" -> CarColor.YELLOW
                    else -> brandColor
                }

                listBuilder.addItem(
                    Row.Builder()
                        .setTitle("${servicio.tipo}: ${servicio.nombre}")
                        .addText("Estatus: ${servicio.estatus} - $lastInfo")
                        .addText(nextInfo)
                        .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_service))
                            .setTint(statusColor)
                            .build())
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Estado del Vehículo")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
