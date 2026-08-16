package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.SeguroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeguroDao {
    @Query("SELECT * FROM seguros WHERE vehiculoId = :vehiculoId LIMIT 1")
    fun getSeguroByVehiculo(vehiculoId: Long): Flow<SeguroEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeguro(seguro: SeguroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeguros(seguros: List<SeguroEntity>)

    @Update
    suspend fun updateSeguro(seguro: SeguroEntity)

    @Delete
    suspend fun deleteSeguro(seguro: SeguroEntity)

    @Query("SELECT * FROM seguros")
    suspend fun getAllSeguros(): List<SeguroEntity>

    @Query("DELETE FROM seguros")
    suspend fun clearSeguros()
}
