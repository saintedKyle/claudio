package com.claudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.claudio.app.data.CURATED_MODELS
import com.claudio.app.data.ModelInfo
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Magenta
import com.claudio.app.ui.theme.Mint
import com.claudio.app.ui.theme.OutlineSoft
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.Surface2
import com.claudio.app.ui.theme.Surface3
import com.claudio.app.ui.theme.TextMuted
import com.claudio.app.ui.theme.TextPrimary
import com.claudio.app.ui.theme.TextSecondary
import com.claudio.app.ui.theme.Violet

@Composable
fun ClaudioFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Magenta,
    focusedBorderColor = Violet,
    unfocusedBorderColor = OutlineSoft,
    focusedLabelColor = Violet,
    unfocusedLabelColor = TextMuted,
    focusedContainerColor = Surface1,
    unfocusedContainerColor = Surface1,
    focusedTrailingIconColor = Cyan,
    unfocusedTrailingIconColor = TextMuted
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPicker(
    selected: String,
    onSelect: (String) -> Unit,
    models: List<ModelInfo>,
    loading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val liveLabel = models.find { it.id == selected }?.name
    val curatedLabel = CURATED_MODELS.find { it.first == selected }?.second
    val label = liveLabel ?: curatedLabel ?: selected

    Column(modifier) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                label = { Text(if (models.isNotEmpty()) "Free model (${models.size} found)" else "Vision model") },
                trailingIcon = {
                    if (loading) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Magenta, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.ArrowDropDown, null)
                    }
                },
                colors = ClaudioFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Surface2).heightIn(max = 380.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onRefresh)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Refresh, null, tint = Cyan, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (models.isNotEmpty()) "Refresh free models · ${models.count { it.vision }} can see photos"
                        else "Load free models from your key",
                        style = MaterialTheme.typography.labelMedium,
                        color = Cyan
                    )
                }
                if (models.isNotEmpty()) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(model.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, maxLines = 1)
                                        Text(model.id, style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Row(
                                        Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (model.vision) Violet.copy(alpha = 0.25f) else Surface3)
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (model.vision) {
                                            Icon(Icons.Filled.Visibility, null, tint = Mint, modifier = Modifier.size(11.dp))
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            if (model.vision) "sees photos" else "text only",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (model.vision) Mint else TextMuted
                                        )
                                    }
                                }
                            },
                            onClick = { onSelect(model.id); expanded = false }
                        )
                    }
                } else {
                    CURATED_MODELS.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    Text(id, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            },
                            onClick = { onSelect(id); expanded = false }
                        )
                    }
                }
            }
        }
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        } else if (models.isNotEmpty()) {
            Text(
                "All models above are free on your key. Photo questions need a “sees photos” model.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }
    }
}

@Composable
fun CustomModelField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text("Or paste any model id (optional)") },
            placeholder = { Text("e.g. google/gemini-2.5-pro", color = TextMuted) },
            singleLine = true,
            colors = ClaudioFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Any vision-capable model on OpenRouter works. This overrides the picker above.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )
    }
}