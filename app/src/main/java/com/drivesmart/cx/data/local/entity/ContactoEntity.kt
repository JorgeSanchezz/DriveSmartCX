package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "contactos",
    foreignKeys = [
        ForeignKey(
            entity = VehiculoEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehiculoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehiculoId")]
)
data class ContactoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val tipo: String,
    val nombre: String,
    val telefono: String
)
