package com.drivesmart.cx.car.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.GastoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

class MontoGastoCarScreen(
    carContext: CarContext,
    private val category: String,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var currentAmount: Double = 0.0
    private var brandColorInt: Int = 0xFF4CAF50.toInt()

    init {
        lifecycleScope.launch {
            val vehicles = vehicleRepository.getAllVehicles().firstOrNull() ?: emptyList()
            val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
            vehicle?.let {
                brandColorInt = VehicleBrand.fromString(it.marca).color.toArgb()
                
                val history = driveSmartRepository.getGastos(it.id).firstOrNull() ?: emptyList()
                currentAmount = history.filter { g -> g.categoria == category }.lastOrNull()?.monto ?: 0.0
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val gridBuilder = ItemList.Builder()

        // Fila 1: Aumentar
        gridBuilder.addItem(createAdjustmentItem(100))
        gridBuilder.addItem(createAdjustmentItem(10))
        gridBuilder.addItem(createAdjustmentItem(1))

        // Fila 2: Disminuir
        gridBuilder.addItem(createAdjustmentItem(-100))
        gridBuilder.addItem(createAdjustmentItem(-10))
        gridBuilder.addItem(createAdjustmentItem(-1))

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Manual")
                    .setOnClickListener {
                        screenManager.push(ManualMontoCarScreen(carContext, category, vehicleRepository, driveSmartRepository))
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_check))
                        .setTint(CarColor.GREEN)
                        .build())
                    .setOnClickListener {
                        if (currentAmount > 0) {
                            registrarGasto(currentAmount)
                        } else {
                            CarToast.makeText(carContext, "Ingrese un monto", CarToast.LENGTH_SHORT).show()
                        }
                    }
                    .build()
            )
            .build()

        return GridTemplate.Builder()
            .setSingleList(gridBuilder.build())
            .setTitle("$category: $${currentAmount.toInt()}")
            .setHeaderAction(Action.BACK)
            .setActionStrip(actionStrip)
            .build()
    }

    private fun createAdjustmentItem(value: Int): GridItem {
        val label = if (value > 0) "+$value" else "$value"
        return GridItem.Builder()
            .setTitle(" ") 
            // Usamos IMAGE_TYPE_LARGE para que el sistema NO aplique el tinte blanco y se vean los colores reales
            .setImage(createAdjustmentIcon(label, value > 0), GridItem.IMAGE_TYPE_LARGE)
            .setOnClickListener {
                currentAmount += value
                if (currentAmount < 0) currentAmount = 0.0
                invalidate()
            }
            .build()
    }

    private fun createAdjustmentIcon(text: String, isPositive: Boolean): CarIcon {
        val size = 128 // Un poco más grande para mejor resolución
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fondo circular
        paint.color = if (isPositive) brandColorInt else android.graphics.Color.GRAY
        canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)

        // Texto
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 36f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val y = (size / 2f) - bounds.centerY()
        canvas.drawText(text, size / 2f, y, paint)

        return CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build()
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
