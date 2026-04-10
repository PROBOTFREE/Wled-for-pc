package it.sonix.connect.service

import it.sonix.connect.mediabridge.MediaBridgeClient
import kotlinx.coroutines.*

class DesktopSyncService(
    private val wledIps: List<String>
) {
    private val mediaClient = MediaBridgeClient()
    private val wledSync = DesktopWLEDSync(wledIps)

    private var job: Job? = null
    private var lastColor: Triple<Int, Int, Int>? = null

    fun start() {
        if (job != null) return

        println("DesktopSyncService started")

        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    println("Fetching now-playing…")

                    val media = mediaClient.fetchNowPlaying()

                    println(
                        "Now playing: ${media.title} - ${media.artist}, art=${media.hasAlbumArt}"
                    )

                    if (media.hasAlbumArt && !media.albumArtBase64.isNullOrBlank()) {

                        val color =
                            AlbumArtColorExtractor.extractDominantColor(
                                media.albumArtBase64
                            )

                        if (color != null) {
                            val (r, g, b) = color
                            val newColor = Triple(r, g, b)

                            if (newColor != lastColor) {
                                println("Sending color → R=$r G=$g B=$b")
                                wledSync.sendColor(r, g, b)
                                lastColor = newColor
                            } else {
                                println("Color unchanged, skipping")
                            }

                        } else {
                            println("Color extraction failed")
                        }
                    } else {
                        println("No album art available")
                    }

                } catch (e: Exception) {
                    println("Sync error: ${e.message}")
                    e.printStackTrace()
                }

                delay(1500) // slower + safer
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}