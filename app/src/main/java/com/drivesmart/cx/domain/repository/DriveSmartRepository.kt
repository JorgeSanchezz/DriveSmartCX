package com.drivesmart.cx.domain.repository

import com.drivesmart.cx.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

interface DriveSmartRepository {
    // Error Logs
    fun getAllErrorLogs(): Flow<List<ErrorLogEntity>>
    suspend fun logError(log: ErrorLogEntity)
    suspend fun deleteErrorLog(log: ErrorLogEntity)
    suspend fun clearAllErrorLogs()

    // Gastos
    fun getGastos(vehiculoId: Long): Flow<List<GastoEntity>>
    suspend fun addGasto(gasto: GastoEntity)
    suspend fun removeGasto(gasto: GastoEntity)

    // Servicios
    fun getServicios(vehiculoId: Long): Flow<List<ServicioEntity>>
    suspend fun saveServicio(servicio: ServicioEntity)
    suspend fun removeServicio(servicio: ServicioEntity)

    // Trámites
    fun getTramites(vehiculoId: Long): Flow<List<TramiteEntity>>
    suspend fun saveTramite(tramite: TramiteEntity)
    suspend fun removeTramite(tramite: TramiteEntity)

    // Bitácora
    fun getBitacora(vehiculoId: Long): Flow<List<BitacoraEntity>>
    suspend fun getActiveViaje(vehiculoId: Long): BitacoraEntity?
    suspend fun startViaje(viaje: BitacoraEntity)
    suspend fun updateViaje(viaje: BitacoraEntity)
    suspend fun removeViaje(viaje: BitacoraEntity)

    // Contactos & Ubicaciones
    fun getContactos(vehiculoId: Long): Flow<List<ContactoEntity>>
    suspend fun saveContacto(contacto: ContactoEntity)
    suspend fun removeContacto(contacto: ContactoEntity)
    fun getUbicaciones(vehiculoId: Long): Flow<List<UbicacionEntity>>
    suspend fun saveUbicacion(ubicacion: UbicacionEntity)
    suspend fun removeUbicacion(ubicacion: UbicacionEntity)

    // SOS
    fun getSOSContacts(vehiculoId: Long): Flow<List<ContactoEmergenciaEntity>>
    suspend fun saveSOSContact(contacto: ContactoEmergenciaEntity)
    suspend fun removeSOSContact(contacto: ContactoEmergenciaEntity)

    // Seguro
    fun getSeguro(vehiculoId: Long): Flow<SeguroEntity?>
    suspend fun saveSeguro(seguro: SeguroEntity)

    // Preventivos
    fun getPreventivos(vehiculoId: Long): Flow<List<PreventivoEntity>>
    suspend fun savePreventivo(preventivo: PreventivoEntity)
    suspend fun removePreventivo(preventivo: PreventivoEntity)

    // Backup & Restore
    suspend fun getAllData(): BackupData
    suspend fun restoreAllData(data: BackupData)
}

@Keep
data class BackupData(
    @SerializedName("vehicles") val vehicles: List<VehiculoEntity> = emptyList(),
    @SerializedName("gastos") val gastos: List<GastoEntity> = emptyList(),
    @SerializedName("servicios") val servicios: List<ServicioEntity> = emptyList(),
    @SerializedName("tramites") val tramites: List<TramiteEntity> = emptyList(),
    @SerializedName("bitacora") val bitacora: List<BitacoraEntity> = emptyList(),
    @SerializedName("contactos") val contactos: List<ContactoEntity> = emptyList(),
    @SerializedName("ubicaciones") val ubicaciones: List<UbicacionEntity> = emptyList(),
    @SerializedName("seguros") val seguros: List<SeguroEntity> = emptyList(),
    @SerializedName("preventivos") val preventivos: List<PreventivoEntity> = emptyList(),
    @SerializedName("sosContacts") val sosContacts: List<ContactoEmergenciaEntity> = emptyList()
)
