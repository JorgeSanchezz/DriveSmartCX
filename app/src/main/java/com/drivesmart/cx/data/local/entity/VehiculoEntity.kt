package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "vehiculos")
data class VehiculoEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("placas") val placas: String,
    @SerializedName("vin") val vin: String,
    @SerializedName("modelo") val modelo: String,
    @SerializedName("anio") val anio: Int,
    @SerializedName("kilometrajeActual") val kilometrajeActual: Double,
    @SerializedName("marca") val marca: String = "Otro",
    @SerializedName("tipo") val tipo: String = "Coche",
    @SerializedName("customMarca") val customMarca: String? = null,
    @SerializedName("customColorHex") val customColorHex: String? = null,
    @SerializedName("isSelected") val isSelected: Boolean = false
)
