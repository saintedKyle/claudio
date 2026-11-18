package com.claudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.claudio.app.ui.theme.LocalClaudioColors
import com.claudio.app.ui.theme.LocalClaudioPrefs

/**
 * Warm-black backdrop: a soft vertical ink wash, two barely-there ember glows
 * and a gentle vignette. Far calmer than the old neon aurora.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = LocalClaudioColors.current
    val reduceMotion = LocalClaudioPrefs.current.reduceMotion
    val transition = rememberInfiniteTransition(label = "backdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse),
        label = "backdropDrift"
    )
    val t = if (reduceMotion) 0.42f else drift

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.backgroundDeep)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val emberCenter = Offset(size.width * (0.82f - 0.10f * t), size.height * 0.05f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.accent.copy(alpha = 0.11f), Color.Transparent),
                    center = emberCenter,
                    radius = size.width * 0.95f
                ),
                radius = size.width * 0.95f,
                center = emberCenter
            )
            val goldCenter = Offset(size.width * (0.10f + 0.08f * t), size.height * 0.98f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.highlight.copy(alpha = 0.05f), Color.Transparent),
                    center = goldCenter,
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = goldCenter
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, colors.backgroundDeep.copy(alpha = 0.55f)),
                    center = center,
                    radius = size.maxDimension * 0.78f
                )
            )
        }
        content()
    }
}

/**
 * Claudio's brand mark: a slowly turning compass-spark with a quiet orbit ring.
 * Related to Claude's starburst in spirit, but a distinct shape and motion.
 */
@Composable
fun ClaudioMark(size: Dp = 72.dp, modifier: Modifier = Modifier, animated: Boolean = true) {
    val colors = LocalClaudioColors.current
    val reduceMotion = LocalClaudioPrefs.current.reduceMotion || !animated
    val transition = rememberInfiniteTransition(label = "mark")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(90000, easing = LinearEasing)),
        label = "markSpin"
    )
    val breath by transition.animateFloat(
        initialValue = 0.975f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "markBreath"
    )
    val scale = if (reduceMotion) 1f else breath

    Canvas(
        modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        val r = this.size.minDimension / 2f
        val c = center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.accent.copy(alpha = 0.16f), Color.Transparent),
                center = c,
                radius = r * 1.15f
            ),
            radius = r * 1.15f,
            center = c
        )
        rotate(degrees = if (reduceMotion) 0f else spin, pivot = c) {
            repeat(12) { i ->
                rotate(degrees = i * 30f, pivot = c) {
                    val longRay = i % 3 != 1
                    val outer = r * if (longRay) 0.94f else 0.74f
                    val inner = r * 0.30f
                    val rayColor = if (i % 4 == 0) colors.accentAlt else colors.accent
                    drawLine(
                        color = rayColor.copy(alpha = if (i % 2 == 0) 1f else 0.72f),
                        start = Offset(c.x, c.y - inner),
                        end = Offset(c.x, c.y - outer),
                        strokeWidth = r * if (longRay) 0.072f else 0.052f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        drawCircle(
            color = colors.accent.copy(alpha = 0.34f),
            radius = r * 0.46f,
            center = c,
            style = Stroke(width = r * 0.026f)
        )
        drawCircle(color = colors.background, radius = r * 0.15f, center = c)
        drawCircle(color = colors.textPrimary.copy(alpha = 0.92f), radius = r * 0.068f, center = c)
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = LocalClaudioColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 800f),
        label = "buttonPress"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    if (enabled) listOf(colors.accent, colors.accentDeep)
                    else listOf(colors.surface2, colors.surface2)
                )
            )
            .border(1.dp, if (enabled) Color.White.copy(alpha = 0.10f) else colors.outline, shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val contentColor = if (enabled) colors.backgroundDeep else colors.textMuted
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = contentColor)
        }
    }
}

@Composable
fun TypingDots(modifier: Modifier = Modifier) {
    val colors = LocalClaudioColors.current
    val reduceMotion = LocalClaudioPrefs.current.reduceMotion
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val bounce by transition.animateFloat(
                initialValue = 0f,
                targetValue = if (reduceMotion) 0f else -6f,
                animationSpec = infiniteRepeatable(
                    keyframes {
                        durationMillis = 900
                        0f at 0
                        -6f at 140 + i * 140
                        0f at 280 + i * 140
                        0f at 900
                    },
                    RepeatMode.Restart
                ),
                label = "dot$i"
            )
            Box(
                Modifier
                    .graphicsLayer { translationY = bounce }
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(listOf(colors.accent, colors.highlight, colors.info)[i])
            )
        }
    }
}

@Composable
fun PillChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: ImageVector? = null
) {
    val colors = LocalClaudioColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 900f),
        label = "chipPress"
    )
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(if (selected) colors.accent else colors.surface2)
            .border(1.dp, if (selected) Color.Transparent else colors.outline, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val content = if (selected) colors.backgroundDeep else colors.textPrimary
        if (icon != null) {
            Icon(icon, null, tint = if (selected) colors.backgroundDeep else colors.accent, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = content)
    }
}
