package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "vehiculos")
data class VehiculoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val placas: String,
    val vin: String,
    val modelo: String,
    val anio: Int,
    val kilometrajeActual: Double,
    val marca: String = "Otro",
    val tipo: String = "Coche",
    val customMarca: String? = null,
    val customColorHex: String? = null,
    val isSelected: Boolean = false
)
