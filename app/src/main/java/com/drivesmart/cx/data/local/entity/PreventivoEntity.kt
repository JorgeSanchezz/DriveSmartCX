package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "mantenimientos_preventivos")
data class PreventivoEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("vehiculoId") val vehiculoId: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("ultimaRevision") val ultimaRevision: Long,
    @SerializedName("frecuenciaDias") val frecuenciaDias: Int,
    @SerializedName("notas") val notas: String? = null
)
