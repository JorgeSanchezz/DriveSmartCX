package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(
    tableName = "gastos",
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
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("vehiculoId") val vehiculoId: Long,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("monto") val monto: Double,
    @SerializedName("litros") val litros: Double?,
    @SerializedName("fecha") val fecha: Long,
    @SerializedName("nota") val nota: String?,
    @SerializedName("photoUri") val photoUri: String? = null
)
