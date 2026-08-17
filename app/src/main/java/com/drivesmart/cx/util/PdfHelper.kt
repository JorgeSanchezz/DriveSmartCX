package com.drivesmart.cx.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.drivesmart.cx.domain.repository.BackupData
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfHelper {

    fun generateMaintenanceReport(context: Context, uri: Uri, data: BackupData) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        var y = 40f

        // Title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        canvas.drawText("Reporte de Mantenimiento - DriveSmartCX", 40f, y, paint)
        y += 30f

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Generado el: ${sdf.format(Date())}", 40f, y, paint)
        y += 40f

        // Vehicles
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Vehículos:", 40f, y, paint)
        y += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        data.vehicles.forEach { v ->
            canvas.drawText("- ${v.nombre}: ${v.marca} ${v.modelo} (${v.placas}) - ${v.kilometrajeActual} KM", 50f, y, paint)
            y += 15f
        }
        y += 20f

        // Services
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Historial de Servicios:", 40f, y, paint)
        y += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        data.servicios.forEach { s ->
            canvas.drawText("[${sdf.format(Date(s.ultimaFecha))}] ${s.tipo}: ${s.nombre} - ${s.ultimoKilometraje} KM - $${s.monto ?: 0.0}", 50f, y, paint)
            y += 15f
            if (y > 750) return@forEach
        }
        y += 20f

        // Insurance
        if (data.seguros.isNotEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Seguros:", 40f, y, paint)
            y += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            data.seguros.forEach { seg ->
                canvas.drawText("- ${seg.aseguradora}: Poliza ${seg.numeroPoliza} - Vence: ${sdf.format(Date(seg.fechaVencimiento))}", 50f, y, paint)
                y += 15f
            }
        }

        document.finishPage(page)

        try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outputStream ->
                    document.writeTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }
}
