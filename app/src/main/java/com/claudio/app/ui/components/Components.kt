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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Ink
import com.claudio.app.ui.theme.InkDeep
import com.claudio.app.ui.theme.Magenta
import com.claudio.app.ui.theme.Surface3
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.Violet

@Composable
fun AuroraBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val drift by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    Box(modifier.fillMaxSize().background(Ink)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Violet.copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(size.width * (0.15f + 0.35f * drift), size.height * 0.08f),
                    radius = size.width * 0.95f
                ),
                radius = size.width * 0.95f,
                center = Offset(size.width * (0.15f + 0.35f * drift), size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Magenta.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * (0.95f - 0.25f * drift), size.height * 0.85f),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * (0.95f - 0.25f * drift), size.height * 0.85f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Cyan.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * (0.25f + 0.15f * drift)),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.85f, size.height * (0.25f + 0.15f * drift))
            )
        }
        content()
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
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "press"
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) Brush.horizontalGradient(listOf(Violet, Magenta))
                else SolidColor(Surface3)
            )
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = if (enabled) TextPrimary else TextMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = if (enabled) TextPrimary else TextMuted)
        }
    }
}

@Composable
fun OwlLogo(size: Dp = 96.dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "owl")
    val blink by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4200
                1f at 0
                1f at 3800
                0.08f at 3950
                1f at 4100
            }
        ),
        label = "blink"
    )
    val bob by transition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    Canvas(modifier.size(size).graphicsLayer { translationY = bob }) {
        val s = this.size.width / 108f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)

        drawCircle(
            brush = Brush.radialGradient(listOf(Violet.copy(alpha = 0.35f), Color.Transparent), center = p(54f, 58f), radius = 52f * s),
            radius = 52f * s, center = p(54f, 58f)
        )
        drawOval(Violet, topLeft = p(32f, 30f), size = androidx.compose.ui.geometry.Size(44f * s, 50f * s))
        val leftEar = Path().apply { moveTo(36f * s, 38f * s); lineTo(40f * s, 25f * s); lineTo(47f * s, 34f * s); close() }
        val rightEar = Path().apply { moveTo(72f * s, 38f * s); lineTo(68f * s, 25f * s); lineTo(61f * s, 34f * s); close() }
        drawPath(leftEar, Violet)
        drawPath(rightEar, Violet)
        drawOval(Color(0xFFA78BFA), topLeft = p(38f, 48f), size = androidx.compose.ui.geometry.Size(32f * s, 28f * s))
        val eyeH = 18f * s * blink
        drawOval(TextPrimary, topLeft = Offset(36f * s, 52f * s - eyeH / 2), size = androidx.compose.ui.geometry.Size(18f * s, eyeH))
        drawOval(TextPrimary, topLeft = Offset(54f * s, 52f * s - eyeH / 2), size = androidx.compose.ui.geometry.Size(18f * s, eyeH))
        if (blink > 0.4f) {
            drawCircle(InkDeep, radius = 4.2f * s, center = p(45f, 52f))
            drawCircle(InkDeep, radius = 4.2f * s, center = p(63f, 52f))
            drawCircle(Color.White, radius = 1.4f * s, center = p(46.5f, 50.5f))
            drawCircle(Color.White, radius = 1.4f * s, center = p(64.5f, 50.5f))
        }
        val beak = Path().apply { moveTo(54f * s, 58f * s); lineTo(50f * s, 64f * s); lineTo(54f * s, 70f * s); lineTo(58f * s, 64f * s); close() }
        drawPath(beak, Amber)
        val cap = Path().apply { moveTo(54f * s, 17f * s); lineTo(79f * s, 26f * s); lineTo(54f * s, 35f * s); lineTo(29f * s, 26f * s); close() }
        drawPath(cap, InkDeep)
        drawLine(Amber, start = p(74f, 28f), end = p(74f, 40f), strokeWidth = 2f * s)
        drawCircle(Amber, radius = 2.6f * s, center = p(74f, 42f))
    }
}

@Composable
fun TypingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val bounce by transition.animateFloat(
                initialValue = 0f, targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    keyframes {
                        durationMillis = 900
                        0f at 0
                        -10f at 150 + i * 150
                        0f at 300 + i * 150
                        0f at 900
                    },
                    RepeatMode.Restart
                ),
                label = "dot$i"
            )
            Box(
                Modifier
                    .graphicsLayer { translationY = bounce }
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(listOf(Violet, Magenta, Cyan)[i])
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
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(dampingRatio = 0.6f, stiffness = 800f), label = "chip")
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(50))
            .background(if (selected) Brush.horizontalGradient(listOf(Violet, Magenta)) else SolidColor(Surface3))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (selected) TextPrimary else Cyan, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (selected) TextPrimary else TextPrimary)
    }
}