package com.l2loot.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skiko.Cursor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectInput(
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: @Composable (() -> Unit),
    onValueChange: (String) -> Unit,
    width: Dp = 160.dp,
    height: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            modifier = Modifier
                .width(width)
                .height(height)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            value = value.replaceFirstChar { it.uppercase() },
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onExpandedChange(false)
                        onValueChange(option)
                    }
                )
            }
        }
    }
}