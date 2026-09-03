package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(
    tableName = "bitacora_puntos",
    foreignKeys = [
        ForeignKey(
            entity = BitacoraEntity::class,
            parentColumns = ["id"],
            childColumns = ["viajeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("viajeId")]
)
data class BitacoraPuntoEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("viajeId") val viajeId: Long,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("timestamp") val timestamp: Long
)
