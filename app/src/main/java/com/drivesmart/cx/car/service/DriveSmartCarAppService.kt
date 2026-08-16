package com.drivesmart.cx.car.service

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.drivesmart.cx.car.screens.MainScreen
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import com.drivesmart.cx.domain.repository.VehicleRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DriveSmartCarAppService : CarAppService() {

    @Inject
    lateinit var vehicleRepository: VehicleRepository
    
    @Inject
    lateinit var driveSmartRepository: DriveSmartRepository

    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return DriveSmartSession(vehicleRepository, driveSmartRepository)
    }
}

class DriveSmartSession(
    private val vehicleRepository: VehicleRepository,
    private val driveSmartRepository: DriveSmartRepository
) : Session() {
    override fun onCreateScreen(intent: Intent): androidx.car.app.Screen {
        return MainScreen(carContext, vehicleRepository, driveSmartRepository)
    }
}

