package com.claudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.data.AccentTone
import com.claudio.app.data.BubbleStyle
import com.claudio.app.data.Settings
import com.claudio.app.data.SurfaceTone
import com.claudio.app.data.ThemePrefs
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.ClaudioFieldColors
import com.claudio.app.ui.components.ClaudioMark
import com.claudio.app.ui.components.CustomModelField
import com.claudio.app.ui.components.GradientButton
import com.claudio.app.ui.components.ModelPicker
import com.claudio.app.ui.components.TopBar
import com.claudio.app.ui.theme.LocalClaudioColors
import com.claudio.app.ui.theme.accentColor
import com.claudio.app.ui.theme.tonePreview
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(vm: ClaudioViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val colors = LocalClaudioColors.current

    var theme by remember(settings.loaded) { mutableStateOf(settings.theme) }
    var customKey by remember(settings.customKey) { mutableStateOf(settings.customKey) }
    var baseUrl by remember(settings.baseUrl) { mutableStateOf(settings.baseUrl) }
    var model by remember(settings.model) { mutableStateOf(settings.model) }
    var customModel by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var showOwnKey by remember { mutableStateOf(settings.customKey.isNotBlank()) }
    var saved by remember { mutableStateOf(false) }

    val freeModels by vm.freeModels.collectAsState()
    val modelsLoading by vm.modelsLoading.collectAsState()
    val modelsError by vm.modelsError.collectAsState()

    LaunchedEffect(customKey, baseUrl) {
        if (customKey.trim().length > 12) {
            delay(700)
            vm.fetchModels(customKey.trim(), baseUrl.trim())
        }
    }
    LaunchedEffect(freeModels) {
        if (model == Settings.DEFAULT_MODEL && freeModels.isNotEmpty()) {
            model = (freeModels.firstOrNull { it.vision } ?: freeModels.first()).id
        }
    }
    LaunchedEffect(saved) {
        if (saved) {
            delay(2200)
            saved = false
        }
    }
    LaunchedEffect(settings.customKey) {
        if (settings.customKey.isNotBlank() && customKey.isBlank()) customKey = settings.customKey
    }

    fun updateTheme(next: ThemePrefs) {
        theme = next
        vm.saveTheme(next)
    }

    AuroraBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            TopBar(title = "Settings", subtitle = "Make Claudio yours", onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(title = "Appearance", icon = Icons.Filled.Palette) {
                    Text(
                        "Your name",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                    OutlinedTextField(
                        value = theme.name,
                        onValueChange = { updateTheme(theme.copy(name = it.take(24))) },
                        singleLine = true,
                        placeholder = { Text("saint", color = colors.textMuted) },
                        colors = ClaudioFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Accent",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AccentTone.entries.forEach { tone ->
                            AccentSwatch(
                                tone = tone,
                                selected = theme.accent == tone,
                                onClick = { updateTheme(theme.copy(accent = tone)) }
                            )
                        }
                    }

                    Text(
                        "Background",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SurfaceTone.entries.forEach { tone ->
                            ToneChip(
                                tone = tone,
                                selected = theme.tone == tone,
                                onClick = { updateTheme(theme.copy(tone = tone)) }
                            )
                        }
                    }

                    Text(
                        "Your messages",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                    SegmentedOptions(
                        options = BubbleStyle.entries.map { it to it.label },
                        selected = theme.bubble,
                        onSelect = { updateTheme(theme.copy(bubble = it)) }
                    )

                    Text(
                        "Text size",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                    SegmentedOptions(
                        options = listOf(0.94f to "Small", 1f to "Default", 1.08f to "Large"),
                        selected = theme.fontScale,
                        onSelect = { updateTheme(theme.copy(fontScale = it)) }
                    )

                    Row(
                        Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Reduce motion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                "Calm the backdrop, mascot and entrances.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = theme.reduceMotion,
                            onCheckedChange = { updateTheme(theme.copy(reduceMotion = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.backgroundDeep,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surface3,
                                uncheckedBorderColor = colors.outline
                            )
                        )
                    }
                }

                SectionCard(title = "Connection", icon = Icons.Filled.Key) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (settings.usingFreeKey) colors.accent.copy(alpha = 0.11f)
                                else colors.surface2
                            )
                            .border(
                                1.dp,
                                if (settings.usingFreeKey) colors.accent.copy(alpha = 0.30f) else colors.outline,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClaudioMark(size = 30.dp, animated = false)
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (settings.usingFreeKey) "Claudio Free" else "Your own key",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                when {
                                    settings.usingFreeKey && settings.apiKey.isBlank() ->
                                        "Free mode isn't configured in this build — add your own key below."
                                    settings.usingFreeKey ->
                                        "No key needed — the built-in free key is active."
                                    else ->
                                        "Using ${settings.baseUrl.removePrefix("https://").removeSuffix("/api/v1")}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (!settings.usingFreeKey) {
                        Text(
                            "Switch back to the built-in free key",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    vm.useFreeKey()
                                    customKey = ""
                                    baseUrl = Settings.DEFAULT_BASE_URL
                                    model = Settings.DEFAULT_MODEL
                                    showOwnKey = false
                                }
                                .padding(vertical = 3.dp)
                        )
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showOwnKey = !showOwnKey }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Key, null, tint = colors.textMuted, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (showOwnKey) "Hide my key" else "Use my own key (optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        val rotation by animateFloatAsState(
                            targetValue = if (showOwnKey) 180f else 0f,
                            animationSpec = tween(200),
                            label = "keyArrow"
                        )
                        Icon(
                            Icons.Filled.ExpandMore,
                            null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp).rotate(rotation)
                        )
                    }

                    AnimatedVisibility(
                        visible = showOwnKey,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = customKey,
                                onValueChange = { customKey = it },
                                label = { Text("OpenRouter API key") },
                                placeholder = { Text("sk-or-v1-…", color = colors.textMuted) },
                                singleLine = true,
                                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { showKey = !showKey }) {
                                        Icon(
                                            if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            null,
                                            tint = colors.textMuted
                                        )
                                    }
                                },
                                colors = ClaudioFieldColors(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = baseUrl,
                                onValueChange = { baseUrl = it },
                                label = { Text("Base URL (OpenAI-compatible)") },
                                singleLine = true,
                                colors = ClaudioFieldColors(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "Any OpenAI-compatible endpoint works. The built-in free key is only ever used with OpenRouter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    ModelPicker(
                        selected = model,
                        onSelect = { model = it },
                        models = freeModels,
                        loading = modelsLoading,
                        error = modelsError,
                        onRefresh = {
                            val activeKey = if (showOwnKey && customKey.isNotBlank()) customKey else settings.apiKey
                            vm.fetchModels(activeKey, if (showOwnKey) baseUrl else Settings.DEFAULT_BASE_URL)
                        }
                    )
                    if (showOwnKey) {
                        CustomModelField(value = customModel, onChange = { customModel = it })
                    }

                    GradientButton(
                        text = if (showOwnKey) "Save connection" else "Save changes",
                        icon = Icons.Filled.AutoAwesome,
                        onClick = {
                            if (showOwnKey) {
                                vm.saveSettings(customKey, baseUrl, customModel.ifBlank { model })
                            } else {
                                vm.saveSettings("", Settings.DEFAULT_BASE_URL, customModel.ifBlank { model })
                            }
                            saved = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(visible = saved, enter = fadeIn(), exit = fadeOut()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                null,
                                tint = colors.success,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Saved. Claudio is ready.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.success
                            )
                        }
                    }
                }

                SectionCard(title = "About", icon = Icons.Filled.AutoAwesome) {
                    Text(
                        "Claudio v1.3.0 · a warm study companion that reads photos, drawings and notes, then teaches the answer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Text(
                        "Free by default. Bring your own key whenever you want. Conversations stay on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Text(
                        "Made by poging kyle",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.accent
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalClaudioColors.current
    val shape = RoundedCornerShape(24)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface1)
            .border(1.dp, colors.outline, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = colors.accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
        content()
    }
}

