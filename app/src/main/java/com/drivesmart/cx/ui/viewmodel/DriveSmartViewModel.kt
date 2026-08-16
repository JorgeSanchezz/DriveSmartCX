package com.drivesmart.cx.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
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

    private val _isBiometricEnabled = MutableStateFlow(sharedPreferences.getBoolean("biometric_enabled", true))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _appPrimaryColor = MutableStateFlow(sharedPreferences.getString("app_primary_color", null))
    val appPrimaryColor: StateFlow<String?> = _appPrimaryColor.asStateFlow()

    fun setAppPrimaryColor(colorHex: String?) {
        sharedPreferences.edit().putString("app_primary_color", colorHex).apply()
        _appPrimaryColor.value = colorHex
    }

    private val _isAuthenticatedSession = MutableStateFlow(false)
    val isAuthenticatedSession: StateFlow<Boolean> = _isAuthenticatedSession.asStateFlow()

    private val _sosMessage = MutableStateFlow(sharedPreferences.getString("sos_message", "¡Emergencia! Esta es mi ubicación actual:") ?: "¡Emergencia! Esta es mi ubicación actual:")
    val sosMessage: StateFlow<String> = _sosMessage.asStateFlow()

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

    val selectedVehicleId: StateFlow<Long?> = allVehicles.map { vehicles ->
        vehicles?.find { it.isSelected }?.id ?: vehicles?.firstOrNull()?.id
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
        val defaults = listOf("Gasolina", "Casetas", "Comida", "Refacciones")
        val otherCategories = gastos.map { it.categoria }
            .filter { it !in defaults }
            .distinct()
            .sorted()
        defaults + otherCategories
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Gasolina", "Casetas", "Comida", "Refacciones"))

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
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) {
                val data = BackupHelper.importFromJson(json)
                if (data != null) {
                    driveSmartRepository.restoreAllData(data)
                }
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
        viewModelScope.launch {
            val all = allVehicles.value ?: return@launch
            all.forEach { v ->
                vehicleRepository.saveVehicle(v.copy(isSelected = v.id == id))
            }
        }
    }

    fun addVehicle(nombre: String, placas: String, vin: String, modelo: String, anio: Int, km: Double, marca: String, tipo: String, customMarca: String?, customColor: String?) {
        viewModelScope.launch {
            // Deseleccionar otros
            val all = allVehicles.value ?: emptyList()
            all.forEach { v ->
                if (v.isSelected) vehicleRepository.saveVehicle(v.copy(isSelected = false))
            }
            
            vehicleRepository.saveVehicle(
                VehiculoEntity(
                    nombre = nombre, 
                    placas = placas, 
                    vin = vin, 
                    modelo = modelo, 
                    anio = anio, 
                    kilometrajeActual = km,
                    marca = marca,
                    tipo = tipo,
                    customMarca = customMarca,
                    customColorHex = customColor,
                    isSelected = true
                )
            )
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

    fun addGasto(categoria: String, monto: Double, litros: Double?, nota: String?, photoUri: String? = null) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.addGasto(
                GastoEntity(
                    vehiculoId = vehiculoId,
                    categoria = categoria,
                    monto = monto,
                    litros = litros,
                    fecha = System.currentTimeMillis(),
                    nota = nota,
                    photoUri = photoUri
                )
            )
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
        tipo: String,
        nombre: String,
        ultimoKm: Double,
        proximoKm: Double?,
        ultimaFecha: Long,
        proximaFecha: Long?,
        componentes: String?,
        estatus: String,
        monto: Double?,
        photoUri: String? = null
    ) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.saveServicio(
                ServicioEntity(
                    id = id,
                    vehiculoId = vehiculoId,
                    tipo = tipo,
                    nombre = nombre,
                    ultimoKilometraje = ultimoKm,
                    proximoKilometraje = proximoKm,
                    ultimaFecha = ultimaFecha,
                    proximaFecha = proximaFecha,
                    componentesIncluidos = componentes,
                    estatus = estatus,
                    monto = monto,
                    photoUri = photoUri
                )
            )
        }
    }

    fun removeServicio(servicio: ServicioEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeServicio(servicio)
        }
    }

    fun saveTramite(
        id: Long = 0,
        nombre: String,
        fechaVencimiento: Long,
        estatus: String,
        descripcion: String?
    ) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.saveTramite(
                TramiteEntity(
                    id = id,
                    vehiculoId = vehiculoId,
                    nombre = nombre,
                    fechaVencimiento = fechaVencimiento,
                    estatus = estatus,
                    descripcion = descripcion
                )
            )
        }
    }

    fun removeTramite(tramite: TramiteEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeTramite(tramite)
        }
    }

    fun saveContacto(id: Long = 0, nombre: String, tipo: String, telefono: String) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.saveContacto(
                ContactoEntity(id = id, vehiculoId = vehiculoId, nombre = nombre, tipo = tipo, telefono = telefono)
            )
        }
    }

    fun removeContacto(contacto: ContactoEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeContacto(contacto)
        }
    }

    fun saveUbicacion(id: Long = 0, nombre: String, lat: Double, lng: Double) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.saveUbicacion(
                UbicacionEntity(id = id, vehiculoId = vehiculoId, nombre = nombre, latitud = lat, longitud = lng, fechaGuardado = System.currentTimeMillis())
            )
        }
    }

    fun removeUbicacion(ubicacion: UbicacionEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeUbicacion(ubicacion)
        }
    }

    fun startViaje(lat: Double, lng: Double) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.startViaje(
                BitacoraEntity(
                    vehiculoId = vehiculoId,
                    fechaInicio = System.currentTimeMillis(),
                    fechaFin = null,
                    latInicio = lat,
                    lngInicio = lng,
                    latFin = null,
                    lngFin = null,
                    distancia = null,
                    duracion = null
                )
            )
        }
    }

    fun endViaje(lat: Double, lng: Double) {
        val viaje = activeViaje.value ?: return
        viewModelScope.launch {
            driveSmartRepository.updateViaje(
                viaje.copy(
                    fechaFin = System.currentTimeMillis(),
                    latFin = lat,
                    lngFin = lng,
                    duracion = System.currentTimeMillis() - viaje.fechaInicio
                )
            )
        }
    }

    fun removeViaje(viaje: BitacoraEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeViaje(viaje)
        }
    }

    fun saveSeguro(
        id: Long = 0,
        aseguradora: String,
        poliza: String,
        inicio: Long,
        vencimiento: Long,
        tel: String,
        cobertura: String,
        notas: String?,
        documentUri: String? = null
    ) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.saveSeguro(
                SeguroEntity(
                    id = id,
                    vehiculoId = vehiculoId,
                    aseguradora = aseguradora,
                    numeroPoliza = poliza,
                    fechaInicio = inicio,
                    fechaVencimiento = vencimiento,
                    telefonoSiniestros = tel,
                    tipoCobertura = cobertura,
                    notas = notas,
                    documentUri = documentUri
                )
            )
        }
    }

    fun savePreventivo(
        id: Long = 0,
        nombre: String,
        dias: Int,
        notas: String? = null
    ) {
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            driveSmartRepository.savePreventivo(
                PreventivoEntity(
                    id = id,
                    vehiculoId = vehiculoId,
                    nombre = nombre,
                    ultimaRevision = System.currentTimeMillis(),
                    frecuenciaDias = dias,
                    notas = notas
                )
            )
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
        val vehiculoId = selectedVehicleId.value ?: return
        viewModelScope.launch {
            val contacts = currentSOSContacts.value
            if (contacts.size < 5) {
                driveSmartRepository.saveSOSContact(
                    ContactoEmergenciaEntity(vehiculoId = vehiculoId, nombre = nombre, telefono = telefono)
                )
            }
        }
    }

    fun removeSOSContact(contacto: ContactoEmergenciaEntity) {
        viewModelScope.launch {
            driveSmartRepository.removeSOSContact(contacto)
        }
    }
}
