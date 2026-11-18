package com.claudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.claudio.app.ClaudioViewModel
import com.claudio.app.data.Settings
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.ClaudioFieldColors
import com.claudio.app.ui.components.CustomModelField
import com.claudio.app.ui.components.GradientButton
import com.claudio.app.ui.components.ModelPicker
import com.claudio.app.ui.components.TopBar
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Mint
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(vm: ClaudioViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var baseUrl by remember { mutableStateOf(settings.baseUrl) }
    var model by remember { mutableStateOf(settings.model) }
    var customModel by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    val freeModels by vm.freeModels.collectAsState()
    val modelsLoading by vm.modelsLoading.collectAsState()
    val modelsError by vm.modelsError.collectAsState()

    LaunchedEffect(apiKey, baseUrl) {
        if (apiKey.trim().length > 12) {
            delay(700)
            vm.fetchModels(apiKey.trim(), baseUrl.trim())
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

    AuroraBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            TopBar(title = "Settings", subtitle = "Your key, your model, your rules", onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface1).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Key, null, tint = Amber, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Connection", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API key") },
                        singleLine = true,
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                            }
                        },
                        colors = ClaudioFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL (OpenAI-compatible)") },
                        singleLine = true,
                        colors = ClaudioFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ModelPicker(
                        selected = model,
                        onSelect = { model = it },
                        models = freeModels,
                        loading = modelsLoading,
                        error = modelsError,
                        onRefresh = { vm.fetchModels(apiKey.trim(), baseUrl.trim()) }
                    )
                    CustomModelField(value = customModel, onChange = { customModel = it })
                }

                GradientButton(
                    text = "Save settings",
                    enabled = apiKey.isNotBlank(),
                    onClick = {
                        vm.saveSettings(apiKey, baseUrl, customModel.ifBlank { model })
                        saved = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(visible = saved, enter = fadeIn(), exit = fadeOut()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Mint, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Saved. Claudio is ready.", style = MaterialTheme.typography.bodyMedium, color = Mint)
                    }
                }

                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface1).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("About Claudio", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        "Claudio v1.2.0 · a study companion that reads any photo of a question and teaches you the answer. " +
                            "It is BYOK: you bring an OpenRouter (or any OpenAI-compatible) API key, and requests go straight from your phone to the provider. " +
                            "No accounts, no analytics, no servers in between. Conversations are stored only on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        "Get a key at openrouter.ai/keys — many vision models cost fractions of a cent per question.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Made by poging kyle",
                        style = MaterialTheme.typography.labelLarge,
                        color = Amber
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}