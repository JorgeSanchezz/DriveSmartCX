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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class EmergenciasCarScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private val brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private var contactos: List<ContactoEntity> = emptyList()
    private var sosContacts: List<com.drivesmart.cx.data.local.entity.ContactoEmergenciaEntity> = emptyList()

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles().flatMapLatest { vehicles ->
                val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                if (vehicle != null) {
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
        val listBuilder = ItemList.Builder()

        if (sosContacts.isNotEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("BOTÓN SOS - ENVIAR UBICACIÓN")
                    .addText("Envía un SMS a tus ${sosContacts.size} contactos de emergencia")
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_dialog_alert))
                        .setTint(CarColor.RED)
                        .build())
                    .setOnClickListener {
                        val message = carContext.getSharedPreferences("drivesmart_prefs", android.content.Context.MODE_PRIVATE)
                            .getString("sos_message", "¡Emergencia! Esta es mi ubicación actual:") ?: "¡Emergencia! Esta es mi ubicación actual:"
                        com.drivesmart.cx.util.NotificationHelper.sendSOS(carContext, sosContacts, message)
                    }
                    .build()
            )
        }

        if (contactos.isEmpty() && sosContacts.isEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Sin contactos de asistencia")
                    .addText("Registra tu seguro en la app móvil")
                    .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_dialog_info))
                        .setTint(brandColor)
                        .build())
                    .build()
            )
        } else {
            contactos.forEach { contacto ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(contacto.nombre)
                        .addText("${contacto.tipo}: ${contacto.telefono}")
                        .setImage(CarIcon.Builder(IconCompat.createWithResource(carContext, android.R.drawable.ic_menu_call))
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

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setTitle("Asistencia")
            .setHeaderAction(Action.BACK)
            .build()
    }
}

