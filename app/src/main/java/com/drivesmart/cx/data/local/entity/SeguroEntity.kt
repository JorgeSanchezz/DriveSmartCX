package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "seguros")
data class SeguroEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("vehiculoId") val vehiculoId: Long,
    @SerializedName("aseguradora") val aseguradora: String,
    @SerializedName("numeroPoliza") val numeroPoliza: String,
    @SerializedName("fechaInicio") val fechaInicio: Long,
    @SerializedName("fechaVencimiento") val fechaVencimiento: Long,
    @SerializedName("telefonoSiniestros") val telefonoSiniestros: String,
    @SerializedName("tipoCobertura") val tipoCobertura: String,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("documentUri") val documentUri: String? = null
)
