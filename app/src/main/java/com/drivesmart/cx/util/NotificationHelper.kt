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
        val loc = LocationHelper.getLastKnownLocation(context)
        val mapsUrl = if (loc != null) "\nUbicación: https://www.google.com/maps?q=${loc.latitude},${loc.longitude}" else "\n(Sin GPS disponible)"
        val fullMessage = "$message $mapsUrl"

        val hasSmsPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

        if (hasSmsPermission) {
            try {
                val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
                contacts.forEach { contact ->
                    smsManager.sendTextMessage(contact.telefono, null, fullMessage, null, null)
                }
                android.widget.Toast.makeText(context, "SOS Enviado a ${contacts.size} contactos", android.widget.Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Si falla el envío directo por alguna razón técnica (no permisos)
                openSmsApp(context, contacts, fullMessage)
            }
        } else {
            // Si no tiene permisos, simplemente abre la app de SMS sin mostrar error
            openSmsApp(context, contacts, fullMessage)
        }
    }

    private fun openSmsApp(context: Context, contacts: List<ContactoEmergenciaEntity>, fullMessage: String) {
        if (contacts.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${contacts.first().telefono}")
                putExtra("sms_body", fullMessage)
            }
            context.startActivity(intent)
        } else {
            android.widget.Toast.makeText(context, "No hay contactos configurados", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
