package com.drivesmart.cx.util

import android.content.Context
import android.net.Uri
import com.drivesmart.cx.domain.repository.BackupData
import com.google.gson.Gson
import com.opencsv.CSVWriter
import java.io.OutputStreamWriter

object BackupHelper {
    private val gson = Gson()

    fun exportToJson(data: BackupData): String {
        return gson.toJson(data)
    }

    fun importFromJson(json: String): BackupData? {
        return gson.fromJson(json, BackupData::class.java)
    }

    fun exportGastosToCsv(context: Context, uri: Uri, data: BackupData) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = CSVWriter(OutputStreamWriter(outputStream))
                
                // Header for Gastos
                writer.writeNext(arrayOf("CATEGORIA", "MONTO", "LITROS", "FECHA", "NOTA"))
                data.gastos.forEach { gasto ->
                    writer.writeNext(arrayOf(
                        gasto.categoria,
                        gasto.monto.toString(),
                        gasto.litros?.toString() ?: "",
                        gasto.fecha.toString(),
                        gasto.nota ?: ""
                    ))
                }

                // Separator
                writer.writeNext(arrayOf(""))
                
                // Header for Servicios
                writer.writeNext(arrayOf("TIPO", "NOMBRE", "ULTIMO_KM", "PROXIMO_KM", "ESTATUS"))
                data.servicios.forEach { servicio ->
                    writer.writeNext(arrayOf(
                        servicio.tipo,
                        servicio.nombre,
                        servicio.ultimoKilometraje.toString(),
                        servicio.proximoKilometraje?.toString() ?: "",
                        servicio.estatus
                    ))
                }
                
                writer.close()
            }
        } catch (e: Exception) {
            AppLogger.error("BackupHelper", "Error al exportar CSV", e)
            throw e
        }
    }
}
