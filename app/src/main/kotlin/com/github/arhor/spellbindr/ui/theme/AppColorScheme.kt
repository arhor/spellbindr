package com.github.arhor.spellbindr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFC6A866)
private val DeepGold = Color(0xFF8F7843)
private val GoldOnDark = Color(0xFF1C1305)
private val GoldOnLight = Color(0xFF201000)
private val GoldContainerDark = Color(0xFF3E2B0B)
private val GoldContainerLight = Color(0xFFF5E4C2)

private val Violet = Color(0xFF8C7BB6)
private val VioletContainerDark = Color(0xFF3D2F5A)
private val VioletLight = Color(0xFF6C5A92)
private val VioletContainerLight = Color(0xFFE7DEF9)
private val VioletOnLight = Color(0xFF20192D)

private val Ember = Color(0xFFF39C5A)
private val EmberContainer = Color(0xFF43240D)
private val EmberLight = Color(0xFFBE6C28)
private val EmberContainerLight = Color(0xFFFFE0C9)

private val DarkBackground = Color(0xFF0F1015)
private val DarkSurface = Color(0xFF151621)
private val DarkSurfaceVariant = Color(0xFF2C2D3A)
private val DarkOutline = Color(0xFF474957)
private val DarkOutlineVariant = Color(0xFF5F6170)

private val LightBackground = Color(0xFFFBF8F2)
private val LightSurface = Color(0xFFFFFBF5)
private val LightSurfaceVariant = Color(0xFFE5DECF)
private val LightOutline = Color(0xFF7A715D)
private val LightOutlineVariant = Color(0xFFAEA694)

internal val AppDarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = GoldOnDark,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = Color(0xFFF4E2B0),
    secondary = Violet,
    onSecondary = Color(0xFFF5F0FF),
    secondaryContainer = VioletContainerDark,
    onSecondaryContainer = Color(0xFFE5DAFF),
    tertiary = Ember,
    onTertiary = Color(0xFF2A1203),
    tertiaryContainer = EmberContainer,
    onTertiaryContainer = Color(0xFFFFE0C9),
    background = DarkBackground,
    onBackground = Color(0xFFE4E0F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE4E0F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD1C6E8),
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = Color(0xFF000000),
    surfaceBright = DarkSurface,
    surfaceDim = DarkBackground
)

internal val AppLightColorScheme = lightColorScheme(
    primary = DeepGold,
    onPrimary = Color.White,
    primaryContainer = GoldContainerLight,
    onPrimaryContainer = GoldOnLight,
    secondary = VioletLight,
    onSecondary = Color.White,
    secondaryContainer = VioletContainerLight,
    onSecondaryContainer = VioletOnLight,
    tertiary = EmberLight,
    onTertiary = Color.White,
    tertiaryContainer = EmberContainerLight,
    onTertiaryContainer = Color(0xFF2B1300),
    background = LightBackground,
    onBackground = Color(0xFF1E1A24),
    surface = LightSurface,
    onSurface = Color(0xFF1E1A24),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF453C2B),
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = Color(0xFF000000),
    surfaceBright = Color.White,
    surfaceDim = LightSurface
)
val Accent = Color(0xFFFFC107)

// --- Semantic icon colors ---

// Damage = warm ember / “burnt” orange
val DamageIconDark = Color(0xFFFFB677)   // lighter, glows nicely on DarkSurface
val DamageIconLight = EmberLight         // 0xFFBE6C28 – already in your palette

// Heal = cool emerald / herbal green
val HealIconDark = Color(0xFF78D9A3)     // soft, bright enough for DarkSurface
val HealIconLight = Color(0xFF1E8E5E)    // deeper green for LightSurface
