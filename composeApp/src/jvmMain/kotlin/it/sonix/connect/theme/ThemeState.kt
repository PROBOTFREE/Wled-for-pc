package it.sonix.connect.theme

import androidx.compose.runtime.mutableStateOf
import it.sonix.connect.utlis.THEME_SYSTEM

object ThemeState {
    // system | light | dark
    val mode = mutableStateOf(THEME_SYSTEM)
}
