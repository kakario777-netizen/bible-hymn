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

/** 이북판: 색 없이 흰 바탕·검은 글자. 선택·강조만 옅은 회색. */
private val EinkLight = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E6E6),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color.White,
    onSecondaryContainer = Color.Black,
    tertiary = Color.Black,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF333333),
    surfaceTint = Color.White,
    surfaceBright = Color.White,
    surfaceDim = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    outline = Color(0xFF555555),
    outlineVariant = Color(0xFF999999),
)

/** 이북판 '검은색' 배경: 검은 바탕·흰 글자. */
private val EinkDark = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = Color.Black,
    onSecondaryContainer = Color.White,
    tertiary = Color.White,
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFCCCCCC),
    surfaceTint = Color.Black,
    surfaceBright = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Color.Black,
    surfaceContainerHigh = Color.Black,
    surfaceContainerHighest = Color.Black,
    outline = Color(0xFFAAAAAA),
    outlineVariant = Color(0xFF666666),
)

@Composable
fun AppTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val eink = com.kakario.biblehymn.BuildConfig.IS_EINK
    MaterialTheme(
        colorScheme = when {
            eink && dark -> EinkDark
            eink -> EinkLight
            dark -> DarkColors
            else -> LightColors
        },
        content = content,
    )
}
