package it.sonix.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import it.sonix.connect.component.TitleBar
import it.sonix.connect.mediabridge.MediaBridgeLauncher
import it.sonix.connect.theme.ThemeState
import it.sonix.connect.utils.AppLogger
import it.sonix.connect.utlis.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import kotlin.system.exitProcess

fun main() {

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        AppLogger.error("Fatal uncaught exception", e)
        exitProcess(1)
    }

    application {

        var windowVisible by remember { mutableStateOf(true) }

        // ✅ Load tray icon INSIDE composition
        val trayIcon = painterResource("sonix_logo.png")

        // ---- SYSTEM TRAY ----
        Tray(
            icon = trayIcon, // we define this below
            tooltip = "Sonix Connect",
            menu = {
                Item(
                    if (windowVisible) "Hide" else "Show",
                    onClick = { windowVisible = !windowVisible }
                )

                Separator()

                Item("Exit", onClick = {
                    try {
                        MediaBridgeLauncher.stop()
                    } catch (_: Exception) {}

                    exitApplication()
                })
            }
        )

        if (windowVisible) {
            Window(
                title = "Sonix Connect",
                icon = trayIcon,
                onCloseRequest = {
                    // 🚫 DO NOT EXIT — just hide
                    windowVisible = false
                },
               /* undecorated = true,*/
            ) {

                window.minimumSize = java.awt.Dimension(900, 600)


                // Load saved theme
                LaunchedEffect(Unit) {
                    ThemeState.mode.value = DesktopDataStore.getThemeMode()
                }

                // Start MediaBridge AFTER UI shows
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        try {
                            MediaBridgeLauncher.start()
                        } catch (e: Exception) {
                            AppLogger.error("MediaBridge failed", e)
                            exitProcess(1)
                        }
                    }
                }

                val systemDark = isSystemInDarkTheme()

                val isDark = when (ThemeState.mode.value) {
                    THEME_DARK -> true
                    THEME_LIGHT -> false
                    else -> systemDark
                }

                MaterialTheme(
                    colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides
                                if (isDark) Color.White
                                else MaterialTheme.colorScheme.onSurface
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize(),
                            color = Color(0xFF1D1F2A)

                        ) {
                            Column {
                                /*WindowDraggableArea{
                                    TitleBar(window)
                                }*/

                                DesktopApp()
                            }
                        }
                    }
                }
            }
        }
    }
}
