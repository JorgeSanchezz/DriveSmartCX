package com.drivesmart.cx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactos_emergencia_sos")
data class ContactoEmergenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val nombre: String,
    val telefono: String
)
