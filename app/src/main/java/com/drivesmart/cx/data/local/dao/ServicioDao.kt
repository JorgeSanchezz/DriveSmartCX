package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.ServicioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicioDao {
    @Query("SELECT * FROM servicios WHERE vehiculoId = :vehiculoId")
    fun getServiciosByVehiculo(vehiculoId: Long): Flow<List<ServicioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServicio(servicio: ServicioEntity)

    @Update
    suspend fun updateServicio(servicio: ServicioEntity)

    @Delete
    suspend fun deleteServicio(servicio: ServicioEntity)

    @Query("SELECT * FROM servicios")
    suspend fun getAllServicios(): List<ServicioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServicios(servicios: List<ServicioEntity>)

    @Query("DELETE FROM servicios")
    suspend fun clearServicios()
}
