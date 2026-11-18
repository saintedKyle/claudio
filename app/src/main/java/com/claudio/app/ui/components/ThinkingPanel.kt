package com.claudio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.data.ChatMessage
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Mint
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet
import kotlinx.coroutines.delay

@Composable
fun ThinkingPanel(message: ChatMessage, live: Boolean) {
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
        0.45f, 1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "glow"
    )

    val scroll = rememberScrollState()
    LaunchedEffect(message.reasoning) { scroll.scrollTo(scroll.maxValue) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface2)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Psychology,
                null,
                tint = if (live) Violet else Mint,
                modifier = Modifier
                    .size(17.dp)
                    .graphicsLayer { alpha = if (live) glow else 1f }
            )
            Spacer(Modifier.width(9.dp))
            Text(
                if (live) "Thinking" else "Thought for ${(message.reasoningMs ?: 0) / 1000}s",
                style = MaterialTheme.typography.labelMedium,
                color = if (live) Violet else Mint,
                modifier = Modifier.weight(1f)
            )
            if (live) {
                Text(
                    "${seconds}s",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                Icons.Filled.ExpandMore,
                null,
                tint = TextMuted,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 170.dp)
                    .verticalScroll(scroll)
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Violet.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        (message.reasoning ?: "") + if (live) " ▍" else "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}