package it.sonix.connect.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import java.net.InetAddress

data class WledDevice(
    val name: String,
    val ip: String
)

class WLEDDiscoveryService {

    suspend fun scan(timeoutMs: Long = 4000): List<WledDevice> =
        withContext(Dispatchers.IO) {

            val devices = mutableMapOf<String, WledDevice>()
            val jmdns = JmDNS.create(InetAddress.getLocalHost())

            val listener = object : ServiceListener {
                override fun serviceAdded(event: ServiceEvent) {
                    jmdns.requestServiceInfo(event.type, event.name, true)
                }

                override fun serviceResolved(event: ServiceEvent) {
                    val info = event.info
                    val ip = info.inet4Addresses.firstOrNull()?.hostAddress
                        ?: return

                    devices[ip] = WledDevice(
                        name = info.name,
                        ip = ip
                    )
                }

                override fun serviceRemoved(event: ServiceEvent) {}
            }

            jmdns.addServiceListener("_wled._tcp.local.", listener)

            // wait for responses
            kotlinx.coroutines.delay(timeoutMs)

            jmdns.removeServiceListener("_wled._tcp.local.", listener)
            jmdns.close()

            devices.values.toList()
        }
}
