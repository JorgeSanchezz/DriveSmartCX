package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.GastoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MontoGastoCarScreen(
    carContext: CarContext,
    private val category: String,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val gridBuilder = ItemList.Builder()
        val amounts = listOf(200.0, 500.0, 800.0, 1000.0)

        amounts.forEach { amount ->
            gridBuilder.addItem(
                GridItem.Builder()
                    .setTitle("$${amount.toInt()}")
                    .setImage(CarIcon.Builder(androidx.core.graphics.drawable.IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_add)).build())
                    .setOnClickListener {
                        registrarGasto(amount)
                    }
                    .build()
            )
        }

        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle("Manual")
                .setImage(CarIcon.Builder(androidx.core.graphics.drawable.IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_edit)).build())
                .setOnClickListener {
                    screenManager.push(ManualMontoCarScreen(carContext, category, vehicleRepository, driveSmartRepository))
                }
                .build()
        )

        return GridTemplate.Builder()
            .setSingleList(gridBuilder.build())
            .setTitle("Monto para $category")
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun registrarGasto(monto: Double) {
        lifecycleScope.launch {
            val vehicles = vehicleRepository.getAllVehicles().firstOrNull() ?: emptyList()
            val vehicleId = vehicles.find { it.isSelected }?.id ?: vehicles.firstOrNull()?.id
            if (vehicleId != null) {
                driveSmartRepository.addGasto(
                    GastoEntity(
                        vehiculoId = vehicleId,
                        categoria = category,
                        monto = monto,
                        litros = null,
                        fecha = System.currentTimeMillis(),
                        nota = "Registrado desde Android Auto"
                    )
                )
                CarToast.makeText(carContext, "Gasto de $$monto guardado", CarToast.LENGTH_SHORT).show()
                screenManager.popToRoot()
            }
        }
    }
}

