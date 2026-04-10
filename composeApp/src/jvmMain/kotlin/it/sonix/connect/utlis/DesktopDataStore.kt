package it.sonix.connect.utlis

import java.io.File
import java.util.Properties

object DesktopDataStore {

    private val file = File(
        System.getProperty("user.home"),
        ".sonix-connect/settings.properties"
    )

    private val props = Properties()

    init {
        file.parentFile.mkdirs()
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }
    }

    fun getSelectedIps(): List<String> {
        return props
            .getProperty(WLED_IPS, "")
            .split(",")
            .filter { it.isNotBlank() }
    }

    fun saveSelectedIpsNow(ips: List<String>) {
        props[WLED_IPS] = ips.joinToString(",")
        save()
    }



    fun isRunOnStartupEnabled(): Boolean =
        props.getProperty(RUN_ON_STARTUP, "false").toBoolean()

    fun setRunOnStartup(enabled: Boolean) {
        props[RUN_ON_STARTUP] = enabled.toString()
        save()
    }

    fun isAutoStartEnabled(): Boolean =
        props.getProperty(AUTO_START_SYNC, "false").toBoolean()

    fun setAutoStart(enabled: Boolean) {
        props[AUTO_START_SYNC] = enabled.toString()
        save()
    }

    fun isLoggingEnabled(): Boolean =
        props.getProperty(LOGGING_ENABLED, "false").toBoolean()

    fun setLoggingEnabled(enabled: Boolean) {
        props[LOGGING_ENABLED] = enabled.toString()
        save()
    }


    fun getThemeMode(): String =
        props.getProperty(THEME_MODE, THEME_SYSTEM)

    fun setThemeMode(mode: String) {
        props[THEME_MODE] = mode
        save()
    }



    private fun save() {
        file.outputStream().use {
            props.store(it, "SonixConnect Desktop Settings")
        }
    }
}
