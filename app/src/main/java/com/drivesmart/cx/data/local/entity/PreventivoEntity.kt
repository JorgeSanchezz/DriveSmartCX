package com.drivesmart.cx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mantenimientos_preventivos")
data class PreventivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val nombre: String,
    val ultimaRevision: Long,
    val frecuenciaDias: Int,
    val notas: String? = null
)