@Composable
private fun AccentSwatch(tone: AccentTone, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalClaudioColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor(tone))
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) colors.textPrimary else colors.outline,
                    CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = tone.label,
                    tint = colors.backgroundDeep,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            tone.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.textPrimary else colors.textMuted
        )
    }
}

@Composable
private fun ToneChip(tone: SurfaceTone, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalClaudioColors.current
    val (background, surface) = tonePreview(tone)
    val shape = RoundedCornerShape(13)
    Row(
        Modifier
            .clip(shape)
            .background(if (selected) colors.surface3 else colors.surface2)
            .border(1.dp, if (selected) colors.accent.copy(alpha = 0.55f) else colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(background)
                .border(1.dp, colors.outline, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Box(Modifier.size(12.dp).clip(CircleShape).background(surface))
        Spacer(Modifier.width(8.dp))
        Text(
            tone.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.textPrimary else colors.textSecondary
        )
    }
}

@Composable
private fun <T> SegmentedOptions(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    val colors = LocalClaudioColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14))
            .background(colors.surface2)
            .padding(3.dp)
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11))
                    .background(if (active) colors.surface3 else Color.Transparent)
                    .border(
                        1.dp,
                        if (active) colors.outline else Color.Transparent,
                        RoundedCornerShape(11)
                    )
                    .clickable { onSelect(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) colors.textPrimary else colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
