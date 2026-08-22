package com.drivesmart.cx.car.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.GastoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.R
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ManualMontoCarScreen(
    carContext: CarContext,
    private val category: String,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var currentAmount: String = ""
    private val keyLabels = mapOf(
        "2" to "ABC", "3" to "DEF", "4" to "GHI", "5" to "JKL",
        "6" to "MNO", "7" to "PQRS", "8" to "TUV", "9" to "WXYZ", "0" to "+"
    )

    override fun onGetTemplate(): Template {
        val gridBuilder = ItemList.Builder()

        // Teclado 1-9
        for (i in 1..9) {
            gridBuilder.addItem(createNumberGridItem(i.toString()))
        }

        // Fila final: . , 0
        gridBuilder.addItem(createNumberGridItem("."))
        gridBuilder.addItem(createNumberGridItem("0"))
        
        // Controles en el ActionStrip
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_backspace))
                        .setTint(CarColor.RED)
                        .build())
                    .setOnClickListener {
                        if (currentAmount.isNotEmpty()) {
                            currentAmount = currentAmount.dropLast(1)
                            invalidate()
                        }
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_check))
                        .setTint(CarColor.GREEN)
                        .build())
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
            .setTitle("$category: $${if (currentAmount.isEmpty()) "0" else currentAmount}")
            .setHeaderAction(Action.BACK)
            .setActionStrip(actionStrip)
            .build()
    }

    private fun createNumberGridItem(num: String): GridItem {
        return GridItem.Builder()
            .setTitle(" ") // Espacio mínimo
            .setImage(createDialerBitmapIcon(num, keyLabels[num] ?: ""), GridItem.IMAGE_TYPE_ICON)
            .setOnClickListener {
                if (num == "." && currentAmount.contains(".")) return@setOnClickListener
                if (currentAmount.length < 8) {
                    currentAmount += num
                    invalidate()
                }
            }
            .build()
    }

    private fun createDialerBitmapIcon(number: String, subtext: String): CarIcon {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Nota: Usamos colores que funcionen tanto en fondo blanco como negro
        // La librería de Car App aplicará un tinte automático si usamos CarColor.DEFAULT
        // pero aquí dibujamos directamente. Usaremos Blanco y Gris.
        
        // Número grande
        paint.color = AndroidColor.WHITE
        paint.textSize = 50f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(number, size / 2f, size / 1.8f, paint)

        // Subtexto
        if (subtext.isNotEmpty()) {
            paint.textSize = 18f
            paint.color = AndroidColor.LTGRAY
            canvas.drawText(subtext, size / 2f, size / 1.15f, paint)
        }

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
