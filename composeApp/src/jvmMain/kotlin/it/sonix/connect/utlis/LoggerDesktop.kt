package it.sonix.connect.utils

import java.io.File
import java.time.LocalDateTime

object AppLogger {

    @Volatile
    private var enabled: Boolean = false

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    private val logDir by lazy {
        File(System.getProperty("user.home"), ".sonix-connect")
            .apply { mkdirs() }
    }

    private val logFile by lazy {
        File(logDir, "app.log")
    }

    fun log(message: String) {
        if (!enabled) return

        logFile.appendText(
            "[${LocalDateTime.now()}] $message\n"
        )
    }

    fun error(message: String, t: Throwable? = null) {
        if (!enabled) return

        log("ERROR: $message")
        t?.let { log(it.stackTraceToString()) }
    }
}
