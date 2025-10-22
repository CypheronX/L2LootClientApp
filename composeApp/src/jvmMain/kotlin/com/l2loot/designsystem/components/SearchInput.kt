package com.l2loot.designsystem.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.l2loot.Config
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter

@Composable
fun SearchInput(
    value: String,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchPainter by remember { mutableStateOf<Painter?>(null) }
    var timesPainter by remember { mutableStateOf<Painter?>(null) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        try {
            val searchBytes = Res.readBytes("files/svg/search.svg")
            val timesBytes = Res.readBytes("files/svg/times.svg")
            
            if (searchBytes.isNotEmpty()) {
                searchPainter = searchBytes.decodeToSvgPainter(density)
            }
            if (timesBytes.isNotEmpty()) {
                timesPainter = timesBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            if (Config.IS_DEBUG) {
                println("Failed to load search icons: ${e.message}")
            }
        }
    }

    OutlinedTextField(
        modifier = modifier
            .width(250.dp)
            .height(56.dp),
        value = value,
        onValueChange = onSearch,
        singleLine = true,
        placeholder = {
            Text(
                text = "Search",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            searchPainter?.let { painter ->
                Icon(
                    painter = painter,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingIcon = {
            if (value.isNotEmpty() && timesPainter != null) {
                IconButton(
                    onClick = { onSearch("") },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    timesPainter?.let { painter ->
                        Icon(
                            painter = painter,
                            contentDescription = "Clear search",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
        ),
        shape = MaterialTheme.shapes.small
    )
}