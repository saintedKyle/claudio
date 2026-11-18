package com.claudio.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.data.ChatMessage
import com.claudio.app.data.Session
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.MarkdownText
import com.claudio.app.ui.components.OwlLogo
import com.claudio.app.ui.components.ThinkingPanel
import com.claudio.app.ui.components.TopBar
import com.claudio.app.ui.components.TypingDots
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Danger
import com.claudio.app.ui.theme.Magenta
import com.claudio.app.ui.theme.Mint
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.Surface3
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet
import com.claudio.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun ChatScreen(vm: ClaudioViewModel, onBack: () -> Unit) {
    val session by vm.current.collectAsState()
    val streaming by vm.streaming.collectAsState()
    val error by vm.error.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current

    val current = session
    if (current == null) {
        AuroraBackground {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                TopBar(title = "Answer", onBack = onBack)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This conversation is no longer available.", color = TextMuted)
                }
            }
        }
        return
    }

    val lastText = current.messages.lastOrNull()?.text ?: ""
    LaunchedEffect(current.messages.size, lastText) {
        if (current.messages.isNotEmpty()) {
            listState.scrollToItem(current.messages.size - 1, 1_000_000)
        }
    }

    AuroraBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            TopBar(
                title = current.title,
                subtitle = current.model.substringAfterLast('/'),
                onBack = onBack,
                trailing = {
                    if (streaming) {
                        Row(
                            Modifier.clip(RoundedCornerShape(50)).background(Surface2).padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TypingDots()
                            Spacer(Modifier.width(8.dp))
                            Text("thinking", style = MaterialTheme.typography.labelSmall, color = Cyan)
                        }
                    }
                }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(current.messages, key = { it.id }) { message ->
                    if (message.role == "user") {
                        UserBubble(message, Modifier.animateItem())
                    } else {
                        AssistantBubble(
                            message = message,
                            isStreamingTail = streaming && message.id == current.messages.lastOrNull()?.id,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            if (error != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF3B1220))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Couldn't get an answer", style = MaterialTheme.typography.labelMedium, color = Danger)
                        Text(error!!, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Danger)
                            .clickable { vm.retry() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Refresh, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .shadow(18.dp, RoundedCornerShape(28.dp), clip = false, ambientColor = Violet, spotColor = Magenta)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF251C47), Surface1)))
                    .border(1.dp, Brush.horizontalGradient(listOf(Violet.copy(alpha = 0.55f), Magenta.copy(alpha = 0.35f), Cyan.copy(alpha = 0.4f))), RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask a follow-up…", color = TextMuted) },
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Magenta,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (streaming) {
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Danger)
                            .clickable { vm.stopStreaming() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Stop, "Stop", tint = Color.White)
                    }
                } else {
                    val canSend = input.isNotBlank()
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (pressed) 0.88f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
                        label = "send"
                    )
                    Box(
                        Modifier
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (canSend) Brush.horizontalGradient(listOf(Violet, Magenta)) else Brush.horizontalGradient(listOf(Surface2, Surface2)))
                            .clickable(interactionSource = interaction, indication = null, enabled = canSend) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.followUp(input)
                                input = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = if (canSend) TextPrimary else TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun UserBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        if (message.imageBase64 != null) {
            val bitmap by produceState<ImageBitmap?>(null, message.imageBase64) {
                value = withContext(Dispatchers.IO) { ImageUtils.fromBase64(message.imageBase64) }
            }
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Your photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, Brush.linearGradient(listOf(Violet, Magenta, Cyan)), RoundedCornerShape(20.dp))
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        if (message.text.isNotBlank()) {
            Box(
                Modifier
                    .widthIn(max = 300.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp), clip = false, ambientColor = Violet, spotColor = Magenta)
                    .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                    .background(Brush.horizontalGradient(listOf(Violet, Magenta)))
                    .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(message.text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun AssistantBubble(message: ChatMessage, isStreamingTail: Boolean, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }
    val caret = rememberInfiniteTransition(label = "caret")
    val caretAlpha by caret.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "caretAlpha"
    )
    val cardShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        OwlLogo(size = 30.dp)
        Spacer(Modifier.width(10.dp))
        Column(
            Modifier
                .weight(1f)
                .shadow(16.dp, cardShape, clip = false, ambientColor = Violet, spotColor = Magenta)
                .clip(cardShape)
                .background(Brush.verticalGradient(listOf(Color(0xFF261D49), Color(0xFF1B1438))))
                .border(1.dp, Brush.linearGradient(listOf(Violet.copy(alpha = 0.6f), Magenta.copy(alpha = 0.28f), Cyan.copy(alpha = 0.42f))), cardShape)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Claudio", style = MaterialTheme.typography.labelMedium, color = Amber)
                Spacer(Modifier.weight(1f))
                if (message.text.isNotBlank()) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (copied) Mint.copy(alpha = 0.16f) else Surface3)
                            .clickable {
                                clipboard.setText(AnnotatedString(message.text))
                                copied = true
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                            "Copy answer",
                            tint = if (copied) Mint else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (copied) "Copied" else "Copy",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (copied) Mint else TextSecondary
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            if (message.reasoning != null) {
                ThinkingPanel(message, live = isStreamingTail && message.reasoningMs == null)
                Spacer(Modifier.height(10.dp))
            }
            if (message.text.isBlank() && isStreamingTail) {
                TypingDots(Modifier.padding(vertical = 6.dp))
            } else {
                MarkdownText(message.text, live = isStreamingTail)
                if (isStreamingTail) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .width(10.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Cyan.copy(alpha = caretAlpha))
                    )
                }
            }
        }
    }
}