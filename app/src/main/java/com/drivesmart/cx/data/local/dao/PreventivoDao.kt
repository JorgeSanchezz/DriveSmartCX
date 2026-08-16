package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.PreventivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreventivoDao {
    @Query("SELECT * FROM mantenimientos_preventivos WHERE vehiculoId = :vehiculoId")
    fun getPreventivosByVehiculo(vehiculoId: Long): Flow<List<PreventivoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreventivo(preventivo: PreventivoEntity)

    @Update
    suspend fun updatePreventivo(preventivo: PreventivoEntity)

    @Delete
    suspend fun deletePreventivo(preventivo: PreventivoEntity)

    @Query("SELECT * FROM mantenimientos_preventivos")
    suspend fun getAllPreventivos(): List<PreventivoEntity>

    @Query("DELETE FROM mantenimientos_preventivos")
    suspend fun clearPreventivos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreventivos(preventivos: List<PreventivoEntity>)
}
