package it.sonix.connect.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import it.sonix.connect.mediabridge.MediaBridgeClient
import it.sonix.connect.mediabridge.MediaBridgeLauncher
import it.sonix.connect.service.DesktopSyncService
import it.sonix.connect.service.WLEDDiscoveryService
import it.sonix.connect.service.WledDevice
import it.sonix.connect.utlis.DesktopDataStore
import it.sonix.connect.utlis.WLED_IPS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

@Composable
fun HomeScreen() {
    /* ---------------- STATE ---------------- */

    var isRunning by remember { mutableStateOf(false) }

    var selectedIps by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        selectedIps = DesktopDataStore.getSelectedIps()
    }

    /* ---------------- AUTO SAVE IPS ---------------- */

    LaunchedEffect(selectedIps) {
        DesktopDataStore.saveSelectedIpsNow(selectedIps)
    }


    var useManualInput by remember { mutableStateOf(false) }
    var editedIp by remember { mutableStateOf("") }

    var foundDevices by remember { mutableStateOf<List<WledDevice>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    val discoveryService = remember { WLEDDiscoveryService() }
    val scope = rememberCoroutineScope()

    // manage sync service instance explicitly
    var syncService by remember { mutableStateOf<DesktopSyncService?>(null) }




    var bridgeVersion by remember { mutableStateOf<String?>(null) }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }

    val bridgeClient = remember { MediaBridgeClient() }

    fun checkBridgeVersion() {
        scope.launch {
            checkingUpdate = true

            try {
                bridgeVersion = bridgeClient.fetchBridgeVersion()

                // Replace with your real API
                latestVersion = withContext(Dispatchers.IO) {
                    try {
                        java.net.URL("https://your-server.com/bridge-version.txt")
                            .readText()
                            .trim()
                    } catch (e: Exception) {
                        null
                    }
                }

            } finally {
                checkingUpdate = false
            }
        }
    }



    fun updateBridge() {
        try {

            val url = java.net.URL("https://your-server.com/mediabridge.exe")
            val connection = url.openConnection()

            val file = java.io.File("mediabridge.exe")

            connection.getInputStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            println("MediaBridge updated")

        } catch (e: Exception) {
            println("Update failed: ${e.message}")
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        checkBridgeVersion()
    }

    val scrollState = rememberScrollState()


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                /* ========== LEFT PANEL ========== */

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Color(0xFF313238),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        "Desktop WLED Sync",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text(
                        "Media Bridge Version",
                        style = MaterialTheme.typography.bodyMedium
                    )


                    Text(
                        "Installed: ${bridgeVersion ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (latestVersion != null) {
                        Text(
                            "Latest: ${latestVersion ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        MediaBridgeLauncher.stop()
                                        MediaBridgeLauncher.repair()
                                        MediaBridgeLauncher.start()
                                    }

                                    checkBridgeVersion()
                                }
                            }
                        ) {
                            Text("Repair Bridge")
                        }

                        OutlinedButton(
                            enabled = !checkingUpdate,
                            onClick = { checkBridgeVersion() }
                        ) {
                            Text(if (checkingUpdate) "Checking..." else "Check Update")
                        }

                        val updateAvailable =
                            bridgeVersion != null &&
                                    latestVersion != null &&
                                    bridgeVersion != latestVersion

                        if (updateAvailable) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            updateBridge()
                                        }

                                        checkBridgeVersion()
                                    }
                                }
                            ) {
                                Text("Update")
                            }
                        }
                    }

                    StatusRow("WLED Devices", selectedIps.isNotEmpty())
                    StatusRow("Sync Status", isRunning)

                    HorizontalDivider()

                    /* ---- Scan ---- */
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isScanning,
                        onClick = {
                            // launch on main, do scan on IO and then update state on main
                            scope.launch {
                                isScanning = true
                                val devices = withContext(Dispatchers.IO) {
                                    discoveryService.scan()
                                }
                                foundDevices = devices
                                isScanning = false
                            }
                        }
                    ) {
                        Text(if (isScanning) "Scanning…" else "Scan for WLED Devices")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Manual IP Input",
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = useManualInput,
                            onCheckedChange = { useManualInput = it }
                        )
                    }

                    /* ---- Manual IP Input ---- */
                    AnimatedVisibility(useManualInput) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editedIp,
                                onValueChange = { editedIp = it },
                                placeholder = { Text("e.g. 192.168.1.50") },
                                singleLine = true,
                                isError = editedIp.isNotBlank() && !isValidIp(editedIp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isValidIp(editedIp),
                                onClick = {
                                    if (!selectedIps.contains(editedIp)) {
                                        selectedIps = selectedIps + editedIp
                                        editedIp = ""
                                    }
                                }
                            ) {
                                Text("Add IP")
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = if (isRunning) true else selectedIps.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (isRunning)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            if (isRunning) {
                                // STOP
                                syncService?.stop()
                                syncService = null
                                isRunning = false
                            } else {
                                // START
                                syncService = DesktopSyncService(selectedIps).also { it.start() }
                                isRunning = true
                            }
                        }
                    ) {
                        Text(
                            if (isRunning) "Stop Sync" else "Start Sync",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                }

                /* ========== RIGHT PANEL ========== */

                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(
                            Color(0xFF313238),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text("Discovered WLED Devices", style = MaterialTheme.typography.titleMedium)

                    if (foundDevices.isEmpty()) {
                        Text(
                            "No devices found yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    foundDevices.forEach { device ->
                        val selected = selectedIps.contains(device.ip)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    if (selected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                            ),
                            onClick = {
                                selectedIps =
                                    if (selected)
                                        selectedIps - device.ip
                                    else
                                        selectedIps + device.ip
                            }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(device.name, fontWeight = FontWeight.Bold)
                                Text(device.ip, style = MaterialTheme.typography.bodySmall)

                                Spacer(Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { openWled(device.ip) }
                                    ) {
                                        Text("Open")
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Selected WLED IPs", style = MaterialTheme.typography.titleMedium)

                    if (selectedIps.isEmpty()) {
                        Text(
                            "No WLED devices selected.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    selectedIps.forEach { ip ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column {
                                    Text(ip, fontWeight = FontWeight.Bold)
                                    Text(
                                        "WLED Web UI",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.weight(1f))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { openWled(ip) }) {
                                        Text("Open")
                                    }
                                    TextButton(onClick = { selectedIps = selectedIps - ip }) {
                                        Text("Remove")
                                    }
                                }
                            }

                        }
                    }

                    if (selectedIps.isNotEmpty()) {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedIps = emptyList() }
                        ) {
                            Text("Clear All")
                        }
                    }
                }
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            style = ScrollbarStyle(
                minimalHeight = 16.dp,
                thickness = 8.dp, // bigger scrollbar
                shape = RoundedCornerShape(6.dp),
                hoverDurationMillis = 300,
                unhoverColor =
                    if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        Color.White.copy(alpha = 0.4f) // dark mode
                    else
                        Color.Gray.copy(alpha = 0.6f), // light mode
                hoverColor =
                    if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        Color.White
                    else
                        Color.DarkGray
            ),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 4.dp)
        )
    }
}

/* ---------------- HELPERS ---------------- */

@Composable
private fun StatusRow(label: String, status: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    if (status) Color(0xFF4CAF50) else Color(0xFFE53935),
                    RoundedCornerShape(50)
                )
        )
        Text(label)
    }
}

private fun isValidIp(ip: String): Boolean =
    ip.split(".").let { parts ->
        parts.size == 4 && parts.all {
            it.toIntOrNull()?.let { n -> n in 0..255 } == true
        }
    }

private fun openWled(ip: String) {
    runCatching {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI("http://$ip"))
        }
    }
}
