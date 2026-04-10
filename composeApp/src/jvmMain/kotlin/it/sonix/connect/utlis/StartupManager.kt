// it.sonix.connect.utils.StartupManager.kt
package it.sonix.connect.utlis

import java.io.File
import java.nio.file.Paths

object StartupManager {

    private val startupDir =
        Paths.get(
            System.getenv("APPDATA"),
            "Microsoft",
            "Windows",
            "Start Menu",
            "Programs",
            "Startup"
        ).toFile()

    private val shortcutName = "SonixConnect.lnk"

    fun isEnabled(): Boolean =
        File(startupDir, shortcutName).exists()


    fun enable() {
        val exePath = getExePath() ?: return

        val script = $$"""
            $s = (New-Object -COM WScript.Shell).CreateShortcut('$${startupDir.absolutePath}\\$$shortcutName')
            $s.TargetPath = '$$exePath'
            $s.WorkingDirectory = '$${File(exePath).parent}'
            $s.Save()
        """.trimIndent()

        ProcessBuilder(
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy", "Bypass",
            "-Command", script
        ).start()
    }

    fun disable() {
        File(startupDir, shortcutName).delete()
    }

    private fun getExePath(): String? =
        ProcessHandle.current()
            .info()
            .command()
            .orElse(null)
}
