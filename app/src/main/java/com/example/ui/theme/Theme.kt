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
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

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

private val GoldenOasisColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF4D2C00),
    primaryContainer = Color(0xFF704300),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFFFFCC80),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF14110A),
    surface = Color(0xFF221D12),
    onBackground = Color(0xFFECE0D0),
    onSurface = Color(0xFFECE0D0),
    surfaceVariant = Color(0xFF3D3425),
    onSurfaceVariant = Color(0xFFD5C4AC),
    outline = Color(0xFF9E8E79),
    outlineVariant = Color(0xFF3D3425)
)

private val RoseQuartzColorScheme = darkColorScheme(
    primary = Color(0xFFF06292),
    onPrimary = Color(0xFF5C0028),
    primaryContainer = Color(0xFF880043),
    onPrimaryContainer = Color(0xFFFFD1DF),
    secondary = Color(0xFFF8BBD0),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF120E10),
    surface = Color(0xFF1E1519),
    onBackground = Color(0xFFECE0E4),
    onSurface = Color(0xFFECE0E4),
    surfaceVariant = Color(0xFF3C2E34),
    onSurfaceVariant = Color(0xFFD6C2C9),
    outline = Color(0xFF9E8D93),
    outlineVariant = Color(0xFF3C2E34)
)

private val AmberColorScheme = darkColorScheme(
    primary = Color(0xFFFFB300),
    onPrimary = Color(0xFF3E1F00),
    primaryContainer = Color(0xFF5D2F00),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFFFFC107),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF141008),
    surface = Color(0xFF201B10),
    onBackground = Color(0xFFEDE1CE),
    onSurface = Color(0xFFEDE1CE),
    surfaceVariant = Color(0xFF383120),
    onSurfaceVariant = Color(0xFFCEBFA6),
    outline = Color(0xFF9E8D72),
    outlineVariant = Color(0xFF383120)
)

private val AuroraColorScheme = darkColorScheme(
    primary = Color(0xFF1DE9B6),
    onPrimary = Color(0xFF00372A),
    primaryContainer = Color(0xFF00503E),
    onPrimaryContainer = Color(0xFFA7FFEB),
    secondary = Color(0xFF00E5FF),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF070F0D),
    surface = Color(0xFF0F1E19),
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1),
    surfaceVariant = Color(0xFF1B312B),
    onSurfaceVariant = Color(0xFFB2DFDB),
    outline = Color(0xFF80CBC4),
    outlineVariant = Color(0xFF1B312B)
)

@Composable
fun Modifier.animatedGradientBackground(enabled: Boolean): Modifier {
    if (!enabled) return this
    val infiniteTransition = rememberInfiniteTransition(label = "bgSweep")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    return this.drawBehind {
        val color1 = Color(0xFF050B09)
        val color2 = Color(0xFF08120F)
        val color3 = Color(0xFF0C1D18)
        val accent1 = Color(0xFF1DE9B6).copy(alpha = 0.14f * animProgress)
        val accent2 = Color(0xFF00E5FF).copy(alpha = 0.12f * (1f - animProgress))
        val accent3 = Color(0xFF00ACC1).copy(alpha = 0.08f * animProgress)

        // Draw deep base background
        drawRect(color = color1)
        
        // Draw slowly flowing organic moving gradient blobs
        drawCircle(
            color = accent1,
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.15f, size.height * (0.15f + 0.12f * animProgress))
        )
        drawCircle(
            color = accent2,
            radius = size.width * 1.1f,
            center = Offset(size.width * 0.85f, size.height * (0.85f - 0.12f * animProgress))
        )
        drawCircle(
            color = accent3,
            radius = size.width * 0.75f,
            center = Offset(size.width * 0.5f, size.height * (0.5f + 0.06f * animProgress))
        )
    }
}

@Composable
fun MyApplicationTheme(
  themeName: String = "royal_purple",
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeName) {
    "emerald_dusk" -> EmeraldColorScheme
    "midnight_sapphire" -> SapphireColorScheme
    "crimson_velvet" -> CrimsonColorScheme
    "golden_oasis" -> GoldenOasisColorScheme
    "rose_quartz" -> RoseQuartzColorScheme
    "amber_glow" -> AmberColorScheme
    "aurora_live" -> AuroraColorScheme
    else -> DarkColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
