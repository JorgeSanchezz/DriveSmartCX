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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ServicioCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private val brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var servicios: List<ServicioEntity> = emptyList()

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().flatMapLatest { vehicles ->
                val id = vehicles.find { it.isSelected }?.id ?: vehicles.firstOrNull()?.id
                if (id != null) driveSmartRepository.getServicios(id)
                else flowOf(emptyList())
            }.collect {
                servicios = it
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        if (servicios.isEmpty()) {
            paneBuilder.addRow(Row.Builder().setTitle("No hay servicios registrados").build())
        } else {
            servicios.forEach { servicio ->
                val lastInfo = "Último: ${NumberFormatter.formatKm(servicio.ultimoKilometraje)} KM"
                val nextKm = servicio.proximoKilometraje?.let { NumberFormatter.formatKm(it) } ?: "---"
                val nextDate = servicio.proximaFecha?.let { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)) } ?: "---"
                val nextInfo = "Próximo: $nextDate / $nextKm KM"
                
                val statusColor = when (servicio.estatus) {
                    "Crítico" -> CarColor.RED
                    "Pendiente" -> CarColor.YELLOW
                    else -> brandColor
                }

                paneBuilder.addRow(
                    Row.Builder()
                        .setTitle("${servicio.tipo}: ${servicio.nombre}")
                        .addText("Estatus: ${servicio.estatus} - $lastInfo")
                        .addText(nextInfo)
                        .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_manage))
                            .setTint(statusColor)
                            .build())
                        .build()
                )
            }
        }

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Estado del Vehículo")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

