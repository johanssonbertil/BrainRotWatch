package com.fishforandre.brainrotwatch.ui.theme

import androidx.compose.ui.graphics.Color

// Primary & Text Colors ("Ink")
val NavyPrimary = Color(0xFF141416)
val SlateSecondary = Color(0xFF2E2E33)
val MutedText = Color(0xFF8A8A90)
val LabelText = Color(0xFF8A8A90)
val AvatarTextDark = Color(0xFF141416)

// Surface & Background Colors ("Paper")
val AppBackgroundLight = Color(0xFFF6F5F1)
val AppBackgroundMid = Color(0xFFF6F5F1)
val AppBackgroundEnd = Color(0xFFF3F1EC)
val CardBackground = Color(0xFFFFFFFF)
val PaperSunken = Color(0xFFF0EEE8)
val HairlineBorder = Color(0xFFE8E6E0)

// Flat Pastel Accents (community tiles, avatars, badges)
val AccentLime = Color(0xFFDDF86A)
val AccentLimeDeep = Color(0xFFC3E447)
val AccentPeach = Color(0xFFFFAD8F)
val AccentPeachDeep = Color(0xFFF59573)
val AccentLavender = Color(0xFFC9B8FF)
val AccentMint = Color(0xFFB9EFD3)
val AccentSky = Color(0xFFBCDDFF)
val AccentButter = Color(0xFFFFE48A)
val ProfilePurple = Color(0xFF6C4DF0)

// Status Colors
val TrendGreen = Color(0xFF1F8A4C)
val TrendRed = Color(0xFFE0523A)
val OnlineGreen = Color(0xFF34C46B)
val WarningAmber = Color(0xFFFFA24B)
val OfflineGrey = Color(0xFFB0AFAA)

// Avatar & Badge Colors
val AvatarBackground = ProfilePurple
val AvatarIconBackground = AccentLavender
val BadgeNotificationRed = Color(0xFFFF5A47)

// App Usage Palette
val UsagePurple = Color(0xFF8D74FF)
val UsageCyan = Color(0xFF3CC8A0)
val UsagePink = Color(0xFFFF8A6A)
val UsageTrackDefault = Color(0xFFEFEDE7)

/** Rotating pastel palette used for initials avatars and community tiles. */
val PastelPalette = listOf(AccentLavender, AccentPeach, AccentMint, AccentSky, AccentButter)

/** Stable pastel color for a given name (same person always gets the same color). */
fun pastelFor(key: String): Color =
    PastelPalette[(key.lowercase().hashCode() and Int.MAX_VALUE) % PastelPalette.size]
