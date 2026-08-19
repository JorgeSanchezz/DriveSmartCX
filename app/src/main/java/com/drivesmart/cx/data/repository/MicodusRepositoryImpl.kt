package com.drivesmart.cx.data.repository

import com.drivesmart.cx.data.remote.micodus.*
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MicodusRepositoryImpl @Inject constructor(
    private val apiService: MicodusApiService,
    private val gson: Gson
) : com.drivesmart.cx.domain.repository.MicodusRepository {

    override suspend fun login(username: String, password: String): Result<Int> {
        return Result.failure(Exception("Usa el login web."))
    }

    override fun getVehicleTracking(userId: Int): Flow<Result<List<MicodusDevice>>> = flow {
        while (true) {
            try {
                android.webkit.CookieManager.getInstance().flush()
                val response = apiService.getDevices(GetDevicesRequest(UserID = userId))
                
                if (response.isSuccessful) {
                    val rawData = response.body()?.d ?: ""
                    
                    if (rawData.length > 5) {
                        // LIMPIADOR UNIVERSAL V4:
                        // Aseguramos que todas las llaves posibles tengan comillas, estén como estén
                        val keys = listOf("devices", "id", "name", "sn", "status", "speed", "lat", "lng", 
                                        "latitude", "longitude", "distance", "acc", "battery", 
                                        "serverUtcDate", "deviceUtcDate", "otherInfo", "stopTimeMinute", 
                                        "dy", "yl", "signal", "satellite", "dw")
                        
                        var cleaned = rawData
                        keys.forEach { key ->
                            cleaned = cleaned.replace(Regex("(?<=[\\{,])\\s*\"?$key\"?\\s*:"), "\"$key\":")
                        }
                        cleaned = cleaned.replace("\\\"", "\"")

                        try {
                            val deviceResponse = gson.fromJson(cleaned, MicodusDeviceResponse::class.java)
                            emit(Result.success(deviceResponse.devices))
                        } catch (e: Exception) {
                            emit(Result.failure(Exception("Error de lectura: flota masiva.")))
                        }
                    } else {
                        emit(Result.success(emptyList()))
                    }
                } else {
                    emit(Result.failure(Exception("MiCODUS error: ${response.code()}")))
                }
            } catch (e: Exception) {
                emit(Result.failure(Exception("Error de conexión con el servidor.")))
            }
            delay(10000)
        }
    }
}
