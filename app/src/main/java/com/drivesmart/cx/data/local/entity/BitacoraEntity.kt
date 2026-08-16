package com.drivesmart.cx.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bitacora",
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
data class BitacoraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val fechaInicio: Long,
    val fechaFin: Long?,
    val latInicio: Double,
    val lngInicio: Double,
    val latFin: Double?,
    val lngFin: Double?,
    val distancia: Double?,
    val duracion: Long?
)
