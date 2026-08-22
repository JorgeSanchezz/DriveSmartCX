package com.drivesmart.cx.domain.repository

import com.drivesmart.cx.data.local.entity.BitacoraEntity
import com.drivesmart.cx.data.local.entity.ContactoEmergenciaEntity
import com.drivesmart.cx.data.local.entity.ContactoEntity
import com.drivesmart.cx.data.local.entity.GastoEntity
import com.drivesmart.cx.data.local.entity.PreventivoEntity
import com.drivesmart.cx.data.local.entity.SeguroEntity
import com.drivesmart.cx.data.local.entity.ServicioEntity
import com.drivesmart.cx.data.local.entity.TramiteEntity
import com.drivesmart.cx.data.local.entity.UbicacionEntity
import com.drivesmart.cx.data.local.entity.VehiculoEntity
import kotlinx.coroutines.flow.Flow

interface DriveSmartRepository {
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

data class BackupData(
    val vehicles: List<VehiculoEntity> = emptyList(),
    val gastos: List<GastoEntity> = emptyList(),
    val servicios: List<ServicioEntity> = emptyList(),
    val tramites: List<TramiteEntity> = emptyList(),
    val bitacora: List<BitacoraEntity> = emptyList(),
    val contactos: List<ContactoEntity> = emptyList(),
    val ubicaciones: List<UbicacionEntity> = emptyList(),
    val seguros: List<SeguroEntity> = emptyList(),
    val preventivos: List<PreventivoEntity> = emptyList(),
    val sosContacts: List<ContactoEmergenciaEntity> = emptyList()
)
