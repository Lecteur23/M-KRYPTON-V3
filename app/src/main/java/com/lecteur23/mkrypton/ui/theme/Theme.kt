package com.lecteur23.mkrypton.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KryptonColorScheme: ColorScheme = darkColorScheme(
    primary = KryptonCyan,
    secondary = KryptonBlue,
    tertiary = KryptonAmber,
    background = KryptonBackground,
    surface = KryptonSurface,
    surfaceVariant = KryptonPanel,
    outline = KryptonLine,
    onPrimary = KryptonBackground,
    onSecondary = KryptonText,
    onTertiary = KryptonBackground,
    onBackground = KryptonText,
    onSurface = KryptonText
)

@Composable
fun KryptonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KryptonColorScheme,
        content = content
    )
}

