package com.drivesmart.cx.car.screens

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.car.app.model.CarColor
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.ContactoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

@OptIn(ExperimentalCoroutinesApi::class)
class EmergenciasCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var contactos: List<ContactoEntity> = emptyList()
    private var sosContacts: List<com.drivesmart.cx.data.local.entity.ContactoEmergenciaEntity> = emptyList()

    // Estado para la cuenta regresiva
    private var isCountingDown = false
    private var countdownValue = 5
    private var countdownJob: Job? = null

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().flatMapLatest { vehicles ->
                val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                if (vehicle != null) {
                    val colorInt = VehicleBrand.fromString(vehicle.marca).color.toArgb()
                    brandColor = CarColor.createCustom(colorInt, colorInt)
                    
                    driveSmartRepository.getContactos(vehicle.id).flatMapLatest { c ->
                        driveSmartRepository.getSOSContacts(vehicle.id).map { s ->
                            Pair(c, s)
                        }
                    }
                } else {
                    flowOf(Pair(emptyList<ContactoEntity>(), emptyList<com.drivesmart.cx.data.local.entity.ContactoEmergenciaEntity>()))
                }
            }.collect { result ->
                val (c, s) = result
                contactos = c
                sosContacts = s
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        // Pantalla de cuenta regresiva
        if (isCountingDown) {
            return MessageTemplate.Builder("Se enviará un SMS de emergencia con tu ubicación en $countdownValue segundos a todos tus contactos SOS.")
                .setTitle("¡ALERTA SOS EN CURSO!")
                .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_sos))
                    .setTint(CarColor.RED)
                    .build())
                .addAction(
                    Action.Builder()
                        .setTitle("CANCELAR ENVÍO")
                        .setOnClickListener { cancelSOS() }
                        .build()
                )
                .setHeaderAction(Action.BACK)
                .build()
        }

        val gridBuilder = ItemList.Builder()

        // 1. Botón de Marcación Rápida SOS (Prioritario)
        if (sosContacts.isNotEmpty()) {
            val mainSos = sosContacts.first()
            gridBuilder.addItem(
                GridItem.Builder()
                    .setTitle("LLAMADA SOS: ${mainSos.nombre}")
                    .setText("Marcar número de emergencia inmediatamente")
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_call))
                        .setTint(CarColor.RED)
                        .build())
                    .setOnClickListener {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${mainSos.telefono}")
                        }
                        carContext.startCarApp(intent)
                    }
                    .build()
            )
        }

        val templateBuilder = GridTemplate.Builder()

        // 1. Configurar ActionStrip solo si hay contactos SOS
        if (sosContacts.isNotEmpty()) {
            val actionStrip = ActionStrip.Builder()
                .addAction(
                    Action.Builder()
                        .setTitle("SOS SMS")
                        .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_sos))
                            .setTint(CarColor.RED)
                            .build())
                        .setOnClickListener { startSOSCountdown() }
                        .build()
                )
                .build()
            templateBuilder.setActionStrip(actionStrip)
        }

        if (contactos.isEmpty()) {
            gridBuilder.addItem(
                GridItem.Builder()
                    .setTitle("Sin contactos")
                    .setText("Registra en móvil")
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_info))
                        .setTint(brandColor)
                        .build())
                    .build()
            )
        } else {
            contactos.forEach { contacto ->
                gridBuilder.addItem(
                    GridItem.Builder()
                        .setTitle(contacto.nombre)
                        .setText(contacto.tipo)
                        .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_car_call))
                            .setTint(CarColor.RED)
                            .build())
                        .setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contacto.telefono}")
                            }
                            carContext.startCarApp(intent)
                        }
                        .build()
                )
            }
        }

        return templateBuilder
            .setSingleList(gridBuilder.build())
            .setTitle("Asistencia y Emergencias")
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun startSOSCountdown() {
        isCountingDown = true
        countdownValue = 5
        invalidate()

        countdownJob = lifecycleScope.launch {
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
                invalidate()
            }
            // Cuando llega a 0, enviar
            performSendSOS()
        }
    }

    private fun cancelSOS() {
        countdownJob?.cancel()
        isCountingDown = false
        countdownValue = 5
        androidx.car.app.CarToast.makeText(carContext, "Envío cancelado", androidx.car.app.CarToast.LENGTH_SHORT).show()
        invalidate()
    }

    private fun performSendSOS() {
        val message = carContext.getSharedPreferences("drivesmart_prefs", android.content.Context.MODE_PRIVATE)
            .getString("sos_message", "¡Emergencia! Esta es mi ubicación actual:") ?: "¡Emergencia! Esta es mi ubicación actual:"
        
        com.drivesmart.cx.util.NotificationHelper.sendSOS(carContext, sosContacts, message)
        
        isCountingDown = false
        invalidate()
        androidx.car.app.CarToast.makeText(carContext, "¡Mensajes SOS Enviados!", androidx.car.app.CarToast.LENGTH_LONG).show()
    }
}
