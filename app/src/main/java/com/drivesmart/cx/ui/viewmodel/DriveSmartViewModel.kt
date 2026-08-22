package com.drivesmart.cx.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivesmart.cx.data.local.entity.*
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import com.drivesmart.cx.util.BackupHelper
import com.drivesmart.cx.util.PdfHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DriveSmartViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _isBiometricEnabled = MutableStateFlow(sharedPreferences.getBoolean("biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _appPrimaryColor = MutableStateFlow(sharedPreferences.getString("app_primary_color", null))
    val appPrimaryColor: StateFlow<String?> = _appPrimaryColor.asStateFlow()

    fun setAppPrimaryColor(colorHex: String?) {
        sharedPreferences.edit().putString("app_primary_color", colorHex).apply()
        _appPrimaryColor.value = colorHex
    }

    private val _isAuthenticatedSession = MutableStateFlow(false)
    val isAuthenticatedSession: StateFlow<Boolean> = _isAuthenticatedSession.asStateFlow()

    private val _sosMessage = MutableStateFlow(sharedPreferences.getString("sos_message", "¡Necesito ayuda! Estoy en esta ubicación:") ?: "¡Necesito ayuda! Estoy en esta ubicación:")
    val sosMessage: StateFlow<String> = _sosMessage.asStateFlow()

    private val _tramiteAlertDays = MutableStateFlow(sharedPreferences.getInt("tramite_alert_days", 30))
    val tramiteAlertDays: StateFlow<Int> = _tramiteAlertDays.asStateFlow()

    private val _servicioAlertKm = MutableStateFlow(sharedPreferences.getInt("servicio_alert_km", 1000))
    val servicioAlertKm: StateFlow<Int> = _servicioAlertKm.asStateFlow()

    private val _servicioAlertDays = MutableStateFlow(sharedPreferences.getInt("servicio_alert_days", 15))
    val servicioAlertDays: StateFlow<Int> = _servicioAlertDays.asStateFlow()

    fun setTramiteAlertDays(days: Int) {
        sharedPreferences.edit().putInt("tramite_alert_days", days).apply()
        _tramiteAlertDays.value = days
    }

    fun setServicioAlertKm(km: Int) {
        sharedPreferences.edit().putInt("servicio_alert_km", km).apply()
        _servicioAlertKm.value = km
    }

    fun setServicioAlertDays(days: Int) {
        sharedPreferences.edit().putInt("servicio_alert_days", days).apply()
        _servicioAlertDays.value = days
    }

    fun setSosMessage(message: String) {
        sharedPreferences.edit().putString("sos_message", message).apply()
        _sosMessage.value = message
    }

    fun setAuthenticated(auth: Boolean) {
        _isAuthenticatedSession.value = auth
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    val allVehicles: StateFlow<List<VehiculoEntity>?> = vehicleRepository.getAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedVehicleId: StateFlow<Long?> = allVehicles
        .map { vehicles ->
            // Prioridad: 1. El flag isSelected en DB, 2. El primer vehículo de la lista
            vehicles?.find { it.isSelected }?.id ?: vehicles?.firstOrNull()?.id
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentGastos: StateFlow<List<GastoEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getGastos(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentServicios: StateFlow<List<ServicioEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getServicios(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentTramites: StateFlow<List<TramiteEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getTramites(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentContactos: StateFlow<List<ContactoEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getContactos(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUbicaciones: StateFlow<List<UbicacionEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getUbicaciones(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentBitacora: StateFlow<List<BitacoraEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getBitacora(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSeguro: StateFlow<SeguroEntity?> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getSeguro(id)
        else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentPreventivos: StateFlow<List<PreventivoEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getPreventivos(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeViaje: StateFlow<BitacoraEntity?> = currentBitacora.map { list ->
        list.find { it.fechaFin == null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentSOSContacts: StateFlow<List<ContactoEmergenciaEntity>> = selectedVehicleId.flatMapLatest { id ->
        if (id != null) driveSmartRepository.getSOSContacts(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = currentGastos.map { gastos ->
        val static = listOf("Gasolina", "Mantenimiento", "Seguro", "Trámites", "Lavado", "Estacionamiento", "Otros")
        val otherCategories = gastos.map { it.categoria }
        (static + otherCategories).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            val data = driveSmartRepository.getAllData()
            val json = BackupHelper.exportToJson(data)
            context.contentResolver.openOutputStream(uri)?.use { 
                it.write(json.toByteArray())
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (json != null) {
                    val data = BackupHelper.importFromJson(json)
                    if (data != null && data.vehicles.isNotEmpty()) {
                        driveSmartRepository.restoreAllData(data)
                        
                        // IMPORTANTE: Resetear el ID del vehículo seleccionado tras la importación
                        // para que la UI no se quede buscando un ID viejo.
                        val newSelectedId = data.vehicles.find { it.isSelected }?.id ?: data.vehicles.first().id
                        selectVehicle(newSelectedId)
                        
                        Log.d("DriveSmartVM", "Importación exitosa. Vehículo seleccionado: $newSelectedId")
                    } else if (data != null && data.vehicles.isEmpty()) {
                        Log.w("DriveSmartVM", "El respaldo no contiene vehículos.")
                    } else {
                        Log.e("DriveSmartVM", "Error: El JSON de respaldo no es válido.")
                    }
                }
            } catch (e: Exception) {
                Log.e("DriveSmartVM", "Error al importar respaldo", e)
            }
        }
    }

    fun exportCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val data = driveSmartRepository.getAllData()
            BackupHelper.exportGastosToCsv(context, uri, data)
        }
    }

    fun exportPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            val data = driveSmartRepository.getAllData()
            PdfHelper.generateMaintenanceReport(context, uri, data)
        }
    }

    fun selectVehicle(id: Long) {
        sharedPreferences.edit().putLong("selected_vehicle_id", id).apply()
        
        // Sincronizar con la base de datos para que Android Auto lo vea
        viewModelScope.launch {
            allVehicles.value?.forEach { v ->
                val shouldBeSelected = v.id == id
                if (v.isSelected != shouldBeSelected) {
                    vehicleRepository.saveVehicle(v.copy(isSelected = shouldBeSelected))
                }
            }
        }
    }

    fun addVehicle(
        nombre: String, placas: String, vin: String, modelo: String, 
        anio: Int, kilometraje: Double, marca: String, tipo: String,
        customMarca: String? = null, customColor: String? = null
    ) {
        viewModelScope.launch {
            val v = VehiculoEntity(
                nombre = nombre, placas = placas, vin = vin, modelo = modelo,
                anio = anio, kilometrajeActual = kilometraje, marca = marca, tipo = tipo,
                customMarca = customMarca, customColorHex = customColor
            )
            val id = vehicleRepository.saveVehicle(v)
            selectVehicle(id)
        }
    }

    fun updateVehicle(vehiculo: VehiculoEntity) {
        viewModelScope.launch {
            vehicleRepository.saveVehicle(vehiculo)
        }
    }

    fun deleteVehicle(vehiculo: VehiculoEntity) {
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehiculo)
        }
    }

    fun addGasto(categoria: String, monto: Double, litros: Double?, nota: String?, photoUri: String?, customFecha: Long? = null) {
        val id = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val gasto = GastoEntity(
                vehiculoId = id,
                categoria = categoria,
                monto = monto,
                litros = litros,
                fecha = customFecha ?: System.currentTimeMillis(),
                nota = nota,
                photoUri = photoUri
            )
            driveSmartRepository.addGasto(gasto)
        }
    }

    fun updateGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            driveSmartRepository.addGasto(gasto)
        }
    }

    fun removeGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeGasto(gasto)
        }
    }

    fun saveServicio(
        id: Long = 0,
        nombre: String, tipo: String, ultimoKm: Double, proximoKm: Double?,
        ultimaFecha: Long, proximaFecha: Long?, componentes: String?,
        estatus: String, monto: Double?, photoUri: String?
    ) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val s = ServicioEntity(
                id = id, vehiculoId = vId, nombre = nombre, tipo = tipo,
                ultimoKilometraje = ultimoKm, proximoKilometraje = proximoKm,
                ultimaFecha = ultimaFecha, proximaFecha = proximaFecha,
                componentesIncluidos = componentes, estatus = estatus, monto = monto, photoUri = photoUri
            )
            driveSmartRepository.saveServicio(s)
        }
    }

    fun removeServicio(servicio: ServicioEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeServicio(servicio)
        }
    }

    fun saveTramite(id: Long = 0, nombre: String, fechaVencimiento: Long, estatus: String, descripcion: String?, photoUri: String?) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val t = TramiteEntity(
                id = id, vehiculoId = vId, nombre = nombre,
                fechaVencimiento = fechaVencimiento, estatus = estatus, descripcion = descripcion, photoUri = photoUri
            )
            driveSmartRepository.saveTramite(t)
        }
    }

    fun removeTramite(tramite: TramiteEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeTramite(tramite)
        }
    }

    fun saveContacto(id: Long = 0, nombre: String, telefono: String, tipo: String) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val c = ContactoEntity(id = id, vehiculoId = vId, nombre = nombre, telefono = telefono, tipo = tipo)
            driveSmartRepository.saveContacto(c)
        }
    }

    fun removeContacto(contacto: ContactoEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeContacto(contacto)
        }
    }

    fun saveUbicacion(id: Long = 0, nombre: String, lat: Double, lng: Double) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val u = UbicacionEntity(id = id, vehiculoId = vId, nombre = nombre, latitud = lat, longitud = lng, fechaGuardado = System.currentTimeMillis())
            driveSmartRepository.saveUbicacion(u)
        }
    }

    fun removeUbicacion(ubicacion: UbicacionEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeUbicacion(ubicacion)
        }
    }

    fun startViaje(lat: Double, lng: Double) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val v = BitacoraEntity(
                vehiculoId = vId,
                fechaInicio = System.currentTimeMillis(),
                fechaFin = null,
                latInicio = lat,
                lngInicio = lng,
                latFin = null,
                lngFin = null,
                distancia = null,
                duracion = null
            )
            driveSmartRepository.startViaje(v)
        }
    }

    fun endViaje(lat: Double, lng: Double) {
        val viaje = activeViaje.value ?: return
        viewModelScope.launch {
            val updated = viaje.copy(
                fechaFin = System.currentTimeMillis(),
                latFin = lat,
                lngFin = lng,
                duracion = System.currentTimeMillis() - viaje.fechaInicio
            )
            driveSmartRepository.updateViaje(updated)
        }
    }

    fun removeViaje(viaje: BitacoraEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeViaje(viaje)
        }
    }

    fun saveSeguro(id: Long = 0, aseguradora: String, poliza: String, inicio: Long, vencimiento: Long, tel: String, cobertura: String, notas: String?, documentUri: String?) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val s = SeguroEntity(
                id = id, vehiculoId = vId, aseguradora = aseguradora, numeroPoliza = poliza,
                fechaInicio = inicio, fechaVencimiento = vencimiento, telefonoSiniestros = tel,
                tipoCobertura = cobertura, notas = notas, documentUri = documentUri
            )
            driveSmartRepository.saveSeguro(s)
        }
    }

    fun savePreventivo(id: Long = 0, nombre: String, dias: Int, notas: String?) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val p = PreventivoEntity(
                id = id, vehiculoId = vId, nombre = nombre, 
                ultimaRevision = System.currentTimeMillis(), frecuenciaDias = dias, notas = notas
            )
            driveSmartRepository.savePreventivo(p)
        }
    }

    fun removePreventivo(preventivo: PreventivoEntity) {
        viewModelScope.launch {
            driveSmartRepository.removePreventivo(preventivo)
        }
    }

    fun updatePreventivoRevision(preventivo: PreventivoEntity) {
        viewModelScope.launch {
            driveSmartRepository.savePreventivo(preventivo.copy(ultimaRevision = System.currentTimeMillis()))
        }
    }

    fun saveSOSContact(nombre: String, telefono: String) {
        val vId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val c = ContactoEmergenciaEntity(vehiculoId = vId, nombre = nombre, telefono = telefono)
            driveSmartRepository.saveSOSContact(c)
        }
    }

    fun removeSOSContact(contacto: ContactoEmergenciaEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeSOSContact(contacto)
        }
    }
}
