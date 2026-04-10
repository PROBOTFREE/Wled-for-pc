package it.sonix.connect.utlis

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun isSystemDark(): Boolean {
    return isSystemInDarkTheme()
}
