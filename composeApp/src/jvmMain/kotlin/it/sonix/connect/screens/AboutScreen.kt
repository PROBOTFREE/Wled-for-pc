package it.sonix.connect.screens

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1100.dp)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    /* ---------- HEADER ---------- */

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Sonix Connect",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Desktop WLED Music Sync",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    /* ---------- INFO ROW ---------- */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoCard(
                            title = "Version",
                            value = "v1.0.0 (Desktop)",
                            modifier = Modifier.weight(1f)
                        )
                        InfoCard(
                            title = "Platform",
                            value = "Windows • macOS • Linux",
                            modifier = Modifier.weight(1f)
                        )
                        InfoCard(
                            title = "Engine",
                            value = "Kotlin + Compose Desktop",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    /* ---------- ABOUT ---------- */

                    SectionCard(title = "What is Sonix Connect?") {
                        Text(
                            "Sonix Connect is a desktop application that synchronizes your WLED-powered " +
                                    "lights with real-time music playback. It captures system media data and " +
                                    "sends low-latency color and brightness updates directly to your WLED devices."
                        )
                    }

                    /* ---------- FEATURES ---------- */

                    SectionCard(title = "Key Features") {
                        FeatureItem("Automatic WLED device discovery")
                        FeatureItem("Manual IP configuration support")
                        FeatureItem("Real-time music-to-light synchronization")
                        FeatureItem("Multiple WLED device support")
                        FeatureItem("Low-latency desktop media bridge")
                        FeatureItem("Clean, modern desktop interface")
                    }

                    /* ---------- TECHNOLOGY ---------- */

                    SectionCard(title = "Technology") {
                        FeatureItem("Kotlin Multiplatform")
                        FeatureItem("Jetpack Compose Desktop")
                        FeatureItem("WLED JSON API")
                        FeatureItem("Desktop Media Session APIs")
                        FeatureItem("Local network UDP/HTTP communication")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    /* ---------- FOOTER ---------- */

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Built with ❤️ for music & lights",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "© 2026 Sonix Project",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

/* ---------- COMPONENTS ---------- */

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Text(
        text = "•  $text",
        style = MaterialTheme.typography.bodyMedium
    )
}
