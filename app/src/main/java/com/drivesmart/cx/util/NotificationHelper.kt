package com.drivesmart.cx.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.drivesmart.cx.R
import com.drivesmart.cx.data.local.entity.ContactoEmergenciaEntity

object NotificationHelper {
    private const val CHANNEL_ID = "drivesmart_alerts"
    private const val CHANNEL_NAME = "Alertas de Vehículo"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notificaciones para servicios y trámites próximos"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun sendSOS(context: Context, contacts: List<ContactoEmergenciaEntity>, message: String) {
        if (contacts.isEmpty()) {
            android.widget.Toast.makeText(context, "No hay contactos SOS configurados", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val loc = LocationHelper.getLastKnownLocation(context)
        val mapsUrl = if (loc != null) "\nUbicación: https://www.google.com/maps?q=${loc.latitude},${loc.longitude}" else "\n(Sin GPS disponible)"
        val fullMessage = "$message $mapsUrl"

        val hasSmsPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

        if (hasSmsPermission) {
            try {
                val smsManager: SmsManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.applicationContext.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                if (smsManager != null) {
                    contacts.forEach { contact ->
                        val sanitizedPhone = contact.telefono.filter { it.isDigit() || it == '+' }
                        val parts = smsManager.divideMessage(fullMessage)
                        if (parts.size > 1) {
                            smsManager.sendMultipartTextMessage(sanitizedPhone, null, parts, null, null)
                        } else {
                            smsManager.sendTextMessage(sanitizedPhone, null, fullMessage, null, null)
                        }
                    }
                    android.widget.Toast.makeText(context, "SOS Enviado a ${contacts.size} contactos", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    AppLogger.error("NotificationHelper", "SmsManager null, abriendo app de SMS")
                    openSmsApp(context, contacts, fullMessage)
                }
            } catch (e: Exception) {
                AppLogger.error("NotificationHelper", "Error enviando SMS directo", e)
                openSmsApp(context, contacts, fullMessage)
            }
        } else {
            AppLogger.error("NotificationHelper", "Sin permiso SEND_SMS, abriendo app de SMS")
            openSmsApp(context, contacts, fullMessage)
        }
    }

    private fun openSmsApp(context: Context, contacts: List<ContactoEmergenciaEntity>, fullMessage: String) {
        if (contacts.isNotEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${contacts.first().telefono}")
                    putExtra("sms_body", fullMessage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                AppLogger.error("NotificationHelper", "Error al abrir app de mensajes", e)
                android.widget.Toast.makeText(context, "Error al abrir app de mensajes", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "No hay contactos configurados", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
