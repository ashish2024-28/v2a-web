package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.ui.viewmodel.ThemeMode

private fun darkScheme(accent: AccentColor) = darkColorScheme(
    primary = accent.darkPrimary,
    onPrimary = Color.Black,
    primaryContainer = accent.darkPrimary.copy(alpha = 0.85f),
    onPrimaryContainer = if (accent == AccentColor.YELLOW || accent == AccentColor.ORANGE) Color(0xFF101010) else Color.White,
    secondary = PlayitCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003554),
    onSecondaryContainer = Color(0xFF90E0EF),
    tertiary = accent.darkPrimary,
    onTertiary = Color.Black,
    background = VlcDarkBackground,
    onBackground = VlcDarkOnBackground,
    surface = VlcDarkSurface,
    onSurface = VlcDarkOnSurface,
    surfaceVariant = VlcDarkSurfaceVariant,
    onSurfaceVariant = VlcDarkOnSurfaceVariant,
    outline = VlcDarkOutline
)

private fun lightScheme(accent: AccentColor) = lightColorScheme(
    primary = accent.lightPrimary,
    onPrimary = Color.White,
    primaryContainer = accent.primary.copy(alpha = 0.22f),
    onPrimaryContainer = Color(0xFF202020),
    secondary = PlayitBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = accent.lightPrimary,
    onTertiary = Color.White,
    background = VlcLightBackground,
    onBackground = VlcLightOnBackground,
    surface = VlcLightSurface,
    onSurface = VlcLightOnSurface,
    surfaceVariant = VlcLightSurfaceVariant,
    onSurfaceVariant = VlcLightOnSurfaceVariant,
    outline = VlcLightOutline
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    fontScale: Float = 1.00f,
    uiScale: Float = 1.00f,
    accentColor: AccentColor = AccentColor.YELLOW,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) {
        darkScheme(accentColor)
    } else {
        lightScheme(accentColor)
    }

    val safeFontScale = fontScale.coerceIn(0.50f, 1.50f)
    val safeUiScale = uiScale.coerceIn(0.80f, 1.40f)

    val currentDensity = LocalDensity.current
    
    // Ignore external phone system font scaling (which can blow up or distort layouts on different phones).
    // The application controls its own clean, normalized, and professional font size:
    // Baseline is 0.95f for compact, clean typography across all phone screen sizes.
    // Users can adjust this via in-app Settings -> Text & Font Size slider.
    val scaledDensity = Density(
        density = currentDensity.density * 0.95f * safeUiScale,
        fontScale = 0.95f * safeFontScale
    )

    CompositionLocalProvider(
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
