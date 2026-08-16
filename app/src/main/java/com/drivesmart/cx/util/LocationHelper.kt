package com.drivesmart.cx.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || (l.time > bestLocation.time && l.accuracy <= bestLocation.accuracy)) {
                    bestLocation = l
                }
            }
            
            if (bestLocation == null) {
                // Forzar intento con GPS y Network si falló lo anterior
                val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val passiveLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                
                bestLocation = listOfNotNull(gpsLoc, netLoc, passiveLoc)
                    .maxByOrNull { it.time }
            }
            
            bestLocation
        } catch (e: Exception) {
            null
        }
    }
}
