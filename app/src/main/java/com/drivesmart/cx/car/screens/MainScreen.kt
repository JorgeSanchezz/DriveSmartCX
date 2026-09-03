package com.drivesmart.cx.car.screens

import android.content.Intent
import android.os.Build
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import com.drivesmart.cx.data.local.entity.BitacoraEntity
import com.drivesmart.cx.data.local.entity.BitacoraPuntoEntity
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.LocationHelper
import com.drivesmart.cx.util.NumberFormatter
import com.drivesmart.cx.util.VehicleBrand
import com.drivesmart.cx.R
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreen(
    carContext: CarContext,
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Screen(carContext) {

    private var brandColor = CarColor.createCustom(0xFF607D8B.toInt(), 0xFF607D8B.toInt())
    private val alertColor = CarColor.RED

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
                    
                    vehicle?.let {
                        val colorInt = VehicleBrand.fromString(it.marca).color.toArgb()
                        brandColor = CarColor.createCustom(colorInt, colorInt)
                    }

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

        val brand = VehicleBrand.fromString(current.marca)
        val headerAction = Action.Builder()
            .setIcon(createIcon(brand.iconRes, CarColor.DEFAULT))
            .build()

        // ActionStrip (Iconos pequeños arriba a la derecha, como en el móvil)
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setIcon(createIcon(R.drawable.ic_car_garage))
                    .setOnClickListener { screenManager.push(GarageCarScreen(carContext, vehicleRepository)) }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setIcon(createIcon(R.drawable.ic_car_info))
                    .setOnClickListener { screenManager.push(InformacionVehiculoCarScreen(carContext, vehicleRepository)) }
                    .build()
            )
            .build()

        val gridBuilder = ItemList.Builder()

        // FILA SUPERIOR
        // 1. Registrar Gasto
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle("Gastos")
                .setText("Registrar")
                .setImage(createIcon(R.drawable.ic_car_add))
                .setOnClickListener { screenManager.push(GastosCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        // 2. Viaje (Dinámico)
        val viajeTitle = if (activeViaje == null) "Iniciar" else "Terminar"
        val viajeIcon = if (activeViaje == null) R.drawable.ic_car_play else R.drawable.ic_car_pause
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle(viajeTitle)
                .setText("Viaje")
                .setImage(createIcon(viajeIcon))
                .setOnClickListener {
                    if (activeViaje == null) startViaje() else endViaje()
                }
                .build()
        )

        // 3. Parking
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle("Parking")
                .setText("Guardar")
                .setImage(createIcon(R.drawable.ic_car_parking))
                .setOnClickListener { screenManager.push(ConfirmarEstacionamientoCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        // FILA INFERIOR
        // 4. Servicio
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle("Servicio")
                .setText("Estado")
                .setImage(createIcon(R.drawable.ic_car_service))
                .setOnClickListener { screenManager.push(ServicioCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        // 5. Seguro
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle("Seguro")
                .setText("Póliza")
                .setImage(createIcon(R.drawable.ic_car_insurance))
                .setOnClickListener { screenManager.push(SeguroCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        // 6. Emergencias (Rojo)
        gridBuilder.addItem(
            GridItem.Builder()
                .setTitle("SOS")
                .setText("Emergencia")
                .setImage(createIcon(R.drawable.ic_car_sos, alertColor))
                .setOnClickListener { screenManager.push(EmergenciasCarScreen(carContext, vehicleRepository, driveSmartRepository)) }
                .build()
        )

        return GridTemplate.Builder()
            .setSingleList(gridBuilder.build())
            .setTitle("DriveSmartCX - $vehicleName ($vehicleKm)")
            .setHeaderAction(headerAction)
            .setActionStrip(actionStrip)
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
                val lat = loc?.latitude ?: 0.0
                val lng = loc?.longitude ?: 0.0

                val viajeId = driveSmartRepository.startViaje(
                    BitacoraEntity(
                        vehiculoId = vehicle.id,
                        fechaInicio = System.currentTimeMillis(),
                        fechaFin = null,
                        latInicio = lat,
                        lngInicio = lng,
                        latFin = null,
                        lngFin = null,
                        distancia = null,
                        duracion = null
                    )
                )

                if (lat != 0.0 || lng != 0.0) {
                    driveSmartRepository.insertPunto(
                        BitacoraPuntoEntity(
                            viajeId = viajeId,
                            latitud = lat,
                            longitud = lng,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }

                try {
                    val intent = Intent(carContext, com.drivesmart.cx.service.LocationTrackingService::class.java).apply {
                        action = com.drivesmart.cx.service.LocationTrackingService.ACTION_START
                        putExtra(com.drivesmart.cx.service.LocationTrackingService.EXTRA_VIAJE_ID, viajeId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        carContext.startForegroundService(intent)
                    } else {
                        carContext.startService(intent)
                    }
                } catch (e: Exception) {
                    com.drivesmart.cx.util.AppLogger.error("MainScreen", "Error al iniciar servicio de rastreo", e)
                }

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
            val lat = loc?.latitude ?: 0.0
            val lng = loc?.longitude ?: 0.0

            try {
                val intent = Intent(carContext, com.drivesmart.cx.service.LocationTrackingService::class.java).apply {
                    action = com.drivesmart.cx.service.LocationTrackingService.ACTION_STOP
                }
                carContext.startService(intent)
            } catch (e: Exception) {
                com.drivesmart.cx.util.AppLogger.error("MainScreen", "Error al detener servicio de rastreo", e)
            }

            val puntos = driveSmartRepository.getPuntosByViajeSync(viaje.id)
            var lastLat = if (puntos.isNotEmpty()) puntos.last().latitud else viaje.latInicio
            var lastLng = if (puntos.isNotEmpty()) puntos.last().longitud else viaje.lngInicio

            if (lat != 0.0 && lng != 0.0) {
                lastLat = lat
                lastLng = lng
            }

            var totalMeters = 0.0
            if (puntos.size >= 2) {
                for (i in 0 until puntos.size - 1) {
                    val p1 = puntos[i]
                    val p2 = puntos[i + 1]
                    totalMeters += LocationHelper.calculateDistance(p1.latitud, p1.longitud, p2.latitud, p2.longitud)
                }
            } else if (puntos.size == 1) {
                val p = puntos[0]
                totalMeters += LocationHelper.calculateDistance(viaje.latInicio, viaje.lngInicio, p.latitud, p.longitud)
                if (lat != 0.0 && lng != 0.0) {
                    totalMeters += LocationHelper.calculateDistance(p.latitud, p.longitud, lat, lng)
                }
            } else {
                if (lat != 0.0 && lng != 0.0) {
                    totalMeters += LocationHelper.calculateDistance(viaje.latInicio, viaje.lngInicio, lat, lng)
                }
            }

            val distanceKm = totalMeters / 1000.0

            driveSmartRepository.updateViaje(
                viaje.copy(
                    fechaFin = System.currentTimeMillis(),
                    latFin = lastLat,
                    lngFin = lastLng,
                    distancia = distanceKm,
                    duracion = System.currentTimeMillis() - viaje.fechaInicio
                )
            )

            currentVehicle?.let { vehicle ->
                vehicleRepository.saveVehicle(
                    vehicle.copy(kilometrajeActual = vehicle.kilometrajeActual + distanceKm)
                )
            }

            androidx.car.app.CarToast.makeText(carContext, "Viaje Terminado", androidx.car.app.CarToast.LENGTH_SHORT).show()
        }
    }
}
