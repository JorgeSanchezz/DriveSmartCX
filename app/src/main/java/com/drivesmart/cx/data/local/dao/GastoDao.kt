package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC")
    fun getGastosByVehiculo(vehiculoId: Long): Flow<List<GastoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGasto(gasto: GastoEntity)

    @Delete
    suspend fun deleteGasto(gasto: GastoEntity)

    @Query("SELECT * FROM gastos")
    suspend fun getAllGastos(): List<GastoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGastos(gastos: List<GastoEntity>)

    @Query("DELETE FROM gastos")
    suspend fun clearGastos()
}
