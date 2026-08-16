package com.drivesmart.cx.data.local.dao

import androidx.room.*
import com.drivesmart.cx.data.local.entity.ContactoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {
    @Query("SELECT * FROM contactos WHERE vehiculoId = :vehiculoId")
    fun getContactosByVehiculo(vehiculoId: Long): Flow<List<ContactoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacto(contacto: ContactoEntity)

    @Delete
    suspend fun deleteContacto(contacto: ContactoEntity)

    @Query("SELECT * FROM contactos")
    suspend fun getAllContactos(): List<ContactoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactos(contactos: List<ContactoEntity>)

    @Query("DELETE FROM contactos")
    suspend fun clearContactos()
}
