package com.lifeguard.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FE3B2),
    onPrimary = Color(0xFF08111F),
    background = Color(0xFF08111F),
    surface = Color(0xFF101C2D),
    surfaceVariant = Color(0xFF15243A),
    secondary = Color(0xFFA99AF7),
    tertiary = Color(0xFFF5C878),
    error = Color(0xFFF27777),
    onBackground = Color(0xFFF4F7FB),
    onSurface = Color(0xFFF4F7FB),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF168B58),
    background = Color(0xFFF5F8FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF0F7),
    secondary = Color(0xFF6855D8),
    tertiary = Color(0xFFA56E0A),
)

@Composable
fun LifeGuardTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
