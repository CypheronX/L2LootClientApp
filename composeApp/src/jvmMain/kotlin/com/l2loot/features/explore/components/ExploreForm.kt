package com.l2loot.features.explore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import com.l2loot.design.LocalSpacing

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
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("C5", "Interlude")

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
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = chronicle,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Chronicle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    onChronicleChange(selectionOption)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                TextField(
                    value = minLevel,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onMinLevelChange(newValue)
                        }
                    },
                    label = { Text("Min Level") }
                )
                TextField(
                    value = maxLevel,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onMaxLevelChange(newValue)
                        }
                    },
                    label = { Text("Max Level") }
                )
                TextField(
                    value = limit,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onLimitChange(newValue)
                        }
                    },
                    label = { Text("Results Count") }
                )

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Include Rift Mobs",
                        style = MaterialTheme.typography.bodyLarge,
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
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
            ) {
                Text("Let's Spoil",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}