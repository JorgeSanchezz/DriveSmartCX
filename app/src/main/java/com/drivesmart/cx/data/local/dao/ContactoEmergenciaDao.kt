package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.ContactoEmergenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoEmergenciaDao {
    @Query("SELECT * FROM contactos_emergencia_sos WHERE vehiculoId = :vehiculoId")
    fun getSOSContactsByVehiculo(vehiculoId: Long): Flow<List<ContactoEmergenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSOSContact(contacto: ContactoEmergenciaEntity)

    @Delete
    suspend fun deleteSOSContact(contacto: ContactoEmergenciaEntity)

    @Query("SELECT * FROM contactos_emergencia_sos")
    suspend fun getAllSOSContacts(): List<ContactoEmergenciaEntity>

    @Query("DELETE FROM contactos_emergencia_sos")
    suspend fun clearSOSContacts()
}
