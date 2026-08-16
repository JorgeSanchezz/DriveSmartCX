package com.drivesmart.cx.data.repository

import com.drivesmart.cx.data.local.dao.VehiculoDao
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import com.drivesmart.cx.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehiculoDao: VehiculoDao
) : VehicleRepository {
    override fun getAllVehicles(): Flow<List<VehiculoEntity>> = vehiculoDao.getAllVehiculos()
    override suspend fun getVehicleById(id: Long): VehiculoEntity? = vehiculoDao.getVehiculoById(id)
    override suspend fun saveVehicle(vehicle: VehiculoEntity): Long {
        return if (vehicle.id == 0L) {
            vehiculoDao.insertVehiculo(vehicle)
        } else {
            vehiculoDao.updateVehiculo(vehicle)
            vehicle.id
        }
    }
    override suspend fun deleteVehicle(vehicle: VehiculoEntity) = vehiculoDao.deleteVehiculo(vehicle)
}
