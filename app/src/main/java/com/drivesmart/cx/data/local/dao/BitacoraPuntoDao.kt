package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.BitacoraPuntoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BitacoraPuntoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPunto(punto: BitacoraPuntoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuntos(puntos: List<BitacoraPuntoEntity>)

    @Query("SELECT * FROM bitacora_puntos WHERE viajeId = :viajeId ORDER BY timestamp ASC")
    fun getPuntosByViaje(viajeId: Long): Flow<List<BitacoraPuntoEntity>>

    @Query("SELECT * FROM bitacora_puntos WHERE viajeId = :viajeId ORDER BY timestamp ASC")
    suspend fun getPuntosByViajeSync(viajeId: Long): List<BitacoraPuntoEntity>

    @Query("DELETE FROM bitacora_puntos WHERE viajeId = :viajeId")
    suspend fun deletePuntosByViaje(viajeId: Long)

    @Query("SELECT * FROM bitacora_puntos")
    suspend fun getAllPuntos(): List<BitacoraPuntoEntity>

    @Query("DELETE FROM bitacora_puntos")
    suspend fun clearBitacoraPuntos()
}
