package com.claudio.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.data.Session
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.ClaudioMark
import com.claudio.app.ui.components.IconBubble
import com.claudio.app.ui.components.StaggeredIn
import com.claudio.app.ui.components.timeAgo
import com.claudio.app.ui.theme.LocalClaudioColors
import java.util.Calendar
import java.util.Locale

private val SUGGESTIONS = listOf(
    "Explain photosynthesis simply",
    "Solve 2x + 5 = 17 step by step",
    "Quiz me on the French Revolution",
    "Summarize a chapter for me"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    vm: ClaudioViewModel,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onAsk: (String) -> Unit,
    onOpenSession: (String) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    val settings by vm.settings.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val colors = LocalClaudioColors.current
    var input by rememberSaveable { mutableStateOf("") }

    val send = {
        val text = input.trim()
        if (text.isNotEmpty()) {
            input = ""
            onAsk(text)
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(Icons.Filled.Menu, onHistory, contentDescription = "History")
                Spacer(Modifier.weight(1f))
                Text(
                    "Claudio",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontSize = 19.sp,
                    letterSpacing = (-0.2).sp,
                    color = colors.textSecondary
                )
                Spacer(Modifier.weight(1f))
                IconBubble(Icons.Filled.Tune, onSettings, contentDescription = "Settings")
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { Spacer(Modifier.height(26.dp)) }

                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        StaggeredIn(0) { ClaudioMark(size = 60.dp) }
                        Spacer(Modifier.height(18.dp))
                        StaggeredIn(1) {
                            Text(
                                "${greeting()}, ${settings.theme.name.ifBlank { "saint" }}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                        StaggeredIn(2) {
                            Text(
                                "What are we learning today?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        StaggeredIn(3) {
                            PlanPill(
                                usingFree = settings.usingFreeKey,
                                onClick = onSettings
                            )
                        }
                    }
                }

                item {
                    StaggeredIn(4) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickAction(
                                icon = Icons.Filled.PhotoCamera,
                                title = "Snap a question",
                                accent = true,
                                onClick = onCamera,
                                modifier = Modifier.weight(1f)
                            )
                            QuickAction(
                                icon = Icons.Filled.Image,
                                title = "Photo library",
                                accent = false,
                                onClick = onGallery,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    StaggeredIn(5) {
                        Column {
                            Text(
                                "Try asking",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted,
                                modifier = Modifier.padding(start = 2.dp, bottom = 9.dp)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SUGGESTIONS.forEach { prompt ->
                                    SuggestionChip(prompt) { onAsk(prompt) }
                                }
                            }
                        }
                    }
                }

                if (sessions.isNotEmpty()) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "See all",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.accent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onHistory)
                                    .padding(6.dp)
                            )
                        }
                    }
                    items(sessions.take(3), key = { it.id }) { session ->
                        RecentRow(session = session, onClick = { onOpenSession(session.id) })
                    }
                }

                item { Spacer(Modifier.height(6.dp)) }
            }

            HomeComposer(
                value = input,
                onValueChange = { input = it },
                onSend = send,
                onGallery = onGallery,
                onCamera = onCamera,
                model = settings.model,
                usingFree = settings.usingFreeKey,
                webSearch = settings.webSearch,
                onToggleWebSearch = { vm.setWebSearch(!settings.webSearch) }
            )
            Column(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Claudio can make mistakes — double-check important answers.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Made by kyle",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun PlanPill(usingFree: Boolean, onClick: () -> Unit) {
    val colors = LocalClaudioColors.current
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(shape)
            .background(colors.surface1)
            .border(1.dp, colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(colors.accent))
        Spacer(Modifier.width(7.dp))
        Text(
            if (usingFree) "Free plan" else "Your key",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Text("  ·  ", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
        Text("Personalize", style = MaterialTheme.typography.labelMedium, color = colors.accent)
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    title: String,
    accent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClaudioColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 900f),
        label = "quickPress"
    )
    val shape = RoundedCornerShape(18)
    Row(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(if (accent) colors.accent.copy(alpha = 0.13f) else colors.surface1)
            .border(1.dp, if (accent) colors.accent.copy(alpha = 0.30f) else colors.outline, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (accent) colors.accent else colors.surface2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (accent) colors.backgroundDeep else colors.textSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.width(9.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    val colors = LocalClaudioColors.current
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(colors.surface1)
            .border(1.dp, colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 9.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
    }
}

@Composable
private fun RecentRow(session: Session, onClick: () -> Unit) {
    val colors = LocalClaudioColors.current
    val shape = RoundedCornerShape(18)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface1)
            .border(1.dp, colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClaudioMark(size = 24.dp, animated = false)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                session.title,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                timeAgo(session.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
        Icon(
            Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HomeComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    model: String,
    usingFree: Boolean,
    webSearch: Boolean,
    onToggleWebSearch: () -> Unit
) {
    val colors = LocalClaudioColors.current
    val shape = RoundedCornerShape(26)
    val modelLabel = model.substringAfterLast('/').substringBefore(':')
        .replace('-', ' ')
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface1)
                .border(1.dp, colors.outline, shape)
                .padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .border(1.dp, colors.outline, CircleShape)
                        .clickable(onClick = onGallery),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add a photo",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    decorationBox = { inner ->
                        Box(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                            if (value.isBlank()) {
                                Text(
                                    "Message Claudio",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.textMuted
                                )
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                if (value.isNotBlank()) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickable(onClick = onSend),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = "Send",
                            tint = colors.backgroundDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickable(onClick = onCamera),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = "Take a photo",
                            tint = colors.backgroundDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 6.dp, end = 4.dp, top = 5.dp, bottom = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.surface2)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(colors.accent))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        modelLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (webSearch) colors.accent.copy(alpha = 0.16f) else colors.surface2)
                        .border(
                            1.dp,
                            if (webSearch) colors.accent.copy(alpha = 0.35f) else colors.outline,
                            RoundedCornerShape(50)
                        )
                        .clickable(onClick = onToggleWebSearch)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = "Toggle web search",
                        tint = if (webSearch) colors.accent else colors.textMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (webSearch) "Web on" else "Web",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (webSearch) colors.accent else colors.textMuted
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (usingFree) "Free" else "BYOK",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent
                )
            }
        }
    }
}
