package com.drivesmart.cx.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.util.NotificationHelper
import com.drivesmart.cx.util.NumberFormatter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class AlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: DriveSmartRepository,
    private val sharedPreferences: SharedPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val allData = repository.getAllData()
            val currentTime = System.currentTimeMillis()
            
            val tramiteAlertDays = sharedPreferences.getInt("tramite_alert_days", 30)
            val servicioAlertKm = sharedPreferences.getInt("servicio_alert_km", 1000)
            val servicioAlertDays = sharedPreferences.getInt("servicio_alert_days", 15)

            val tramiteMs = tramiteAlertDays.toLong() * 24 * 60 * 60 * 1000
            val serviceMs = servicioAlertDays.toLong() * 24 * 60 * 60 * 1000

            allData.vehicles.forEach { vehicle ->
                // 1. Trámites
                allData.tramites.filter { it.vehiculoId == vehicle.id && it.estatus != "Realizado" }.forEach { tramite ->
                    val timeLeft = tramite.fechaVencimiento - currentTime
                    if (tramite.estatus == "Crítico" || (timeLeft > 0 && timeLeft <= tramiteMs)) {
                        NotificationHelper.showNotification(
                            applicationContext,
                            "Trámite Próximo: ${vehicle.nombre}",
                            "${tramite.nombre} vence el ${formatDate(tramite.fechaVencimiento)}"
                        )
                    }
                }

                // 2. Servicios
                allData.servicios.filter { it.vehiculoId == vehicle.id && it.estatus != "Realizado" }.forEach { servicio ->
                    val kmLeft = if (servicio.proximoKilometraje != null) servicio.proximoKilometraje!! - vehicle.kilometrajeActual else Double.MAX_VALUE
                    val timeLeft = if (servicio.proximaFecha != null) servicio.proximaFecha!! - currentTime else Long.MAX_VALUE
                    
                    if (servicio.estatus == "Crítico" || (kmLeft > 0 && kmLeft <= servicioAlertKm) || (timeLeft > 0 && timeLeft <= serviceMs)) {
                        val detail = if (kmLeft <= servicioAlertKm && kmLeft > 0) "en ${NumberFormatter.formatKm(kmLeft)} KM" 
                                     else if (servicio.proximaFecha != null) "el ${formatDate(servicio.proximaFecha!!)}"
                                     else "pronto"
                        
                        NotificationHelper.showNotification(
                            applicationContext,
                            "Servicio Pendiente: ${vehicle.nombre}",
                            "${servicio.nombre} programado $detail"
                        )
                    }
                }
                
                // 3. Preventivos
                allData.preventivos.filter { it.vehiculoId == vehicle.id }.forEach { prev ->
                    val nextDate = prev.ultimaRevision + (prev.frecuenciaDias.toLong() * 24 * 60 * 60 * 1000)
                    if (currentTime > nextDate) {
                        NotificationHelper.showNotification(
                            applicationContext,
                            "Revisión Pendiente: ${vehicle.nombre}",
                            "Es hora de revisar: ${prev.nombre}"
                        )
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            com.drivesmart.cx.util.AppLogger.error("AlertWorker", "Error en tarea de alertas", e)
            return Result.retry()
        }
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    }
}
