package com.claudio.app.ui.screens

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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.data.BubbleStyle
import com.claudio.app.data.ChatMessage
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.ClaudioMark
import com.claudio.app.ui.components.IconBubble
import com.claudio.app.ui.components.MarkdownText
import com.claudio.app.ui.components.ThinkingPanel
import com.claudio.app.ui.components.TopBar
import com.claudio.app.ui.components.TypingDots
import com.claudio.app.ui.theme.LocalClaudioColors
import com.claudio.app.ui.theme.LocalClaudioPrefs
import com.claudio.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(vm: ClaudioViewModel, onBack: () -> Unit, onNew: () -> Unit) {
    val session by vm.current.collectAsState()
    val streaming by vm.streaming.collectAsState()
    val error by vm.error.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val colors = LocalClaudioColors.current

    val current = session
    if (current == null) {
        AuroraBackground {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                TopBar(title = "Answer", onBack = onBack)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This conversation is no longer available.", color = colors.textMuted)
                }
            }
        }
        return
    }

    val messages = current.messages
    val lastText = messages.lastOrNull()?.text ?: ""
    val lastReasoning = messages.lastOrNull()?.reasoning ?: ""
    var stickToBottom by remember(current.id) { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to listState.canScrollForward }
            .collect { (scrolling, canScrollForward) ->
                if (scrolling && canScrollForward) stickToBottom = false
                if (!scrolling && !canScrollForward) stickToBottom = true
            }
    }

    LaunchedEffect(current.id) {
        if (messages.isNotEmpty()) listState.scrollToItem(max(0, messages.lastIndex), Int.MAX_VALUE)
    }

    // Streamed text grows in place without re-running item placement animations.
    LaunchedEffect(messages.size, lastText.length, lastReasoning.length) {
        if (messages.isNotEmpty() && stickToBottom) {
            listState.scrollToItem(messages.lastIndex, Int.MAX_VALUE)
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
                    IconBubble(
                        icon = Icons.Filled.Add,
                        onClick = onNew,
                        contentDescription = "New question"
                    )
                }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(messages, key = { it.id }, contentType = { it.role }) { message ->
                    val isTail = streaming && message.id == messages.lastOrNull()?.id
                    if (message.role == "user") {
                        UserMessage(
                            message = message,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(220),
                                placementSpec = null,
                                fadeOutSpec = tween(140)
                            )
                        )
                    } else {
                        // Assistant text is deliberately not item-animated: animating
                        // a growing streaming item makes it pulse/flicker per token.
                        AssistantMessage(
                            message = message,
                            isStreamingTail = isTail
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val message = error
                if (message != null) {
                    ErrorCard(message = message, onRetry = vm::retry)
                }
            }

            ChatComposer(
                value = input,
                onValueChange = { input = it },
                streaming = streaming,
                model = current.model,
                onStop = vm::stopStreaming,
                onSend = {
                    val text = input.trim()
                    if (text.isNotEmpty() && !streaming) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.followUp(text)
                        input = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun UserMessage(message: ChatMessage, modifier: Modifier = Modifier) {
    val colors = LocalClaudioColors.current
    val prefs = LocalClaudioPrefs.current
    val shape = RoundedCornerShape(22.dp, 22.dp, 6.dp, 22.dp)

    val bubbleColor: Color
    val bubbleBorder: Color
    when (prefs.bubble) {
        BubbleStyle.Soft -> {
            bubbleColor = colors.surface2
            bubbleBorder = colors.outline
        }
        BubbleStyle.Tint -> {
            bubbleColor = colors.accent.copy(alpha = 0.16f)
            bubbleBorder = colors.accent.copy(alpha = 0.34f)
        }
        BubbleStyle.Outline -> {
            bubbleColor = Color.Transparent
            bubbleBorder = colors.outline
        }
    }

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
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, colors.outline, RoundedCornerShape(18.dp))
                )
                Spacer(Modifier.height(7.dp))
            }
        }
        if (message.text.isNotBlank()) {
            Box(
                Modifier
                    .widthIn(max = 300.dp)
                    .clip(shape)
                    .background(bubbleColor)
                    .border(1.dp, bubbleBorder, shape)
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            ) {
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun AssistantMessage(
    message: ChatMessage,
    isStreamingTail: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClaudioColors.current
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ClaudioMark(size = 20.dp, animated = false)
            Spacer(Modifier.width(7.dp))
            Text("Claudio", style = MaterialTheme.typography.labelMedium, color = colors.accent)
            if (isStreamingTail) {
                Spacer(Modifier.width(7.dp))
                StreamingDot()
            }
            Spacer(Modifier.weight(1f))
            if (message.text.isNotBlank()) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.surface2)
                        .clickable {
                            clipboard.setText(AnnotatedString(message.text))
                            copied = true
                        }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                        contentDescription = "Copy answer",
                        tint = if (copied) colors.success else colors.textMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        if (copied) "Copied" else "Copy",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (copied) colors.success else colors.textMuted
                    )
                }
            }
        }
        Spacer(Modifier.height(9.dp))
        if (message.reasoning != null) {
            ThinkingPanel(message, live = isStreamingTail && message.reasoningMs == null)
            Spacer(Modifier.height(11.dp))
        }
        if (message.text.isBlank() && isStreamingTail) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypingDots()
                Spacer(Modifier.width(9.dp))
                Text("Reading your question", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
            }
        } else {
            MarkdownText(message.text, live = isStreamingTail)
        }
    }
}

@Composable
private fun StreamingDot() {
    val colors = LocalClaudioColors.current
    val transition = rememberInfiniteTransition(label = "streamDot")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(720), RepeatMode.Reverse),
        label = "streamDotAlpha"
    )
    Box(
        Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(colors.accent.copy(alpha = alpha))
    )
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    val colors = LocalClaudioColors.current
    val shape = RoundedCornerShape(18)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(shape)
            .background(colors.danger.copy(alpha = 0.12f))
            .border(1.dp, colors.danger.copy(alpha = 0.32f), shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Couldn't get an answer",
                style = MaterialTheme.typography.labelMedium,
                color = colors.danger
            )
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Spacer(Modifier.width(10.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.surface2)
                .border(1.dp, colors.outline, RoundedCornerShape(50))
                .clickable(onClick = onRetry)
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Retry", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
        }
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    streaming: Boolean,
    model: String,
    onStop: () -> Unit,
    onSend: () -> Unit
) {
    val colors = LocalClaudioColors.current
    val shape = RoundedCornerShape(26)
    val modelLabel = model.substringAfterLast('/')

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface1)
                .border(1.dp, colors.outline, shape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accent),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                decorationBox = { inner ->
                    Box(Modifier.padding(horizontal = 9.dp, vertical = 10.dp)) {
                        if (value.isBlank()) {
                            Text(
                                "Reply to Claudio…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textMuted
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            if (streaming) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .border(1.dp, colors.outline, CircleShape)
                        .clickable(onClick = onStop),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "Stop",
                        tint = colors.danger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                val canSend = value.isNotBlank()
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (canSend) colors.accent else colors.surface2)
                        .clickable(enabled = canSend, onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) colors.backgroundDeep else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modelLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (streaming) {
                Text("Writing…", style = MaterialTheme.typography.labelSmall, color = colors.accent)
            }
        }
    }
}
