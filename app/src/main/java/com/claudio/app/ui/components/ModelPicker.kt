package com.claudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.claudio.app.data.CURATED_MODELS
import com.claudio.app.ui.theme.Cyan
import com.claudio.app.ui.theme.Magenta
import com.claudio.app.ui.theme.OutlineSoft
import com.claudio.app.ui.theme.Surface1
import com.claudio.app.ui.theme.Surface2
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = CURATED_MODELS.find { it.first == selected }?.second ?: selected

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Vision model") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
            colors = ClaudioFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Surface2)
        ) {
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