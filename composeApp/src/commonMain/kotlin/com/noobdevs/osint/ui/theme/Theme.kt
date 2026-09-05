package com.noobdevs.osint.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CorporatePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF1E293B),
    background = CorporateBackground,
    onBackground = CorporateTextPrimary,
    surface = CorporateSurface,
    onSurface = CorporateTextPrimary,
    surfaceVariant = CorporateSurfaceElevated,
    onSurfaceVariant = CorporateTextSecondary,
    outline = CorporateBorder,
    error = StatusRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = TacticalCyberCyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004F55),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = TacticalTextSecondary,
    onSecondary = Color.Black,
    secondaryContainer = TacticalDarkSurfaceElevated,
    onSecondaryContainer = TacticalTextPrimary,
    background = TacticalDarkBackground,
    onBackground = TacticalTextPrimary,
    surface = TacticalDarkSurface,
    onSurface = TacticalTextPrimary,
    surfaceVariant = TacticalDarkSurfaceElevated,
    onSurfaceVariant = TacticalTextSecondary,
    outline = TacticalDarkBorder,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun OSINTTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
