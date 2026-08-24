package com.drivesmart.cx

import android.app.Application
import com.drivesmart.cx.util.AppLogger
import com.drivesmart.cx.util.GlobalExceptionHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DriveSmartApp : Application() {
    @Inject
    lateinit var appLogger: AppLogger

    override fun onCreate() {
        super.onCreate()
        GlobalExceptionHandler.initialize(this, appLogger)
    }
}
