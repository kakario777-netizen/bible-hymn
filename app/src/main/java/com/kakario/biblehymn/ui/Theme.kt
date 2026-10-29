package com.kakario.biblehymn.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6B4226),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1E3D3),
    onPrimaryContainer = Color(0xFF2E1A0E),
    secondary = Color(0xFF8A6A3B),
    secondaryContainer = Color(0xFFEFE6D6),
    onSecondaryContainer = Color(0xFF2B2113),
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFAF7F2),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFEDE6DC),
    onSurfaceVariant = Color(0xFF5A5046),
    outline = Color(0xFFB3A898),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE2BE96),
    onPrimary = Color(0xFF3A230F),
    primaryContainer = Color(0xFF4A3220),
    onPrimaryContainer = Color(0xFFF6E3CF),
    secondary = Color(0xFFD8C29B),
    secondaryContainer = Color(0xFF3A3122),
    onSecondaryContainer = Color(0xFFF0E2C8),
    background = Color(0xFF15130F),
    onBackground = Color(0xFFEAE2D8),
    surface = Color(0xFF15130F),
    onSurface = Color(0xFFEAE2D8),
    surfaceVariant = Color(0xFF2A251F),
    onSurfaceVariant = Color(0xFFCFC4B6),
    outline = Color(0xFF7A6F62),
)

@Composable
fun AppTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
