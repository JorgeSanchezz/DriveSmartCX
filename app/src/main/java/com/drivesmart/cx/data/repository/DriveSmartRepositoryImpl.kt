package com.drivesmart.cx.data.repository

import com.drivesmart.cx.data.local.dao.*
import com.drivesmart.cx.data.local.entity.*
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.BackupData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DriveSmartRepositoryImpl @Inject constructor(
    private val vehiculoDao: VehiculoDao,
    private val gastoDao: GastoDao,
    private val servicioDao: ServicioDao,
    private val tramiteDao: TramiteDao,
    private val bitacoraDao: BitacoraDao,
    private val contactoDao: ContactoDao,
    private val ubicacionDao: UbicacionDao,
    private val seguroDao: SeguroDao,
    private val preventivoDao: PreventivoDao,
    private val sosDao: ContactoEmergenciaDao
) : DriveSmartRepository {
    override fun getGastos(vehiculoId: Long) = gastoDao.getGastosByVehiculo(vehiculoId)
    override suspend fun addGasto(gasto: GastoEntity) = gastoDao.insertGasto(gasto)
    override suspend fun removeGasto(gasto: GastoEntity) = gastoDao.deleteGasto(gasto)

    override fun getServicios(vehiculoId: Long) = servicioDao.getServiciosByVehiculo(vehiculoId)
    override suspend fun saveServicio(servicio: ServicioEntity) = servicioDao.insertServicio(servicio)
    override suspend fun removeServicio(servicio: ServicioEntity) = servicioDao.deleteServicio(servicio)

    override fun getTramites(vehiculoId: Long) = tramiteDao.getTramitesByVehiculo(vehiculoId)
    override suspend fun saveTramite(tramite: TramiteEntity) = tramiteDao.insertTramite(tramite)
    override suspend fun removeTramite(tramite: TramiteEntity) = tramiteDao.deleteTramite(tramite)

    override fun getBitacora(vehiculoId: Long) = bitacoraDao.getBitacoraByVehiculo(vehiculoId)
    override suspend fun getActiveViaje(vehiculoId: Long) = bitacoraDao.getActiveViaje(vehiculoId)
    override suspend fun startViaje(viaje: BitacoraEntity) = bitacoraDao.insertViaje(viaje)
    override suspend fun updateViaje(viaje: BitacoraEntity) = bitacoraDao.updateViaje(viaje)
    override suspend fun removeViaje(viaje: BitacoraEntity) = bitacoraDao.deleteViaje(viaje)

    override fun getContactos(vehiculoId: Long) = contactoDao.getContactosByVehiculo(vehiculoId)
    override suspend fun saveContacto(contacto: ContactoEntity) = contactoDao.insertContacto(contacto)
    override suspend fun removeContacto(contacto: ContactoEntity) = contactoDao.deleteContacto(contacto)

    override fun getUbicaciones(vehiculoId: Long) = ubicacionDao.getUbicacionesByVehiculo(vehiculoId)
    override suspend fun saveUbicacion(ubicacion: UbicacionEntity) = ubicacionDao.insertUbicacion(ubicacion)
    override suspend fun removeUbicacion(ubicacion: UbicacionEntity) = ubicacionDao.deleteUbicacion(ubicacion)

    override fun getSOSContacts(vehiculoId: Long) = sosDao.getSOSContactsByVehiculo(vehiculoId)
    override suspend fun saveSOSContact(contacto: ContactoEmergenciaEntity) = sosDao.insertSOSContact(contacto)
    override suspend fun removeSOSContact(contacto: ContactoEmergenciaEntity) = sosDao.deleteSOSContact(contacto)

    override fun getSeguro(vehiculoId: Long) = seguroDao.getSeguroByVehiculo(vehiculoId)
    override suspend fun saveSeguro(seguro: SeguroEntity) = seguroDao.insertSeguro(seguro)

    override fun getPreventivos(vehiculoId: Long) = preventivoDao.getPreventivosByVehiculo(vehiculoId)
    override suspend fun savePreventivo(preventivo: PreventivoEntity) = preventivoDao.insertPreventivo(preventivo)
    override suspend fun removePreventivo(preventivo: PreventivoEntity) = preventivoDao.deletePreventivo(preventivo)

    override suspend fun getAllData(): BackupData {
        return BackupData(
            vehicles = vehiculoDao.getAllVehiculosSync(),
            gastos = gastoDao.getAllGastos(),
            servicios = servicioDao.getAllServicios(),
            tramites = tramiteDao.getAllTramites(),
            bitacora = bitacoraDao.getAllBitacora(),
            contactos = contactoDao.getAllContactos(),
            ubicaciones = ubicacionDao.getAllUbicaciones(),
            seguros = seguroDao.getAllSeguros(),
            preventivos = preventivoDao.getAllPreventivos(),
            sosContacts = sosDao.getAllSOSContacts()
        )
    }

    override suspend fun restoreAllData(data: BackupData) {
        vehiculoDao.clearVehiculos()
        gastoDao.clearGastos()
        servicioDao.clearServicios()
        tramiteDao.clearTramites()
        bitacoraDao.clearBitacora()
        contactoDao.clearContactos()
        ubicacionDao.clearUbicaciones()
        seguroDao.clearSeguros()
        preventivoDao.clearPreventivos()
        sosDao.clearSOSContacts()

        vehiculoDao.insertVehiculos(data.vehicles)
        gastoDao.insertGastos(data.gastos)
        servicioDao.insertServicios(data.servicios)
        tramiteDao.insertTramites(data.tramites)
        bitacoraDao.insertBitacora(data.bitacora)
        contactoDao.insertContactos(data.contactos)
        ubicacionDao.insertUbicaciones(data.ubicaciones)
        seguroDao.insertSeguros(data.seguros)
        preventivoDao.insertPreventivos(data.preventivos)
        data.sosContacts.forEach { sosDao.insertSOSContact(it) } // Simplificado
    }
}
