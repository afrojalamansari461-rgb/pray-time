package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = ElegantBg,
    surface = ElegantSurface,
    onBackground = ElegantText,
    onSurface = ElegantText,
    surfaceVariant = ElegantSurfaceVariant,
    onSurfaceVariant = ElegantTextMuted,
    outline = ElegantOutline,
    outlineVariant = ElegantOutlineVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = ElegantBg,
    surface = ElegantSurface,
    onBackground = ElegantText,
    onSurface = ElegantText,
    surfaceVariant = ElegantSurfaceVariant,
    onSurfaceVariant = ElegantTextMuted,
    outline = ElegantOutline,
    outlineVariant = ElegantOutlineVariant
  )

private val EmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF33C08E),
    onPrimary = Color(0xFF003D26),
    primaryContainer = Color(0xFF005134),
    onPrimaryContainer = Color(0xFF9CF4CB),
    secondary = Color(0xFFE5C158),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF0C100E),
    surface = Color(0xFF161F1A),
    onBackground = Color(0xFFE0ECE6),
    onSurface = Color(0xFFE0ECE6),
    surfaceVariant = Color(0xFF2E3B34),
    onSurfaceVariant = Color(0xFFBACCC2),
    outline = Color(0xFF8A9B93),
    outlineVariant = Color(0xFF2E3B34)
)

private val SapphireColorScheme = darkColorScheme(
    primary = Color(0xFF53C0FC),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004C70),
    onPrimaryContainer = Color(0xFFC5E7FF),
    secondary = Color(0xFF7CC4F8),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF0A0F14),
    surface = Color(0xFF131B24),
    onBackground = Color(0xFFDEE5ED),
    onSurface = Color(0xFFDEE5ED),
    surfaceVariant = Color(0xFF2B3746),
    onSurfaceVariant = Color(0xFFBACAD9),
    outline = Color(0xFF8697A8),
    outlineVariant = Color(0xFF2B3746)
)

private val CrimsonColorScheme = darkColorScheme(
    primary = Color(0xFFE57373),
    onPrimary = Color(0xFF5F0000),
    primaryContainer = Color(0xFF8C1D1D),
    onPrimaryContainer = Color(0xFFFFDAD7),
    secondary = Color(0xFFFFB4AB),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF120E0E),
    surface = Color(0xFF1E1515),
    onBackground = Color(0xFFECE0E0),
    onSurface = Color(0xFFECE0E0),
    surfaceVariant = Color(0xFF3B2E2E),
    onSurfaceVariant = Color(0xFFD8C2C2),
    outline = Color(0xFF9F8D8D),
    outlineVariant = Color(0xFF3B2E2E)
)

@Composable
fun MyApplicationTheme(
  themeName: String = "royal_purple",
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeName) {
    "emerald_dusk" -> EmeraldColorScheme
    "midnight_sapphire" -> SapphireColorScheme
    "crimson_velvet" -> CrimsonColorScheme
    else -> DarkColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
