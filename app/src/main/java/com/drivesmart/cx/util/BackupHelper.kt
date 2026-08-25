package com.drivesmart.cx.util

import android.content.Context
import android.net.Uri
import com.drivesmart.cx.domain.repository.BackupData
import com.google.gson.Gson
import com.opencsv.CSVWriter
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupHelper {
    private val gson = Gson()

    fun exportToJson(data: BackupData): String {
        return gson.toJson(data)
    }

    fun importFromJson(json: String): BackupData? {
        return gson.fromJson(json, BackupData::class.java)
    }

    /**
     * Empaqueta el JSON y los archivos físicos en un solo archivo ZIP.
     */
    fun exportZipBackup(context: Context, uri: Uri, data: BackupData) {
        try {
            val json = exportToJson(data)
            val foldersToBackup = listOf("gastos", "seguros", "tramites", "servicios")
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zos ->
                    // 1. Añadir JSON de datos
                    val jsonEntry = ZipEntry("backup.json")
                    zos.putNextEntry(jsonEntry)
                    zos.write(json.toByteArray())
                    zos.closeEntry()
                    
                    // 2. Añadir archivos de evidencias (imágenes/PDFs)
                    foldersToBackup.forEach { folderName ->
                        val folder = File(context.filesDir, folderName)
                        if (folder.exists() && folder.isDirectory) {
                            folder.listFiles()?.forEach { file ->
                                if (file.isFile) {
                                    val entry = ZipEntry("$folderName/${file.name}")
                                    zos.putNextEntry(entry)
                                    file.inputStream().use { input ->
                                        input.copyTo(zos)
                                    }
                                    zos.closeEntry()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.error("BackupHelper", "Error al exportar respaldo ZIP", e)
            throw e
        }
    }

    /**
     * Desempaqueta el ZIP, restaura los archivos físicos y devuelve el BackupData.
     */
    fun importZipBackup(context: Context, uri: Uri): BackupData? {
        var backupData: BackupData? = null
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        if (entry.name == "backup.json") {
                            val baos = ByteArrayOutputStream()
                            val buffer = ByteArray(1024)
                            var lenSize: Int
                            while (zis.read(buffer).also { lenSize = it } > 0) {
                                baos.write(buffer, 0, lenSize)
                            }
                            backupData = importFromJson(baos.toString("UTF-8"))
                        } else {
                            // Restaurar archivo físico
                            val destFile = File(context.filesDir, entry.name)
                            destFile.parentFile?.mkdirs()
                            FileOutputStream(destFile).use { output ->
                                zis.copyTo(output)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.error("BackupHelper", "Error al importar respaldo ZIP", e)
            throw e
        }
        return backupData
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
