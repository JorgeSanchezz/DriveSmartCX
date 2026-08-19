package com.drivesmart.cx.domain.repository

import com.drivesmart.cx.data.remote.micodus.MicodusDevice
import kotlinx.coroutines.flow.Flow

interface MicodusRepository {
    suspend fun login(username: String, password: String): Result<Int> // Retorna el UserId
    fun getVehicleTracking(userId: Int): Flow<Result<List<MicodusDevice>>>
}
