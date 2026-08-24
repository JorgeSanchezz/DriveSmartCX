package com.drivesmart.cx.util

import android.content.Context
import android.os.Process
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val context: Context,
    private val logger: AppLogger,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Log the error before the app closes
        logger.logCrash(throwable)
        
        // Wait a bit for the log to be saved (coroutines might need a moment)
        Thread.sleep(500)

        // Delegate to the default handler (which usually shows the "App has stopped" dialog)
        // or just kill the process if we want a silent exit
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable)
        } else {
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    companion object {
        fun initialize(context: Context, logger: AppLogger) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(context, logger, defaultHandler))
        }
    }
}
