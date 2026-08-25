package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(
    tableName = "ubicaciones",
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
data class UbicacionEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("vehiculoId") val vehiculoId: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("fechaGuardado") val fechaGuardado: Long
)
