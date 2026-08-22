package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

class GarageCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository
) : Screen(carContext) {

    private var vehicles: List<VehiculoEntity>? = null

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().collect {
                vehicles = it
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()

        val currentVehicles = vehicles
        if (currentVehicles == null) {
            listBuilder.addItem(Row.Builder().setTitle("Cargando vehículos...").build())
        } else if (currentVehicles.isEmpty()) {
            listBuilder.addItem(Row.Builder().setTitle("No hay vehículos registrados").build())
        } else {
            currentVehicles.forEach { vehicle ->
                val isSelected = vehicle.isSelected
                val brand = VehicleBrand.fromString(vehicle.marca)
                val colorInt = brand.color.toArgb()
                val carBrandColor = CarColor.createCustom(colorInt, colorInt)

                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(vehicle.nombre.ifEmpty { "Vehículo sin nombre" })
                        .addText("${vehicle.marca} ${vehicle.modelo}${if (isSelected) " (Seleccionado)" else ""}")
                        .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_garage))
                            .setTint(if (isSelected) CarColor.GREEN else carBrandColor)
                            .build())
                        .setOnClickListener {
                            selectVehicle(vehicle.id)
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Mi Garage")
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun selectVehicle(selectedId: Long) {
        lifecycleScope.launch {
            val currentList = vehicles ?: return@launch
            
            // 1. Actualizar SharedPreferences para sincronizar con el móvil
            carContext.getSharedPreferences("drivesmart_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putLong("selected_vehicle_id", selectedId)
                .apply()

            // 2. Actualizar la base de datos
            currentList.forEach { v ->
                val shouldBeSelected = v.id == selectedId
                if (v.isSelected != shouldBeSelected) {
                    vehicleRepository.saveVehicle(v.copy(isSelected = shouldBeSelected))
                }
            }
            
            androidx.car.app.CarToast.makeText(carContext, "Vehículo actualizado", androidx.car.app.CarToast.LENGTH_SHORT).show()
            screenManager.pop()
        }
    }
}
