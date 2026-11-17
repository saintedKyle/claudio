package com.claudio.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.IconBubble
import com.claudio.app.ui.components.OwlLogo
import com.claudio.app.ui.components.StaggeredIn
import com.claudio.app.ui.components.timeAgo
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Ink
import com.claudio.app.ui.theme.Magenta
import com.claudio.app.ui.theme.Mint
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet

@Composable
fun HomeScreen(
    vm: ClaudioViewModel,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onOpenSession: (String) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    val sessions by vm.sessions.collectAsState()
    val transition = rememberInfiniteTransition(label = "home")
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "spin")
    val pulse by transition.animateFloat(1f, 1.18f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "pulse")

    AuroraBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            StaggeredIn(0, Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OwlLogo(size = 46.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Claudio", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Text("Your pocket study buddy", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    IconBubble(Icons.Filled.History, onHistory, contentDescription = "History")
                    Spacer(Modifier.width(10.dp))
                    IconBubble(Icons.Filled.Settings, onSettings, contentDescription = "Settings")
                }
            }
            Spacer(Modifier.height(30.dp))

            StaggeredIn(1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Stuck on a question?", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text(
                        "Snap it.",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            brush = Brush.horizontalGradient(listOf(Violet, Magenta, Cyan))
                        ),
                        fontSize = 40.sp
                    )
                }
            }
            Spacer(Modifier.height(26.dp))

            StaggeredIn(2) {
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val pressScale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(dampingRatio = 0.55f, stiffness = 600f), label = "press")
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(230.dp)) {
                    Box(
                        Modifier
                            .size(210.dp)
                            .graphicsLayer { scaleX = pulse; scaleY = pulse; alpha = 0.30f }
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Magenta, Color.Transparent)))
                    )
                    Box(
                        Modifier
                            .size(192.dp)
                            .graphicsLayer { rotationZ = spin }
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(Violet, Magenta, Amber, Cyan, Violet)))
                    )
                    Box(
                        Modifier
                            .size(180.dp)
                            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                            .clip(CircleShape)
                            .background(Ink)
                            .clickable(interactionSource = interaction, indication = null, onClick = onCamera),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.PhotoCamera, null, tint = TextPrimary, modifier = Modifier.size(46.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Take a photo", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                            Text("of any question", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            StaggeredIn(3) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Surface2)
                        .clickable(onClick = onGallery)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Image, null, tint = Cyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Choose from gallery", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
            Spacer(Modifier.height(28.dp))

            StaggeredIn(4, Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface1).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("What Claudio can do", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    AbilityRow(Icons.Filled.Calculate, Amber, "Solve math & science", "Step-by-step working, not just the answer")
                    AbilityRow(Icons.Filled.Translate, Cyan, "Read & translate notes", "Handwriting, textbooks, any language")
                    AbilityRow(Icons.Filled.Lightbulb, Mint, "Explain anything visual", "Diagrams, drawings, charts, characters")
                }
            }
            Spacer(Modifier.height(24.dp))

            if (sessions.isNotEmpty()) {
                StaggeredIn(5, Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Recent questions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                            Text(
                                "See all",
                                style = MaterialTheme.typography.labelMedium,
                                color = Violet,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onHistory).padding(6.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        sessions.take(3).forEach { session ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Surface1)
                                    .clickable { onOpenSession(session.id) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(session.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(timeAgo(session.updatedAt), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextMuted)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            StaggeredIn(6) {
                Text(
                    "BYOK · your key stays on this device",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AbilityRow(icon: ImageVector, tint: Color, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Surface2), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}