package com.drivesmart.cx.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileHelper {

    /**
     * Copia un archivo desde una URI externa a la carpeta interna de la app.
     * Esto asegura que el archivo sea accesible incluso tras reinstalar la app o restaurar un backup.
     */
    fun copyFileToInternalStorage(context: Context, uri: Uri, folderName: String): String? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "DS_${System.currentTimeMillis()}_" + (getFileName(context, uri) ?: "file")
            
            val folder = File(context.filesDir, folderName)
            if (!folder.exists()) folder.mkdirs()
            
            val destFile = File(folder, fileName)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(destFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            destFile.absolutePath
        } catch (e: Exception) {
            AppLogger.error("FileHelper", "Error al copiar archivo a almacenamiento interno", e)
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name ?: uri.lastPathSegment
    }

    /**
     * Obtiene una URI segura para compartir un archivo interno (usando FileProvider).
     */
    fun getShareableUri(context: Context, filePath: String): Uri? {
        return try {
            val file = File(filePath)
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            AppLogger.error("FileHelper", "Error al obtener URI compartible", e)
            null
        }
    }
}
