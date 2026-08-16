package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.GastoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ManualMontoCarScreen(
    carContext: CarContext,
    private val category: String,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var currentAmount: String = ""

    override fun onGetTemplate(): Template {
        val gridBuilder = ItemList.Builder()

        // Botones 1-9
        for (i in 1..9) {
            gridBuilder.addItem(createNumberItem(i.toString()))
        }

        // Botón decimal, 0 y Borrar
        gridBuilder.addItem(createNumberItem("."))
        gridBuilder.addItem(createNumberItem("0"))
        
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle("Borrar")
                .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_input_delete)).build(), GridItem.IMAGE_TYPE_ICON)
                .setOnClickListener {
                    if (currentAmount.isNotEmpty()) {
                        currentAmount = currentAmount.dropLast(1)
                        invalidate()
                    }
                }
                .build()
        )

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Confirmar")
                    .setOnClickListener {
                        val monto = currentAmount.toDoubleOrNull()
                        if (monto != null && monto > 0) {
                            registrarGasto(monto)
                        } else {
                            CarToast.makeText(carContext, "Monto inválido", CarToast.LENGTH_SHORT).show()
                        }
                    }
                    .build()
            )
            .build()

        return GridTemplate.Builder()
            .setSingleList(gridBuilder.build())
            .setTitle("Ingresar: $${if (currentAmount.isEmpty()) "0" else currentAmount}")
            .setHeaderAction(Action.BACK)
            .setActionStrip(actionStrip)
            .build()
    }

    private fun createNumberItem(num: String): GridItem {
        return GridItem.Builder()
            .setTitle(num)
            .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_edit)).build(), GridItem.IMAGE_TYPE_ICON)
            .setOnClickListener {
                if (num == "." && currentAmount.contains(".")) return@setOnClickListener
                if (currentAmount.length < 8) {
                    currentAmount += num
                    invalidate()
                }
            }
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

