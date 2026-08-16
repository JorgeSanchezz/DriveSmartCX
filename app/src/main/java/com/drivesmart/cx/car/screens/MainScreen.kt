package com.drivesmart.cx.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.car.app.model.CarColor
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.BitacoraEntity
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.LocationHelper
import com.drivesmart.cx.util.NumberFormatter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private val brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())

    private var vehicleName: String = "Cargando..."
    private var vehicleKm: String = "---"
    private var activeViaje: BitacoraEntity? = null
    private var currentVehicle: VehiculoEntity? = null

    init {
        lifecycleScope.launch {
            vehicleRepository.getAllVehicles()
                .flatMapLatest { vehicles ->
                    val vehicle = vehicles.find { it.isSelected } ?: vehicles.firstOrNull()
                    currentVehicle = vehicle
                    vehicleName = vehicle?.nombre ?: "Sin Vehículo"
                    vehicleKm = vehicle?.let { "${NumberFormatter.formatKm(it.kilometrajeActual)} KM" } ?: "---"
                    
                    if (vehicle != null) {
                        driveSmartRepository.getBitacora(vehicle.id)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { list ->
                    activeViaje = list.find { it.fechaFin == null }
                    invalidate()
                }
        }
    }

    override fun onGetTemplate(): Template {
        val current = currentVehicle
        
        if (current == null) {
            return MessageTemplate.Builder("¡Bienvenido a DriveSmartCX!\n\nParece que aún no tienes vehículos registrados.\n\nPor favor, abre la aplicación en tu teléfono para configurar tu primer vehículo y comenzar.")
                .setTitle("Bienvenido")
                .setHeaderAction(Action.APP_ICON)
                .build()
        }

        val brand = com.drivesmart.cx.util.VehicleBrand.fromString(current.marca)
        val headerAction = Action.Builder()
            .setIcon(createIcon(brand.iconRes, CarColor.DEFAULT))
            .build()

        val itemListBuilder = ItemList.Builder()

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("Registrar Gasto")
                .addText("Gasolina, Casetas, Comida...")
                .setImage(createIcon(android.R.drawable.ic_menu_add))
                .setOnClickListener { screenManager.push(GastosCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle(if (activeViaje == null) "Iniciar Viaje" else "Terminar Viaje")
                .addText(if (activeViaje == null) "Registrar inicio de recorrido" else "Guardar destino y finalizar")
                .setImage(createIcon(if (activeViaje == null) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause))
                .setOnClickListener {
                    if (activeViaje == null) {
                        startViaje()
                    } else {
                        endViaje()
                    }
                }
                .build()
        )

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("Estado de Servicio")
                .addText("Verificar componentes del vehículo")
                .setImage(createIcon(android.R.drawable.ic_menu_manage))
                .setOnClickListener { screenManager.push(ServicioCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("Información de Seguro")
                .addText("Póliza y contacto de emergencia")
                .setImage(createIcon(android.R.drawable.ic_dialog_info))
                .setOnClickListener { screenManager.push(SeguroCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("Emergencias")
                .addText("Contactos de emergencia y seguro")
                .setImage(createIcon(android.R.drawable.ic_dialog_alert, CarColor.RED))
                .setOnClickListener { screenManager.push(EmergenciasCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("Guardar Parking")
                .addText("Recordar ubicación actual")
                .setImage(createIcon(android.R.drawable.ic_dialog_map))
                .setOnClickListener { screenManager.push(ConfirmarEstacionamientoCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("Info del Vehículo")
                .addText("Resumen técnico de tu Vehículo")
                .setImage(createIcon(android.R.drawable.ic_menu_info_details))
                .setOnClickListener { screenManager.push(InformacionVehiculoCarScreen(carContext, vehicleRepository)) }
                .build()
        )

        itemListBuilder.addItem(
            Row.Builder()
                .setTitle("Mi Garage")
                .addText("Cambiar de vehículo o moto")
                .setImage(createIcon(android.R.drawable.ic_menu_directions))
                .setOnClickListener { screenManager.push(GarageCarScreen(carContext, vehicleRepository)) }
                .build()
        )

        return ListTemplate.Builder()
            .setSingleList(itemListBuilder.build())
            .setTitle("DriveSmartCX - $vehicleName ($vehicleKm)")
            .setHeaderAction(headerAction)
            .build()
    }

    private fun createIcon(resourceId: Int, color: CarColor? = null): CarIcon {
        val builder = CarIcon.Builder(IconCompat.createWithResource(carContext, resourceId))
        color?.let { builder.setTint(it) } ?: builder.setTint(brandColor)
        return builder.build()
    }

    private fun startViaje() {
        lifecycleScope.launch {
            val vehicle = currentVehicle
            if (vehicle != null) {
                val loc = LocationHelper.getLastKnownLocation(carContext)
                if (loc == null) {
                    androidx.car.app.CarToast.makeText(carContext, "Error: Sin señal GPS", androidx.car.app.CarToast.LENGTH_LONG).show()
                }
                
                driveSmartRepository.startViaje(
                    BitacoraEntity(
                        vehiculoId = vehicle.id,
                        fechaInicio = System.currentTimeMillis(),
                        fechaFin = null,
                        latInicio = loc?.latitude ?: 0.0,
                        lngInicio = loc?.longitude ?: 0.0,
                        latFin = null,
                        lngFin = null,
                        distancia = null,
                        duracion = null
                    )
                )
                androidx.car.app.CarToast.makeText(carContext, "Viaje Iniciado", androidx.car.app.CarToast.LENGTH_SHORT).show()
            } else {
                androidx.car.app.CarToast.makeText(carContext, "Selecciona un vehículo primero", androidx.car.app.CarToast.LENGTH_SHORT).show()
            }
        }
    }

    private fun endViaje() {
        val viaje = activeViaje ?: return
        lifecycleScope.launch {
            val loc = LocationHelper.getLastKnownLocation(carContext)
            if (loc == null) {
                androidx.car.app.CarToast.makeText(carContext, "Aviso: Viaje terminado sin GPS final", androidx.car.app.CarToast.LENGTH_LONG).show()
            }
            
            driveSmartRepository.updateViaje(
                viaje.copy(
                    fechaFin = System.currentTimeMillis(),
                    latFin = loc?.latitude ?: 0.0,
                    lngFin = loc?.longitude ?: 0.0,
                    duracion = System.currentTimeMillis() - viaje.fechaInicio
                )
            )
            androidx.car.app.CarToast.makeText(carContext, "Viaje Terminado", androidx.car.app.CarToast.LENGTH_SHORT).show()
        }
    }
}

