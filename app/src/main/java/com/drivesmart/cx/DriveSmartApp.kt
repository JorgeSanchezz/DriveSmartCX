package com.drivesmart.cx

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.drivesmart.cx.util.AppLogger
import com.drivesmart.cx.util.GlobalExceptionHandler
import com.drivesmart.cx.worker.AlertWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DriveSmartApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appLogger: AppLogger

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        GlobalExceptionHandler.initialize(this, appLogger)
        scheduleDailyAlerts()
    }

    private fun scheduleDailyAlerts() {
        val workRequest = PeriodicWorkRequestBuilder<AlertWorker>(24, TimeUnit.HOURS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyAlerts",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
