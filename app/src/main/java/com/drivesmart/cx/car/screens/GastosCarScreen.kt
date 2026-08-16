package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.car.app.model.CarColor
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GastosCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private val brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var categories: List<String> = listOf("Gasolina", "Casetas", "Comida", "Refacciones")

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles()
                .flatMapLatest { vehicles ->
                    val id = vehicles.find { it.isSelected }?.id ?: vehicles.firstOrNull()?.id
                    if (id != null) {
                        driveSmartRepository.getGastos(id)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { gastos ->
                    val defaults = listOf("Gasolina", "Casetas", "Comida", "Refacciones")
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
        val listBuilder = ItemList.Builder()
        categories.forEach { cat ->
            val iconRes = when (cat) {
                "Gasolina" -> android.R.drawable.ic_menu_add
                "Casetas" -> android.R.drawable.ic_dialog_map
                "Comida" -> android.R.drawable.ic_menu_view
                "Refacciones" -> android.R.drawable.ic_menu_manage
                else -> android.R.drawable.ic_menu_agenda
            }

            listBuilder.addItem(
                Row.Builder()
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

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Seleccionar Categoría")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

