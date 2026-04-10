package it.sonix.connect.service

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

object AlbumArtColorExtractor {

    fun extractDominantColor(base64: String): Triple<Int, Int, Int>? {
        val bytes = Base64.getDecoder().decode(base64)
        val image: BufferedImage =
            ImageIO.read(ByteArrayInputStream(bytes)) ?: return null

        var r = 0
        var g = 0
        var b = 0
        var count = 0

        val step = 10 // performance

        for (y in 0 until image.height step step) {
            for (x in 0 until image.width step step) {
                val rgb = image.getRGB(x, y)
                r += (rgb shr 16) and 0xFF
                g += (rgb shr 8) and 0xFF
                b += rgb and 0xFF
                count++
            }
        }

        if (count == 0) return null
        return Triple(r / count, g / count, b / count)
    }
}
