package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.UbicacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {
    @Query("SELECT * FROM ubicaciones WHERE vehiculoId = :vehiculoId ORDER BY fechaGuardado DESC")
    fun getUbicacionesByVehiculo(vehiculoId: Long): Flow<List<UbicacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUbicacion(ubicacion: UbicacionEntity)

    @Delete
    suspend fun deleteUbicacion(ubicacion: UbicacionEntity)

    @Query("SELECT * FROM ubicaciones")
    suspend fun getAllUbicaciones(): List<UbicacionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUbicaciones(ubicaciones: List<UbicacionEntity>)

    @Query("DELETE FROM ubicaciones")
    suspend fun clearUbicaciones()
}
