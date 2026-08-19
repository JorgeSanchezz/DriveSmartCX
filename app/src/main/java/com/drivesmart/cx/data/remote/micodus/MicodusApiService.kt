package com.drivesmart.cx.data.remote.micodus

import retrofit2.Response
import retrofit2.http.*

interface MicodusApiService {

    @GET("Login2.aspx")
    suspend fun getLoginPage(): Response<okhttp3.ResponseBody>

    @FormUrlEncoded
    @POST("Login2.aspx")
    suspend fun login(
        @FieldMap fields: Map<String, String>
    ): Response<okhttp3.ResponseBody>

    @POST("Ajax/DevicesAjax.asmx/GetDevicesByUserID")
    suspend fun getDevices(
        @Body request: GetDevicesRequest
    ): Response<MicodusAsmxResponse>
}

data class GetDevicesRequest(
    val UserID: Int,
    val isFirst: Boolean = true,
    val TimeZones: String = "8:00",
    val DeviceID: Int = 0
)

data class GetOtherRequest(
    val DeviceID: Int
)
