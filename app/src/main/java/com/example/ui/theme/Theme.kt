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
    primary = CinemaRed,
    onPrimary = Color.White,
    primaryContainer = CinemaRedDark,
    onPrimaryContainer = Color.White,
    secondary = CinemaGold,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = CinemaTextPrimary,
    surface = DarkSurface,
    onSurface = CinemaTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = CinemaTextSecondary
)

private val LightColorScheme = DarkColorScheme // Prefer cinematic dark theme for movie streaming experience

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek cinema dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
