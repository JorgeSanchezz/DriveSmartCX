package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
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
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("vehiculoId") val vehiculoId: Long,
    @SerializedName("fechaInicio") val fechaInicio: Long,
    @SerializedName("fechaFin") val fechaFin: Long?,
    @SerializedName("latInicio") val latInicio: Double,
    @SerializedName("lngInicio") val lngInicio: Double,
    @SerializedName("latFin") val latFin: Double?,
    @SerializedName("lngFin") val lngFin: Double?,
    @SerializedName("distancia") val distancia: Double?,
    @SerializedName("duracion") val duracion: Long?
)
