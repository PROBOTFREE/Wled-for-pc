package it.sonix.connect.service

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class DesktopWLEDSync(
    private val wledIps: List<String>
) {

    fun sendColor(r: Int, g: Int, b: Int) {
        val (nr, ng, nb) = normalizeColor(r, g, b)

        wledIps.forEach { ip ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url =
                        URL("http://$ip/win&T=1&R=$nr&G=$ng&B=$nb")
                    with(url.openConnection() as HttpURLConnection) {
                        connectTimeout = 300
                        readTimeout = 300
                        requestMethod = "GET"
                        inputStream.use { it.readBytes() }
                    }
                } catch (e: Exception) {
                    println("WLED error ($ip): ${e.message}")
                }
            }
        }
    }

    private fun normalizeColor(
        r: Int,
        g: Int,
        b: Int,
        target: Int = 255
    ): Triple<Int, Int, Int> {
        val max = maxOf(r, g, b)
        return if (max == 0) {
            Triple(target, target, target)
        } else {
            Triple(
                (r * target / max).coerceAtMost(255),
                (g * target / max).coerceAtMost(255),
                (b * target / max).coerceAtMost(255)
            )
        }
    }
}
