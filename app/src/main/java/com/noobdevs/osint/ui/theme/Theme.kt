package com.noobdevs.osint.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    darkTheme: Boolean = false, // LIGHT MODE DEFAULT!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                // In light mode, status bar icons should be dark; in dark mode, light icons
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}