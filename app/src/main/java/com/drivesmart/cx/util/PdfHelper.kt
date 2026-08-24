package com.drivesmart.cx.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.drivesmart.cx.domain.repository.BackupData
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfHelper {

    fun generateMaintenanceReport(
        context: Context, 
        outputStream: OutputStream, 
        data: BackupData, 
        specificVehicle: VehiculoEntity? = null
    ) {
        val document = PdfDocument()
        val paint = Paint()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        val vehiclesToProcess = if (specificVehicle != null) {
            listOf(specificVehicle)
        } else {
            data.vehicles
        }

        vehiclesToProcess.forEachIndexed { index, vehicle ->
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            var y = 40f

            // Header
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 18f
            canvas.drawText("Reporte de Mantenimiento", 40f, y, paint)
            y += 25f
            
            paint.textSize = 14f
            canvas.drawText("Vehículo: ${vehicle.nombre} (${vehicle.marca} ${vehicle.modelo})", 40f, y, paint)
            y += 20f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            canvas.drawText("Placas: ${vehicle.placas} | KM Actual: ${NumberFormatter.formatKm(vehicle.kilometrajeActual)}", 40f, y, paint)
            y += 15f
            canvas.drawText("Generado el: ${sdf.format(Date())}", 40f, y, paint)
            y += 30f

            // Services Section
            val vehicleServices = data.servicios.filter { it.vehiculoId == vehicle.id }
            if (vehicleServices.isNotEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f
                canvas.drawText("Historial de Servicios:", 40f, y, paint)
                y += 20f
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 10f
                vehicleServices.sortedByDescending { it.ultimaFecha }.forEach { s ->
                    val line = "[${sdf.format(Date(s.ultimaFecha))}] ${s.tipo}: ${s.nombre} - ${NumberFormatter.formatKm(s.ultimoKilometraje)} KM - $${s.monto ?: 0.0}"
                    canvas.drawText(line, 50f, y, paint)
                    y += 15f
                    
                    if (y > 780) {
                        // Aquí idealmente crearíamos otra página, pero para simplificar limitamos por ahora
                        canvas.drawText("... más registros omitidos ...", 50f, y, paint)
                        return@forEach
                    }
                }
                y += 20f
            }

            // Insurance Section
            val vehicleSeguros = data.seguros.filter { it.vehiculoId == vehicle.id }
            if (vehicleSeguros.isNotEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f
                canvas.drawText("Seguros:", 40f, y, paint)
                y += 20f
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 10f
                vehicleSeguros.forEach { seg ->
                    val line = "- ${seg.aseguradora}: Póliza ${seg.numeroPoliza} (Vence: ${sdf.format(Date(seg.fechaVencimiento))})"
                    canvas.drawText(line, 50f, y, paint)
                    y += 15f
                }
                y += 20f
            }
            
            // Preventives Section
            val vehiclePreventivos = data.preventivos.filter { it.vehiculoId == vehicle.id }
            if (vehiclePreventivos.isNotEmpty()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 12f
                canvas.drawText("Revisiones Pendientes/Programadas:", 40f, y, paint)
                y += 20f
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 10f
                vehiclePreventivos.forEach { prev ->
                    val nextDate = prev.ultimaRevision + (prev.frecuenciaDias.toLong() * 24 * 60 * 60 * 1000)
                    val line = "- ${prev.nombre}: Última: ${sdf.format(Date(prev.ultimaRevision))} | Próxima: ${sdf.format(Date(nextDate))}"
                    canvas.drawText(line, 50f, y, paint)
                    y += 15f
                }
            }

            document.finishPage(page)
        }

        try {
            document.writeTo(outputStream)
        } catch (e: Exception) {
            AppLogger.error("PdfHelper", "Error al escribir PDF", e)
            throw e
        } finally {
            document.close()
        }
    }
}
