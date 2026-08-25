package com.drivesmart.cx.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("tag") val tag: String,
    @SerializedName("message") val message: String,
    @SerializedName("stackTrace") val stackTrace: String? = null
)
