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
    primaryContainer = AccentLime,
    onPrimaryContainer = NavyPrimary,
    secondary = SlateSecondary,
    onSecondary = Color.White,
    secondaryContainer = AccentLavender,
    onSecondaryContainer = NavyPrimary,
    tertiaryContainer = AccentPeach,
    onTertiaryContainer = NavyPrimary,
    background = AppBackgroundLight,
    onBackground = NavyPrimary,
    surface = CardBackground,
    onSurface = NavyPrimary,
    surfaceVariant = PaperSunken,
    onSurfaceVariant = MutedText,
    outline = Color(0xFFCFCDC6),
    outlineVariant = HairlineBorder,
    error = TrendRed
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentLime,
    onPrimary = NavyPrimary,
    primaryContainer = AccentLime,
    onPrimaryContainer = NavyPrimary,
    secondary = AccentLavender,
    onSecondary = NavyPrimary,
    secondaryContainer = AccentLavender,
    onSecondaryContainer = NavyPrimary,
    tertiaryContainer = AccentPeach,
    onTertiaryContainer = NavyPrimary,
    background = Color(0xFF121212),
    onBackground = Color(0xFFF2F1EC),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF2F1EC),
    surfaceVariant = Color(0xFF26262A),
    onSurfaceVariant = Color(0xFFA2A1A8),
    outline = Color(0xFF45454B),
    outlineVariant = Color(0xFF2E2E33),
    error = TrendRed
)

/** Flat warm "paper" background (kept as a list so existing gradient call sites keep working). */
val AppBackgroundGradient = listOf(
    AppBackgroundLight,
    AppBackgroundMid,
    AppBackgroundEnd
)

/** Default background gradient for Friends List screen: gentle light blue to crisp white. */
val FriendsListBackgroundGradient = listOf(
    FriendsBackgroundStart,
    FriendsBackgroundMid,
    FriendsBackgroundEnd
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
