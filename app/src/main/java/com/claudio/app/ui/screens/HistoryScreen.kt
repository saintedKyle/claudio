package com.claudio.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.data.Session
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.GradientButton
import com.claudio.app.ui.components.OwlLogo
import com.claudio.app.ui.components.TopBar
import com.claudio.app.ui.components.previewText
import com.claudio.app.ui.components.timeAgo
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Danger
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet

@Composable
fun HistoryScreen(
    vm: ClaudioViewModel,
    onOpen: (String) -> Unit,
    onBack: () -> Unit
) {
    val sessions by vm.sessions.collectAsState()
    var toDelete by remember { mutableStateOf<Session?>(null) }

    AuroraBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopBar(
                title = "History",
                subtitle = "${sessions.size} saved conversation${if (sessions.size == 1) "" else "s"} · stored on this device",
                onBack = onBack
            )
            if (sessions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        OwlLogo(size = 100.dp)
                        Spacer(Modifier.height(16.dp))
                        Text("No questions yet", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Snap your first question and it will show up here, saved privately on your phone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(20.dp))
                        GradientButton(text = "Ask your first question", onClick = onBack)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sessions, key = { it.id }) { session ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Surface1)
                                .clickable { onOpen(session.id) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    session.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                val preview = session.messages.lastOrNull { it.role == "assistant" }?.text
                                    ?: session.messages.firstOrNull()?.text ?: ""
                                Text(
                                    previewText(preview),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Surface2)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            session.model.substringAfterLast('/'),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Cyan
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(timeAgo(session.updatedAt), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.DeleteOutline,
                                "Delete",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { toDelete = session }
                            )
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = Surface2,
            title = { Text("Delete this conversation?", color = TextPrimary) },
            text = { Text("\"${session.title}\" will be removed from this device. This can't be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { vm.deleteSession(session.id); toDelete = null }) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) {
                    Text("Keep", color = Violet)
                }
            }
        )
    }
}