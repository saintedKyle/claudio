package com.claudio.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.data.ChatMessage
import com.claudio.app.data.Session
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.MarkdownText
import com.claudio.app.ui.components.OwlLogo
import com.claudio.app.ui.components.TopBar
import com.claudio.app.ui.components.TypingDots
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Danger
import com.claudio.app.ui.theme.Magenta
import com.claudio.app.ui.theme.Mint
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet
import com.claudio.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ChatScreen(vm: ClaudioViewModel, onBack: () -> Unit) {
    val session by vm.current.collectAsState()
    val streaming by vm.streaming.collectAsState()
    val error by vm.error.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

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
                        UserBubble(message)
                    } else {
                        AssistantBubble(
                            message = message,
                            isStreamingTail = streaming && message.id == current.messages.lastOrNull()?.id
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
                    .clip(RoundedCornerShape(28.dp))
                    .background(Surface1)
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
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (canSend) Brush.horizontalGradient(listOf(Violet, Magenta)) else Brush.horizontalGradient(listOf(Surface2, Surface2)))
                            .clickable(enabled = canSend) {
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
private fun UserBubble(message: ChatMessage) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
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
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        if (message.text.isNotBlank()) {
            Box(
                Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                    .background(Brush.horizontalGradient(listOf(Violet, Magenta)))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(message.text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun AssistantBubble(message: ChatMessage, isStreamingTail: Boolean) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        OwlLogo(size = 30.dp)
        Spacer(Modifier.width(10.dp))
        Column(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                .background(Surface1)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Claudio", style = MaterialTheme.typography.labelMedium, color = Amber)
                Spacer(Modifier.weight(1f))
                if (message.text.isNotBlank()) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        "Copy answer",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable {
                                clipboard.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "Answer copied", Toast.LENGTH_SHORT).show()
                            }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (message.text.isBlank() && isStreamingTail) {
                TypingDots(Modifier.padding(vertical = 6.dp))
            } else {
                MarkdownText(message.text)
                if (isStreamingTail) {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(10.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(Cyan))
                }
            }
        }
    }
}