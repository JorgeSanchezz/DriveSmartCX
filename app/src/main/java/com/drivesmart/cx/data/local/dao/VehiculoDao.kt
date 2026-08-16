package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoDao {
    @Query("SELECT * FROM vehiculos")
    fun getAllVehiculos(): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE id = :id")
    suspend fun getVehiculoById(id: Long): VehiculoEntity?

    @Insert
    suspend fun insertVehiculo(vehiculo: VehiculoEntity): Long

    @Update
    suspend fun updateVehiculo(vehiculo: VehiculoEntity)

    @Delete
    suspend fun deleteVehiculo(vehiculo: VehiculoEntity)

    @Query("SELECT * FROM vehiculos")
    suspend fun getAllVehiculosSync(): List<VehiculoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehiculos(vehiculos: List<VehiculoEntity>)

    @Query("DELETE FROM vehiculos")
    suspend fun clearVehiculos()
}
