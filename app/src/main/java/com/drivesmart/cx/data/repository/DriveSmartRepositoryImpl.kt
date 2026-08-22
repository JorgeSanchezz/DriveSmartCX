package com.drivesmart.cx.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.drivesmart.cx.data.local.database.AppDatabase
import com.drivesmart.cx.data.local.dao.*
import com.drivesmart.cx.data.local.entity.*
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.BackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DriveSmartRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
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

    override suspend fun getAllData(): BackupData = withContext(Dispatchers.IO) {
        BackupData(
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

    override suspend fun restoreAllData(data: BackupData): Unit {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                Log.d("DriveSmartRepo", "Iniciando restauración de datos...")

                // 1. Limpiar todo
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

                // 2. Restaurar en orden (Vehículos primero por llaves foráneas)
                vehiculoDao.insertVehiculos(data.vehicles)
                Log.d("DriveSmartRepo", "Vehículos restaurados: ${data.vehicles.size}")

                gastoDao.insertGastos(data.gastos)
                Log.d("DriveSmartRepo", "Gastos restaurados: ${data.gastos.size}")

                servicioDao.insertServicios(data.servicios)
                Log.d("DriveSmartRepo", "Servicios restaurados: ${data.servicios.size}")

                tramiteDao.insertTramites(data.tramites)
                Log.d("DriveSmartRepo", "Trámites restaurados: ${data.tramites.size}")

                bitacoraDao.insertBitacora(data.bitacora)
                Log.d("DriveSmartRepo", "Bitácora restaurada: ${data.bitacora.size}")

                contactoDao.insertContactos(data.contactos)
                Log.d("DriveSmartRepo", "Contactos restaurados: ${data.contactos.size}")

                ubicacionDao.insertUbicaciones(data.ubicaciones)
                Log.d("DriveSmartRepo", "Ubicaciones restauradas: ${data.ubicaciones.size}")

                seguroDao.insertSeguros(data.seguros)
                Log.d("DriveSmartRepo", "Seguros restaurados: ${data.seguros.size}")

                preventivoDao.insertPreventivos(data.preventivos)
                Log.d("DriveSmartRepo", "Mantenimientos restaurados: ${data.preventivos.size}")

                data.sosContacts.forEach { sosDao.insertSOSContact(it) }
                Log.d("DriveSmartRepo", "Contactos SOS restaurados: ${data.sosContacts.size}")

                Log.d("DriveSmartRepo", "Restauración completada con éxito.")
            }
        }
    }
}
