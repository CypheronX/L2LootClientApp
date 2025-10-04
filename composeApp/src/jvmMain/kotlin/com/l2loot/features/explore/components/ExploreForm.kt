package com.l2loot.features.explore.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l2loot.design.LocalSpacing
import com.l2loot.ui.components.SelectInput
import org.jetbrains.skiko.Cursor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreForm(
    chronicle: String,
    chronicleOptions: List<String>,
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Card(
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            modifier = modifier
                .widthIn(min = 930.dp, max = maxWidth - 110.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(LocalSpacing.current.space24)
            ) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    SelectInput(
                        value = chronicle,
                        options = chronicleOptions,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        label = {
                            Text(text = "Chronicle", style = MaterialTheme.typography.bodySmall)
                        },
                        onValueChange = { onChronicleChange(it) }
                    )

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space12))

                    val minLevelInteractionSource = remember { MutableInteractionSource() }
                    val isMinLevelFocused by minLevelInteractionSource.collectIsFocusedAsState()
                    val minLevelLabelFontSize by animateDpAsState(
                        targetValue = if (isMinLevelFocused || minLevel.isNotEmpty()) 12.dp else 16.dp,
                        animationSpec = tween(durationMillis = 150)
                    )

                    TextField(
                        modifier = Modifier
                            .width(130.dp)
                            .height(56.dp),
                        value = minLevel,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                onMinLevelChange(newValue)
                            }
                        },
                        singleLine = true,
                        label = {
                            Text(
                                text = "Min Level",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = minLevelLabelFontSize.value.sp
                                )
                            )
                        },
                        interactionSource = minLevelInteractionSource
                    )

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space12))

                    val maxLevelInteractionSource = remember { MutableInteractionSource() }
                    val isMaxLevelFocused by maxLevelInteractionSource.collectIsFocusedAsState()
                    val maxLevelLabelFontSize by animateDpAsState(
                        targetValue = if (isMaxLevelFocused || maxLevel.isNotEmpty()) 12.dp else 16.dp,
                        animationSpec = tween(durationMillis = 150)
                    )

                    TextField(
                        modifier = Modifier
                            .width(130.dp)
                            .height(56.dp),
                        value = maxLevel,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                onMaxLevelChange(newValue)
                            }
                        },
                        singleLine = true,
                        label = {
                            Text(
                                text = "Max Level",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = maxLevelLabelFontSize.value.sp
                                )
                            )
                        },
                        interactionSource = maxLevelInteractionSource
                    )

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space12))

                    val limitInteractionSource = remember { MutableInteractionSource() }
                    val isLimitFocused by limitInteractionSource.collectIsFocusedAsState()
                    val limitLabelFontSize by animateDpAsState(
                        targetValue = if (isLimitFocused || limit.isNotEmpty()) 12.dp else 16.dp,
                        animationSpec = tween(durationMillis = 150)
                    )

                    TextField(
                        modifier = Modifier
                            .width(145.dp)
                            .height(56.dp),
                        value = limit,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                onLimitChange(newValue)
                            }
                        },
                        singleLine = true,
                        label = {
                            Text(
                                text = "Results Count",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = limitLabelFontSize.value.sp
                                )
                            )
                        },
                        interactionSource = limitInteractionSource
                    )

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space12))

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
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .width(125.dp)
                        .height(56.dp)
                ) {
                    Text("Let's Spoil",
                        style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}