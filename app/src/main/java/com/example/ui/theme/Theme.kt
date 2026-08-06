package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightIndustrialBlue,
    onPrimary = Color.White,
    secondary = AccentAmber,
    background = SurfaceDark,
    surface = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = IndustrialBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = DarkIndustrialBlue,
    secondary = AccentAmber,
    onSecondary = Color.Black,
    background = SurfaceLight,
    onBackground = Color(0xFF1A1C1E),
    surface = CardWhite,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE7EEF8),
    onSurfaceVariant = Color(0xFF43474E)
)

@Composable
fun BallyWeighbridgeTheme(
    darkTheme: Boolean = false, // Force crisp light industrial theme for high-contrast visibility
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

