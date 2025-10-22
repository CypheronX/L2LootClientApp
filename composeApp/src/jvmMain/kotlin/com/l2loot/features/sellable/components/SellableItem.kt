package com.l2loot.features.sellable.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.l2loot.Config
import com.l2loot.design.LocalSpacing
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToImageBitmap

data class SellableItemData(
    val key: String,
    val name: String,
)

@Composable
fun SellableItem(
    sellableItem: SellableItemData,
    price: String,
    onPriceChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember(sellableItem.key) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(sellableItem.key) {
        imageBitmap = null
        try {
            val imageBytes = Res.readBytes("files/sellable_items/${sellableItem.key}.png")

            if (imageBytes.isNotEmpty()) {
                imageBitmap = imageBytes.decodeToImageBitmap()
            }
        } catch (e: Exception) {
            if (Config.IS_DEBUG) {
                println("Failed to load image for ${sellableItem.key}: ${e.message}")
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(
                    if (imageBitmap == null) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            imageBitmap?.let { image ->
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(LocalSpacing.current.space10))

        TextField(
            modifier = Modifier
                .width(199.dp)
                .height(56.dp),
            value = price,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onPriceChange(newValue)
                }
            },
            singleLine = true,
            label = {
                Text(
                    text = sellableItem.name,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            enabled = enabled
        )
    }
}
