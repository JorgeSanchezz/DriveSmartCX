package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.car.app.model.CarColor
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalCoroutinesApi::class)
class GastosCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var categories: List<String> = listOf("Gasolina", "Casetas", "Estacionamiento", "Comida", "Refacciones", "Otros")

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles()
                .flatMapLatest { vehicles ->
                    val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                    
                    vehicle?.let {
                        val colorInt = VehicleBrand.fromString(it.marca).color.toArgb()
                        brandColor = CarColor.createCustom(colorInt, colorInt)
                    }

                    val id = vehicle?.id
                    if (id != null) {
                        driveSmartRepository.getGastos(id)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { gastos ->
                    val defaults = listOf("Gasolina", "Casetas", "Estacionamiento", "Comida", "Refacciones", "Otros")
                    val otherCategories = gastos.map { it.categoria }
                        .filter { it !in defaults }
                        .distinct()
                        .sorted()
                    categories = defaults + otherCategories
                    invalidate()
                }
        }
    }

    override fun onGetTemplate(): Template {
        val gridBuilder = ItemList.Builder()
        categories.forEach { cat ->
            val iconRes = when (cat) {
                "Gasolina" -> R.drawable.ic_car_gas
                "Casetas" -> R.drawable.ic_car_toll
                "Estacionamiento" -> R.drawable.ic_car_parking
                "Comida" -> R.drawable.ic_car_food
                "Refacciones" -> R.drawable.ic_car_service
                else -> R.drawable.ic_car_add
            }

            gridBuilder.addItem(
                GridItem.Builder()
                    .setTitle(cat)
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, iconRes))
                        .setTint(brandColor)
                        .build())
                    .setOnClickListener {
                        screenManager.push(MontoGastoCarScreen(carContext, cat, vehicleRepository, driveSmartRepository))
                    }
                    .build()
            )
        }

        return GridTemplate.Builder()
            .setSingleList(gridBuilder.build())
            .setTitle("Seleccionar Categoría")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
