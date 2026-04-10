package it.sonix.connect.mediabridge

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import it.sonix.connect.enums.MediaState
import kotlinx.serialization.json.Json

class MediaBridgeClient {

    companion object {
        private const val NOW_PLAYING_URL = "http://127.0.0.1:8765/now-playing"
    }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    suspend fun fetchNowPlaying(): MediaState {
        println("Requesting now-playing from Media Bridge")

        return try {
            client.get(NOW_PLAYING_URL)
                .body<MediaState>()
        } catch (e: Exception) {
            println("Media Bridge error: ${e.message}")
            throw e
        }
    }

    suspend fun fetchBridgeVersion(): String? {
        return try {
            val state: MediaState = client.get(NOW_PLAYING_URL).body()
            state.version
        } catch (e: Exception) {
            println("Failed to get bridge version: ${e.message}")
            null
        }
    }

}
