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
    primary = KadyCyan,
    onPrimary = KadyNavyDark,
    primaryContainer = KadyNavySurface,
    onPrimaryContainer = Color.White,
    secondary = KadyBlue,
    onSecondary = Color.White,
    background = KadyNavyDark,
    onBackground = KadyDarkTextPrimary,
    surface = KadyNavySurface,
    onSurface = KadyDarkTextPrimary,
    surfaceVariant = KadyNavyCard,
    onSurfaceVariant = KadyDarkTextSecondary,
    outline = KadyNavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = KadyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0EAFC),
    onPrimaryContainer = KadyBlue,
    secondary = KadyCyan,
    onSecondary = KadyNavyDark,
    background = KadyLightBg,
    onBackground = KadyLightTextPrimary,
    surface = KadyLightSurface,
    onSurface = KadyLightTextPrimary,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = KadyLightTextSecondary,
    outline = KadyLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand Kady Tech identity by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
