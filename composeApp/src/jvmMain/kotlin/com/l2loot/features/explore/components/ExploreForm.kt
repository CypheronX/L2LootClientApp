package com.l2loot.features.explore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.l2loot.design.LocalSpacing
import org.jetbrains.skiko.Cursor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreForm(
    chronicle: String,
    minLevel: String,
    maxLevel: String,
    limit: String,
    showRiftMobs: Boolean,
    onChronicleChange: (String) -> Unit,
    onMinLevelChange: (String) -> Unit,
    onMaxLevelChange: (String) -> Unit,
    onLimitChange: (String) -> Unit,
    onShowRiftMobsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by mutableStateOf(false)

    Card(
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(LocalSpacing.current.space24)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.space12),
                modifier = Modifier.weight(1f)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    TextField(
                        modifier = Modifier
                            .width(135.dp)
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                        value = chronicle.replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(text = "Chronicle", style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "C5", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                expanded = false
                                onChronicleChange("c5")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Interlude", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                expanded = false
                                onChronicleChange("interlude")
                            }
                        )
                    }
                }

                TextField(
                    modifier = Modifier
                        .width(130.dp),
                    value = minLevel,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onMinLevelChange(newValue)
                        }
                    },
                    label = { Text(text = "Min Level", style = MaterialTheme.typography.bodySmall) }
                )
                TextField(
                    modifier = Modifier
                        .width(130.dp),
                    value = maxLevel,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onMaxLevelChange(newValue)
                        }
                    },
                    label = { Text(text = "Max Level", style = MaterialTheme.typography.bodySmall) }
                )
                TextField(
                    modifier = Modifier
                        .width(130.dp),
                    value = limit,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onLimitChange(newValue)
                        }
                    },
                    label = { Text(text = "Results Count", style = MaterialTheme.typography.bodySmall) }
                )

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .width(133.dp),
                ) {
                    Text("Include Rift Mobs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = showRiftMobs,
                        onCheckedChange = onShowRiftMobsChange
                    )
                }
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .padding(
                        horizontal = LocalSpacing.current.space24,
                        vertical = LocalSpacing.current.space16
                    )
                    .background(MaterialTheme.colorScheme.primary)
                    .clip(CircleShape)
            ) {
                Text("Let's Spoil",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}