package com.drivesmart.cx.domain.repository

import com.drivesmart.cx.data.local.entity.VehiculoEntity
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun getAllVehicles(): Flow<List<VehiculoEntity>>
    suspend fun getVehicleById(id: Long): VehiculoEntity?
    suspend fun saveVehicle(vehicle: VehiculoEntity): Long
    suspend fun deleteVehicle(vehicle: VehiculoEntity)
}
