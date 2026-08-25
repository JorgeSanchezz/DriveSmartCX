package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("vehiculoId") val vehiculoId: Long,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("ultimoKilometraje") val ultimoKilometraje: Double,
    @SerializedName("proximoKilometraje") val proximoKilometraje: Double? = null,
    @SerializedName("ultimaFecha") val ultimaFecha: Long,
    @SerializedName("proximaFecha") val proximaFecha: Long? = null,
    @SerializedName("componentesIncluidos") val componentesIncluidos: String? = null,
    @SerializedName("estatus") val estatus: String,
    @SerializedName("monto") val monto: Double? = null,
    @SerializedName("photoUri") val photoUri: String? = null
)
