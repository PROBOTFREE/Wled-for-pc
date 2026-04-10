package it.sonix.connect.mediabridge

import java.io.File

object MediaBridgeLauncher {

    private var process: Process? = null

    fun start(): Process {
        // Already running → reuse
        if (process?.isAlive == true) {
            println("MediaBridge already running")
            return process!!
        }

        /* ---------------- SAFE INSTALL LOCATION ---------------- */

        val baseDir = File(
            System.getenv("LOCALAPPDATA"),
            "SonixConnect/MediaBridge"
        )

        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }

        val exeFile = File(baseDir, "SonixMediaBridge.exe")

        /* ---------------- EXTRACT FROM RESOURCES ---------------- */

        if (!exeFile.exists()) {
            println("Extracting MediaBridge.exe to ${exeFile.absolutePath}")

            val input = MediaBridgeLauncher::class.java
                .getResourceAsStream("/mediabridge/SonixMediaBridge.exe")
                ?: error("❌ SonixMediaBridge.exe missing from resources")

            exeFile.outputStream().use { output ->
                input.copyTo(output)
            }

            // Ensure Windows allows execution
            exeFile.setReadable(true)
            exeFile.setWritable(true)
            exeFile.setExecutable(true)

            // Remove SmartScreen block (best effort)
            try {
                ProcessBuilder(
                    "powershell",
                    "-Command",
                    "Unblock-File \"${exeFile.absolutePath}\""
                ).start()
            } catch (e: Exception) {
                // Non-fatal (some systems don't allow this)
                println("Unblock-File failed: ${e.message}")
            }
        }

        /* ---------------- LAUNCH PROCESS ---------------- */

        println("Starting MediaBridge from ${exeFile.absolutePath}")

        process = try {
            ProcessBuilder(exeFile.absolutePath)
                .directory(baseDir)            // VERY IMPORTANT
                .redirectErrorStream(true)
                .start()
        } catch (e: Throwable) {
            e.printStackTrace()
            error("❌ Failed to start MediaBridge")
        }

        return process!!
    }

    fun stop() {
        println("Stopping MediaBridge")
        process?.destroy()
        process = null
    }


    fun repair() {

        println("Repairing MediaBridge")

        stop()

        val baseDir = File(
            System.getenv("LOCALAPPDATA"),
            "SonixConnect/MediaBridge"
        )

        val exeFile = File(baseDir, "SonixMediaBridge.exe")

        try {

            if (exeFile.exists()) {
                println("Deleting old MediaBridge")
                exeFile.delete()
            }

            val input = MediaBridgeLauncher::class.java
                .getResourceAsStream("/mediabridge/SonixMediaBridge.exe")
                ?: error("❌ SonixMediaBridge.exe missing from resources")

            exeFile.outputStream().use { output ->
                input.copyTo(output)
            }

            exeFile.setReadable(true)
            exeFile.setWritable(true)
            exeFile.setExecutable(true)

            println("MediaBridge repaired")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
