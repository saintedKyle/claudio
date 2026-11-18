package com.claudio.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claudio.app.data.Settings
import com.claudio.app.ui.components.AuroraBackground
import com.claudio.app.ui.components.ClaudioFieldColors
import com.claudio.app.ui.components.CustomModelField
import com.claudio.app.ui.components.GradientButton
import com.claudio.app.ui.components.ModelPicker
import com.claudio.app.ui.components.OwlLogo
import com.claudio.app.ui.theme.Amber
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Magenta
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet

@Composable
fun SetupScreen(onStart: (key: String, baseUrl: String, model: String) -> Unit) {
    var apiKey by rememberSaveable { mutableStateOf("") }
    var baseUrl by rememberSaveable { mutableStateOf(Settings.DEFAULT_BASE_URL) }
    var model by rememberSaveable { mutableStateOf(Settings.DEFAULT_MODEL) }
    var customModel by rememberSaveable { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val entrance by animateFloatAsState(if (shown) 1f else 0f, tween(700, easing = FastOutSlowInEasing), label = "entrance")

    AuroraBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Box(Modifier.graphicsLayer { alpha = entrance; translationY = (1f - entrance) * 60f }) {
                OwlLogo(size = 128.dp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Claudio",
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = Brush.horizontalGradient(listOf(Violet, Magenta, Amber))
                ),
                fontSize = 44.sp
            )
            Text(
                "Snap it. Ask it. Learn it.",
                style = MaterialTheme.typography.titleMedium,
                color = Cyan
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Photograph any question — homework, notes, a diagram, even handwriting — and Claudio reads it and teaches you the answer.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = entrance; translationY = (1f - entrance) * 90f }
                    .clip(RoundedCornerShape(24.dp))
                    .background(Surface1)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Key, null, tint = Amber, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Bring your own key", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Text(
                    "Claudio talks straight to OpenRouter from your phone. Your key never leaves this device. Grab a free one at openrouter.ai/keys",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("OpenRouter API key") },
                    placeholder = { Text("sk-or-v1-…", color = TextMuted) },
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
                ModelPicker(selected = model, onSelect = { model = it })
                CustomModelField(value = customModel, onChange = { customModel = it })

                Text(
                    if (showAdvanced) "Hide advanced" else "Advanced: custom provider URL",
                    style = MaterialTheme.typography.labelMedium,
                    color = Violet,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(interactionSource = null, indication = null) { showAdvanced = !showAdvanced }
                        .padding(vertical = 4.dp)
                )
                if (showAdvanced) {
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL (OpenAI-compatible)") },
                        singleLine = true,
                        colors = ClaudioFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Works with OpenRouter, OpenAI, or any OpenAI-compatible endpoint.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            GradientButton(
                text = "Start learning",
                icon = Icons.Filled.AutoAwesome,
                enabled = apiKey.isNotBlank(),
                onClick = { onStart(apiKey, baseUrl, customModel.ifBlank { model }) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Private by design · no accounts · no tracking",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "made by poging kyle",
                style = MaterialTheme.typography.labelSmall,
                color = Violet
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}