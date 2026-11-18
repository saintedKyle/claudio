package com.claudio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.data.ChatMessage
import com.claudio.app.ui.theme.LocalClaudioColors
import com.claudio.app.ui.theme.LocalClaudioPrefs
import kotlinx.coroutines.delay

@Composable
fun ThinkingPanel(message: ChatMessage, live: Boolean) {
    val colors = LocalClaudioColors.current
    val prefs = LocalClaudioPrefs.current
    var expanded by remember(message.id) { mutableStateOf(message.reasoningMs == null) }
    LaunchedEffect(message.reasoningMs) {
        if (message.reasoningMs != null) expanded = false
    }

    var seconds by remember { mutableStateOf(0) }
    LaunchedEffect(live) {
        if (live) {
            val start = System.currentTimeMillis()
            while (true) {
                seconds = ((System.currentTimeMillis() - start) / 1000).toInt()
                delay(1000)
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "think")
    val glow by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "thinkGlow"
    )
    val iconAlpha = if (live && !prefs.reduceMotion) glow else 1f
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "thinkArrow"
    )

    val scroll = rememberScrollState()
    LaunchedEffect(message.reasoning) {
        if (scroll.maxValue > 0) scroll.scrollTo(scroll.maxValue)
    }

    val shape = RoundedCornerShape(16)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface1)
            .border(1.dp, colors.outline, shape)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = (if (live) colors.accent else colors.success).copy(alpha = iconAlpha),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (live) "Thinking" else "Thought for ${(message.reasoningMs ?: 0) / 1000}s",
                style = MaterialTheme.typography.labelMedium,
                color = if (live) colors.accent else colors.success,
                modifier = Modifier.weight(1f)
            )
            if (live) {
                Text(
                    "${seconds}s",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = colors.textMuted,
                modifier = Modifier.size(17.dp).rotate(arrowRotation)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(180)) + expandVertically(tween(240)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(200))
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 190.dp)
                    .verticalScroll(scroll)
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(colors.accent.copy(alpha = 0.45f))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        (message.reasoning ?: "") + if (live) " ▍" else "",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
