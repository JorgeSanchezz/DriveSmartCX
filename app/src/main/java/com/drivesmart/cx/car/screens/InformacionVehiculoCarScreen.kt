package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.NumberFormatter
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

class InformacionVehiculoCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository
) : Screen(carContext) {

    private var vehicle: VehiculoEntity? = null
    private var brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().collect { vehicles ->
                val v = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                vehicle = v
                v?.let {
                    val colorInt = VehicleBrand.fromString(it.marca).color.toArgb()
                    brandColor = CarColor.createCustom(colorInt, colorInt)
                }
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        if (vehicle == null) {
            listBuilder.addItem(Row.Builder().setTitle("Cargando información...").build())
        } else {
            val v = vehicle!!
            val brand = VehicleBrand.fromString(v.marca)

            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Vehículo")
                    .addText("${v.nombre} (${v.marca} ${v.modelo})")
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, brand.iconRes))
                        .setTint(brandColor)
                        .build())
                    .build()
            )

            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Año")
                    .addText(v.anio.toString())
                    .build()
            )

            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Placas")
                    .addText(v.placas)
                    .build()
            )

            listBuilder.addItem(
                Row.Builder()
                    .setTitle("VIN (Serie)")
                    .addText(v.vin)
                    .build()
            )

            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Kilometraje Actual")
                    .addText("${NumberFormatter.formatKm(v.kilometrajeActual)} KM")
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_info))
                        .setTint(brandColor)
                        .build())
                    .build()
            )
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Información del Vehículo")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
