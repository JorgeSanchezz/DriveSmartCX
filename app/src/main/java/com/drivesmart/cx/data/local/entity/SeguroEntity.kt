package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "seguros")
data class SeguroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val aseguradora: String,
    val numeroPoliza: String,
    val fechaInicio: Long,
    val fechaVencimiento: Long,
    val telefonoSiniestros: String,
    val tipoCobertura: String,
    val notas: String? = null,
    val documentUri: String? = null
)
