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
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalCoroutinesApi::class)
class SeguroCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var seguro: SeguroEntity? = null

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().flatMapLatest { vehicles ->
                val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                
                vehicle?.let {
                    val colorInt = VehicleBrand.fromString(it.marca).color.toArgb()
                    brandColor = CarColor.createCustom(colorInt, colorInt)
                }

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
        val gridBuilder = ItemList.Builder()

        if (seguro == null) {
            return MessageTemplate.Builder("No hay información de seguro registrada.\n\nPor favor, ingresa los datos de tu póliza desde la aplicación en tu teléfono.")
                .setTitle("Información de Seguro")
                .setHeaderAction(Action.BACK)
                .build()
        }

        val s = seguro!!
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // 1. Aseguradora
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle(s.aseguradora)
                .setText("Compañía")
                .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_insurance))
                    .setTint(brandColor).build())
                .build()
        )

        // 2. Póliza
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle(s.numeroPoliza)
                .setText("No. Póliza")
                .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_info))
                    .setTint(brandColor).build())
                .build()
        )

        // 3. Cobertura
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle(s.tipoCobertura)
                .setText("Cobertura")
                .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_insurance))
                    .setTint(brandColor).build())
                .build()
        )

        // 4. Vencimiento
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle(sdf.format(Date(s.fechaVencimiento)))
                .setText("Vencimiento")
                .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_calendar))
                    .setTint(brandColor).build())
                .build()
        )

        // 5. Botón de Llamada (Acción rápida)
        if (s.telefonoSiniestros.isNotBlank()) {
            gridBuilder.addItem(
                GridItem.Builder()
                    .setTitle("LLAMAR")
                    .setText(s.telefonoSiniestros)
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_call))
                        .setTint(CarColor.RED).build())
                    .setOnClickListener {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${s.telefonoSiniestros}")
                        }
                        carContext.startCarApp(intent)
                    }
                    .build()
            )
        }

        return GridTemplate.Builder()
            .setSingleList(gridBuilder.build())
            .setTitle("Póliza de Seguro")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
