package com.drivesmart.cx.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.drivesmart.cx.MainActivity
import com.drivesmart.cx.R
import com.drivesmart.cx.data.local.entity.BitacoraPuntoEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.util.AppLogger
import com.drivesmart.cx.util.LocationHelper
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var driveSmartRepository: DriveSmartRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentViajeId: Long = -1L
    private var lastSavedPoint: BitacoraPuntoEntity? = null

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val CHANNEL_NAME = "Rastreo de Viaje GPS"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_VIAJE_ID = "EXTRA_VIAJE_ID"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (currentViajeId == -1L) return

                for (location in locationResult.locations) {
                    val speedKmH = location.speed * 3.6f
                    val last = lastSavedPoint

                    val isFirstPoint = (last == null)
                    val distanceMeters = if (last != null) {
                        LocationHelper.calculateDistance(last.latitud, last.longitud, location.latitude, location.longitude)
                    } else 0f

                    // Filtro de ruido estático:
                    // Ignorar lecturas si la velocidad es <= 3 km/h Y la distancia respecto al último punto guardado es < 10m
                    val shouldRegister = isFirstPoint || (speedKmH > 3.0f || distanceMeters >= 10.0f)

                    if (shouldRegister) {
                        val newPoint = BitacoraPuntoEntity(
                            viajeId = currentViajeId,
                            latitud = location.latitude,
                            longitud = location.longitude,
                            timestamp = if (location.time > 0) location.time else System.currentTimeMillis()
                        )
                        lastSavedPoint = newPoint
                        serviceScope.launch {
                            try {
                                driveSmartRepository.insertPunto(newPoint)
                            } catch (e: Exception) {
                                AppLogger.error("LocationTrackingService", "Error guardando punto de ubicación", e)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopTracking()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val viajeId = intent?.getLongExtra(EXTRA_VIAJE_ID, -1L) ?: -1L
        if (viajeId != -1L) {
            currentViajeId = viajeId
            lastSavedPoint = null
            startForegroundServiceNotification()
            startLocationUpdates()
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            this.action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DriveSmartCX")
            .setContentText("Grabando viaje en curso...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Detener", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val hasFineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            AppLogger.error("LocationTrackingService", "Sin permisos de ubicación para iniciar rastreo")
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .setMinUpdateDistanceMeters(10f)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            AppLogger.error("LocationTrackingService", "Error al remover actualizaciones de ubicación", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación persistente durante la grabación de viajes"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }
}
