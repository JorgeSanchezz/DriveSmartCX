package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.TramiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TramiteDao {
    @Query("SELECT * FROM tramites WHERE vehiculoId = :vehiculoId")
    fun getTramitesByVehiculo(vehiculoId: Long): Flow<List<TramiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTramite(tramite: TramiteEntity)

    @Update
    suspend fun updateTramite(tramite: TramiteEntity)

    @Delete
    suspend fun deleteTramite(tramite: TramiteEntity)

    @Query("SELECT * FROM tramites")
    suspend fun getAllTramites(): List<TramiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTramites(tramites: List<TramiteEntity>)

    @Query("DELETE FROM tramites")
    suspend fun clearTramites()
}
