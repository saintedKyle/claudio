package com.claudio.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.ClaudioFieldColors
import com.claudio.app.ui.components.GradientButton
import com.claudio.app.ui.components.PillChip
import com.claudio.app.ui.components.StaggeredIn
import com.claudio.app.ui.components.TopBar
import com.claudio.app.ui.theme.LocalClaudioColors
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable

private val QUICK_PROMPTS = listOf(
    "Solve this",
    "Explain step by step",
    "Summarize this",
    "Translate to English",
    "Make flashcards from this",
    "What is this drawing?"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewScreen(
    vm: ClaudioViewModel,
    onAsk: (String) -> Unit,
    onRetake: () -> Unit,
    onBack: () -> Unit
) {
    val image by vm.pendingImage.collectAsState()
    val loading by vm.loadingImage.collectAsState()
    val colors = LocalClaudioColors.current
    var question by rememberSaveable { mutableStateOf("") }

    val transition = rememberInfiniteTransition(label = "scan")
    val scan by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "scanline")

    AuroraBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            TopBar(title = "New question", subtitle = "Claudio reads the photo, then answers", onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StaggeredIn(0, Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 360.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.surface1)
                            .border(1.dp, colors.outline, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (image != null) {
                            Image(
                                bitmap = image!!,
                                contentDescription = "Captured question",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)
                            )
                        } else if (loading) {
                            val scanTint = colors.accent
                            CircularProgressIndicator(color = colors.accent, strokeWidth = 3.dp)
                            Canvas(Modifier.fillMaxSize()) {
                                val y = size.height * scan
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color.Transparent, scanTint.copy(alpha = 0.35f), Color.Transparent),
                                        startY = y - 30f, endY = y + 30f
                                    ),
                                    topLeft = Offset(0f, y - 30f),
                                    size = androidx.compose.ui.geometry.Size(size.width, 60f)
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                Text("No photo yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Take a photo of your question to get started.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                StaggeredIn(1) {
                    Text(
                        "Retake photo",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onRetake)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))

                StaggeredIn(2, Modifier.fillMaxWidth()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QUICK_PROMPTS.forEach { prompt ->
                            PillChip(
                                text = prompt,
                                selected = question == prompt,
                                onClick = { question = if (question == prompt) "" else prompt }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                StaggeredIn(3, Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Ask anything about this photo…") },
                        placeholder = { Text("e.g. Solve question 3 and explain why", color = TextMuted) },
                        minLines = 2,
                        maxLines = 5,
                        colors = ClaudioFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Leave it blank and Claudio will figure out what you need.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(18.dp))
                StaggeredIn(4, Modifier.fillMaxWidth()) {
                    GradientButton(
                        text = "Ask Claudio",
                        icon = Icons.Filled.AutoAwesome,
                        enabled = image != null && !loading,
                        onClick = { onAsk(question) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}