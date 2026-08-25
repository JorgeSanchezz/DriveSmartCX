package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "servicios",
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
data class ServicioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val tipo: String,
    val nombre: String,
    val ultimoKilometraje: Double,
    val proximoKilometraje: Double? = null,
    val ultimaFecha: Long,
    val proximaFecha: Long? = null,
    val componentesIncluidos: String? = null,
    val estatus: String,
    val monto: Double? = null,
    val photoUri: String? = null
)
