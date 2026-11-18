package com.claudio.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.claudio.app.data.AccentTone
import com.claudio.app.data.SurfaceTone

/**
 * Claudio's semantic color set. The whole palette is data so themes can be
 * swapped (and animated) at runtime from Settings.
 */
data class ClaudioColors(
    val background: Color,
    val backgroundDeep: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val accent: Color,
    val accentDeep: Color,
    val accentAlt: Color,
    val highlight: Color,
    val info: Color,
    val success: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val outline: Color
)

private data class ToneSet(
    val background: Color,
    val backgroundDeep: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

private data class AccentSet(
    val accent: Color,
    val deep: Color,
    val alt: Color
)

private val EspressoTone = ToneSet(
    background = Color(0xFF171310),
    backgroundDeep = Color(0xFF0F0C0A),
    surface1 = Color(0xFF211B16),
    surface2 = Color(0xFF2A231D),
    surface3 = Color(0xFF342C24),
    outline = Color(0xFF3D332A),
    textPrimary = Color(0xFFF4EFE6),
    textSecondary = Color(0xFFB7AD9F),
    textMuted = Color(0xFF857B6E)
)

private val GraphiteTone = ToneSet(
    background = Color(0xFF17171A),
    backgroundDeep = Color(0xFF0F0F11),
    surface1 = Color(0xFF202024),
    surface2 = Color(0xFF28282D),
    surface3 = Color(0xFF323238),
    outline = Color(0xFF3C3C43),
    textPrimary = Color(0xFFF4EFE6),
    textSecondary = Color(0xFFB7B0A6),
    textMuted = Color(0xFF857F76)
)

private val MidnightTone = ToneSet(
    background = Color(0xFF11151C),
    backgroundDeep = Color(0xFF0A0D12),
    surface1 = Color(0xFF181E27),
    surface2 = Color(0xFF202733),
    surface3 = Color(0xFF293240),
    outline = Color(0xFF34404F),
    textPrimary = Color(0xFFF1EEE8),
    textSecondary = Color(0xFFADB4BE),
    textMuted = Color(0xFF7D8693)
)

private val AmoledTone = ToneSet(
    background = Color(0xFF050505),
    backgroundDeep = Color(0xFF000000),
    surface1 = Color(0xFF0E0E0E),
    surface2 = Color(0xFF161616),
    surface3 = Color(0xFF1F1F1F),
    outline = Color(0xFF2C2C2C),
    textPrimary = Color(0xFFF4EFE6),
    textSecondary = Color(0xFFB7AD9F),
    textMuted = Color(0xFF80776B)
)

private val EmberAccent = AccentSet(Color(0xFFD97757), Color(0xFFB85B3E), Color(0xFFE9A07F))
private val AmberAccent = AccentSet(Color(0xFFDDA35C), Color(0xFFB9853F), Color(0xFFEBC38D))
private val SageAccent = AccentSet(Color(0xFF93AE8F), Color(0xFF759274), Color(0xFFB8CCB2))
private val OceanAccent = AccentSet(Color(0xFF7FA5BD), Color(0xFF6489A2), Color(0xFFA9C4D6))
private val IrisAccent = AccentSet(Color(0xFFA08FCB), Color(0xFF806CB0), Color(0xFFC0B3E0))
private val RoseAccent = AccentSet(Color(0xFFCF8797), Color(0xFFAE6B7B), Color(0xFFE3AEBA))
private val MonoAccent = AccentSet(Color(0xFFD6CEC0), Color(0xFFB4AB9C), Color(0xFFE8E1D5))

private fun accentSet(tone: AccentTone): AccentSet = when (tone) {
    AccentTone.Ember -> EmberAccent
    AccentTone.Amber -> AmberAccent
    AccentTone.Sage -> SageAccent
    AccentTone.Ocean -> OceanAccent
    AccentTone.Iris -> IrisAccent
    AccentTone.Rose -> RoseAccent
    AccentTone.Mono -> MonoAccent
}

private fun toneSet(tone: SurfaceTone): ToneSet = when (tone) {
    SurfaceTone.Espresso -> EspressoTone
    SurfaceTone.Graphite -> GraphiteTone
    SurfaceTone.Midnight -> MidnightTone
    SurfaceTone.Amoled -> AmoledTone
}

/** Public previews for the theme picker. */
fun accentColor(tone: AccentTone): Color = accentSet(tone).accent
fun accentSoftColor(tone: AccentTone): Color = accentSet(tone).alt
fun tonePreview(tone: SurfaceTone): Pair<Color, Color> = toneSet(tone).let { it.background to it.surface2 }

fun paletteFor(accent: AccentTone, tone: SurfaceTone): ClaudioColors {
    val t = toneSet(tone)
    val a = accentSet(accent)
    return ClaudioColors(
        background = t.background,
        backgroundDeep = t.backgroundDeep,
        surface1 = t.surface1,
        surface2 = t.surface2,
        surface3 = t.surface3,
        accent = a.accent,
        accentDeep = a.deep,
        accentAlt = a.alt,
        highlight = Color(0xFFD9A55E),
        info = Color(0xFF8FAAB8),
        success = Color(0xFF94B98F),
        danger = Color(0xFFDF8774),
        textPrimary = t.textPrimary,
        textSecondary = t.textSecondary,
        textMuted = t.textMuted,
        outline = t.outline
    )
}

val WarmEmber: ClaudioColors = paletteFor(AccentTone.Ember, SurfaceTone.Espresso)

val LocalClaudioColors = staticCompositionLocalOf { WarmEmber }

/*
 * Backwards-compatible semantic tokens. These are composable getters, so every
 * existing screen automatically follows the selected theme (and animates when
 * the theme changes).
 */
val Ink: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.background
val InkDeep: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.backgroundDeep
val Surface1: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.surface1
val Surface2: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.surface2
val Surface3: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.surface3
val Violet: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.accent
val VioletDeep: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.accentDeep
val Magenta: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.accentAlt
val Amber: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.highlight
val Cyan: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.info
val Mint: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.success
val Danger: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.danger
val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.textPrimary
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.textSecondary
val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.textMuted
val OutlineSoft: Color @Composable @ReadOnlyComposable get() = LocalClaudioColors.current.outline
