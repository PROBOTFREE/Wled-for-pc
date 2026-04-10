package it.sonix.connect.screens

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.sonix.connect.theme.ThemeState
import it.sonix.connect.utils.AppLogger
import it.sonix.connect.utlis.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {

    var runOnStartup by remember { mutableStateOf(false) }
    var autoStartSync by remember { mutableStateOf(false) }
    var loggingEnabled by remember { mutableStateOf(false) }

    /* ---- Load once ---- */
    LaunchedEffect(Unit) {
        runOnStartup = DesktopDataStore.isRunOnStartupEnabled()
        autoStartSync = DesktopDataStore.isAutoStartEnabled()
        loggingEnabled = DesktopDataStore.isLoggingEnabled()
    }

    /* ---- Persist changes ---- */

    LaunchedEffect(runOnStartup) {
        DesktopDataStore.setRunOnStartup(runOnStartup)
        if (runOnStartup) StartupManager.enable()
        else StartupManager.disable()
    }

    LaunchedEffect(autoStartSync) {
        DesktopDataStore.setAutoStart(autoStartSync)
    }

    LaunchedEffect(loggingEnabled) {
        DesktopDataStore.setLoggingEnabled(loggingEnabled)
        AppLogger.setEnabled(loggingEnabled)
    }

    /* ---- UI ---- */

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Settings", style = MaterialTheme.typography.headlineMedium)

            HorizontalDivider()

            Text(
                "Appearance",
                style = MaterialTheme.typography.titleMedium
            )

            ThemeModeSelector()

            HorizontalDivider()

            SettingToggle(
                title = "Run on system startup",
                checked = runOnStartup,
                onChange = { runOnStartup = it }
            )

            SettingToggle(
                title = "Auto-start sync on launch",
                checked = autoStartSync,
                onChange = { autoStartSync = it }
            )

            HorizontalDivider()

            SettingToggle(
                title = "Enable debug logging",
                checked = loggingEnabled,
                onChange = { loggingEnabled = it }
            )
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


@Composable
private fun SettingToggle(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onChange(!checked) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onChange
            )
        }
    }
}



@Composable
private fun ThemeModeSelector() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        ThemeOptionItem(
            title = "System",
            subtitle = "Follow OS appearance",
            value = THEME_SYSTEM
        )

        ThemeOptionItem(
            title = "Light",
            subtitle = "Always use light theme",
            value = THEME_LIGHT
        )

        ThemeOptionItem(
            title = "Dark",
            subtitle = "Always use dark theme",
            value = THEME_DARK
        )
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    value: String
) {
    val selected = ThemeState.mode.value == value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14))
            .clickable {
                ThemeState.mode.value = value
                DesktopDataStore.setThemeMode(value)
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (selected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = selected,
                onClick = null // handled by Card click
            )
        }
    }
}
