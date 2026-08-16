package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GarageCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository
) : Screen(carContext) {

    private val brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
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
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(vehicle.nombre.ifEmpty { "Vehículo sin nombre" })
                        .addText("${vehicle.marca} ${vehicle.modelo}${if (isSelected) " (Seleccionado)" else ""}")
                        .setImage(CarIcon.Builder(androidx.core.graphics.drawable.IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_directions))
                            .setTint(if (isSelected) CarColor.GREEN else brandColor)
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

