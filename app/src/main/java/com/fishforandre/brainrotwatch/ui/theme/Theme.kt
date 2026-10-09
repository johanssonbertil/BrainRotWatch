package com.fishforandre.brainrotwatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    secondary = SlateSecondary,
    onSecondary = Color.White,
    background = AppBackgroundLight,
    onBackground = NavyPrimary,
    surface = CardBackground,
    onSurface = NavyPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = MutedText
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB0B7E6),
    onPrimary = Color(0xFF1E2040),
    secondary = Color(0xFF9FA5C4),
    onSecondary = Color(0xFF1E2040),
    background = Color(0xFF101222),
    onBackground = Color(0xFFE2E5F8),
    surface = Color(0xFF1A1C30),
    onSurface = Color(0xFFE2E5F8),
    surfaceVariant = Color(0xFF1A1C30),
    onSurfaceVariant = Color(0xFFA0A5C0)
)

val AppBackgroundGradient = listOf(
    AppBackgroundLight,
    AppBackgroundMid,
    AppBackgroundEnd
)

@Composable
fun BrainRotWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
