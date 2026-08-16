package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.BitacoraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BitacoraDao {
    @Query("SELECT * FROM bitacora WHERE vehiculoId = :vehiculoId ORDER BY fechaInicio DESC")
    fun getBitacoraByVehiculo(vehiculoId: Long): Flow<List<BitacoraEntity>>

    @Query("SELECT * FROM bitacora WHERE vehiculoId = :vehiculoId AND fechaFin IS NULL LIMIT 1")
    suspend fun getActiveViaje(vehiculoId: Long): BitacoraEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViaje(viaje: BitacoraEntity)

    @Update
    suspend fun updateViaje(viaje: BitacoraEntity)

    @Delete
    suspend fun deleteViaje(viaje: BitacoraEntity)

    @Query("SELECT * FROM bitacora")
    suspend fun getAllBitacora(): List<BitacoraEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBitacora(bitacora: List<BitacoraEntity>)

    @Query("DELETE FROM bitacora")
    suspend fun clearBitacora()
}
