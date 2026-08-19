package com.drivesmart.cx.data.remote.micodus

import com.google.gson.annotations.SerializedName

data class MicodusAsmxResponse(
    @SerializedName("d")
    val d: String
)

data class MicodusDeviceResponse(
    @SerializedName("devices")
    val devices: List<MicodusDevice>,
    @SerializedName("dw")
    val unitType: Int? = null
)

data class MicodusDevice(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sn") val sn: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("speed") val speed: String? = null,
    @SerializedName("lat") val lat: String? = null,
    @SerializedName("lng") val lng: String? = null,
    @SerializedName("latitude") val latitude: String? = null,
    @SerializedName("longitude") val longitude: String? = null,
    @SerializedName("serverUtcDate") val serverUtcDate: String? = null,
    @SerializedName("deviceUtcDate") val deviceUtcDate: String? = null,
    @SerializedName("battery") val battery: String? = null,
    @SerializedName("acc") val acc: String? = null,
    @SerializedName("ofl") val ofl: String? = null,
    @SerializedName("signal") val signal: String? = null,
    @SerializedName("satellite") val satellite: String? = null,
    @SerializedName("distance") val distance: String? = null,
    @SerializedName("stopTimeMinute") val stopTimeMinute: Int? = null,
    @SerializedName("dy") val voltage: String? = null,
    @SerializedName("yl") val fuel: String? = null,
    @SerializedName("otherInfo") val otherInfo: String? = null
)
