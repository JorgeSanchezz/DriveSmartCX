package com.drivesmart.cx.util

import android.util.Log
import com.drivesmart.cx.data.local.entity.ErrorLogEntity
import com.drivesmart.cx.domain.repository.DriveSmartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val repository: DriveSmartRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        instance = this
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        scope.launch {
            repository.logError(
                ErrorLogEntity(
                    timestamp = System.currentTimeMillis(),
                    tag = tag,
                    message = message,
                    stackTrace = throwable?.stackTraceToString()
                )
            )
        }
    }

    fun logCrash(throwable: Throwable) {
        e("CRASH", "App crashed unexpectedly", throwable)
    }

    companion object {
        private var instance: AppLogger? = null

        fun error(tag: String, message: String, throwable: Throwable? = null) {
            instance?.e(tag, message, throwable) ?: Log.e(tag, "Logger not init: $message", throwable)
        }
    }
}
