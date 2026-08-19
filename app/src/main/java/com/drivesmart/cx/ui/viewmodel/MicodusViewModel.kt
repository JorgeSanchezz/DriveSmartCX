package com.drivesmart.cx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivesmart.cx.data.remote.micodus.MicodusDevice
import com.drivesmart.cx.domain.repository.MicodusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MicodusViewModel @Inject constructor(
    private val repository: MicodusRepository
) : ViewModel() {

    private val _devicesState = MutableStateFlow<List<MicodusDevice>>(emptyList())
    val devicesState: StateFlow<List<MicodusDevice>> = _devicesState.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _savedImei = MutableStateFlow<String>("")
    val savedImei: StateFlow<String> = _savedImei.asStateFlow()

    private var currentUserId: Int? = null

    fun onWebLoginSuccess(userId: Int, deviceId: Int = 0) {
        currentUserId = userId
        viewModelScope.launch {
            android.webkit.CookieManager.getInstance().flush()
            delay(1000) 
            startTracking(userId)
        }
    }

    fun updateImei(imei: String) {
        _savedImei.value = imei
    }

    private fun startTracking(userId: Int) {
        viewModelScope.launch {
            repository.getVehicleTracking(userId).collectLatest { result ->
                result.onSuccess { allDevices ->
                    val query = _savedImei.value.trim()
                    
                    val filtered = if (query.isEmpty()) {
                        // Si no hay filtro, mostramos los primeros 50 para que el usuario pueda elegir
                        allDevices.take(50)
                    } else {
                        // BÚSQUEDA MULTICAMPO: nombre, imei(sn) o id
                        allDevices.filter { 
                            it.name?.contains(query, ignoreCase = true) == true || 
                            it.sn?.contains(query) == true ||
                            it.id.toString() == query
                        }
                    }
                    
                    _devicesState.value = filtered
                    _errorState.value = if (allDevices.isEmpty()) "Cuenta sin vehículos." 
                                       else if (filtered.isEmpty()) "Carro '$query' no hallado en la lista de ${allDevices.size} vehículos."
                                       else if (query.isEmpty()) "Mostrando los primeros 50 carros. Configura tu IMEI para filtrar."
                                       else null
                }.onFailure { error ->
                    _errorState.value = error.message
                }
            }
        }
    }
}
