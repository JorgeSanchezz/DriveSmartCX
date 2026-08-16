package com.drivesmart.cx.car.screens

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.car.app.model.CarColor
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.SeguroEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class SeguroCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private val brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var seguro: SeguroEntity? = null

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().flatMapLatest { vehicles ->
                val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                if (vehicle != null) {
                    driveSmartRepository.getSeguro(vehicle.id)
                } else {
                    flowOf(null)
                }
            }.collect {
                seguro = it
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        if (seguro == null) {
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Sin información de seguro")
                    .addText("Registra tu póliza en la aplicación móvil")
                    .build()
            )
        } else {
            val s = seguro!!
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle(s.aseguradora)
                    .addText("Póliza: ${s.numeroPoliza}")
                    .build()
            )
            
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Vigencia")
                    .addText("Vence el: ${sdf.format(Date(s.fechaVencimiento))}")
                    .build()
            )

            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Tipo de Cobertura")
                    .addText(s.tipoCobertura)
                    .build()
            )

            if (s.telefonoSiniestros.isNotBlank()) {
                paneBuilder.addAction(
                    Action.Builder()
                        .setTitle("Llamar Seguro")
                        .setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${s.telefonoSiniestros}")
                            }
                            carContext.startCarApp(intent)
                        }
                        .build()
                )

                paneBuilder.addRow(
                    Row.Builder()
                        .setTitle("Teléfono de Emergencia")
                        .addText(s.telefonoSiniestros)
                        .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_call))
                            .setTint(CarColor.RED)
                            .build())
                        .build()
                )
            }
        }

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("Información de Seguro")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
