package com.claudio.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.data.ThemePrefs

val LocalClaudioPrefs = staticCompositionLocalOf { ThemePrefs() }

private fun claudioScheme(c: ClaudioColors) = darkColorScheme(
    primary = c.accent,
    onPrimary = c.backgroundDeep,
    primaryContainer = c.accentDeep,
    onPrimaryContainer = c.textPrimary,
    secondary = c.highlight,
    onSecondary = c.backgroundDeep,
    tertiary = c.info,
    onTertiary = c.backgroundDeep,
    background = c.background,
    onBackground = c.textPrimary,
    surface = c.surface1,
    onSurface = c.textPrimary,
    surfaceVariant = c.surface2,
    onSurfaceVariant = c.textSecondary,
    outline = c.outline,
    outlineVariant = c.surface3,
    error = c.danger,
    onError = c.backgroundDeep,
    errorContainer = Color(0xFF3B1F1A),
    onErrorContainer = c.danger,
    scrim = c.backgroundDeep
)

private fun claudioTypography(scale: Float): Typography {
    fun size(value: Int) = (value * scale).sp
    fun line(value: Int) = (value * scale).sp
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = size(34),
            lineHeight = line(41),
            letterSpacing = (-0.4).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = size(27),
            lineHeight = line(34),
            letterSpacing = (-0.3).sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = size(20),
            lineHeight = line(26)
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = size(16),
            lineHeight = line(22)
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = size(16),
            lineHeight = line(25)
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = size(14),
            lineHeight = line(21)
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = size(12),
            lineHeight = line(18)
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = size(15),
            letterSpacing = 0.2.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = size(12),
            letterSpacing = 0.3.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = size(11),
            letterSpacing = 0.3.sp
        )
    )
}

private val ClaudioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
private fun animatedColor(target: Color, label: String): Color =
    animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing),
        label = label
    ).value

@Composable
private fun animatePalette(target: ClaudioColors): ClaudioColors = ClaudioColors(
    background = animatedColor(target.background, "background"),
    backgroundDeep = animatedColor(target.backgroundDeep, "backgroundDeep"),
    surface1 = animatedColor(target.surface1, "surface1"),
    surface2 = animatedColor(target.surface2, "surface2"),
    surface3 = animatedColor(target.surface3, "surface3"),
    accent = animatedColor(target.accent, "accent"),
    accentDeep = animatedColor(target.accentDeep, "accentDeep"),
    accentAlt = animatedColor(target.accentAlt, "accentAlt"),
    highlight = animatedColor(target.highlight, "highlight"),
    info = animatedColor(target.info, "info"),
    success = animatedColor(target.success, "success"),
    danger = animatedColor(target.danger, "danger"),
    textPrimary = animatedColor(target.textPrimary, "textPrimary"),
    textSecondary = animatedColor(target.textSecondary, "textSecondary"),
    textMuted = animatedColor(target.textMuted, "textMuted"),
    outline = animatedColor(target.outline, "outline")
)

@Composable
fun ClaudioTheme(prefs: ThemePrefs = ThemePrefs(), content: @Composable () -> Unit) {
    val colors = animatePalette(paletteFor(prefs.accent, prefs.tone))
    CompositionLocalProvider(
        LocalClaudioColors provides colors,
        LocalClaudioPrefs provides prefs
    ) {
        MaterialTheme(
            colorScheme = claudioScheme(colors),
            typography = claudioTypography(prefs.fontScale),
            shapes = ClaudioShapes,
            content = content
        )
    }
}
