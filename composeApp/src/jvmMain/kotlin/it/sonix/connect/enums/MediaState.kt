package it.sonix.connect.enums
import kotlinx.serialization.Serializable

@Serializable
data class MediaState(
    val version: String,
    val title: String,
    val artist: String,
    val hasAlbumArt: Boolean,
    val albumArtBase64: String? = null
)
