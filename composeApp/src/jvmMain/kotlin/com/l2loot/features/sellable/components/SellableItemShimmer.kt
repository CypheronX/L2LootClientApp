package com.l2loot.features.sellable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.l2loot.design.LocalSpacing
import com.l2loot.designsystem.components.shimmerLoading

@Composable
fun SellableItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Image shimmer
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .shimmerLoading()
        )

        Spacer(modifier = Modifier.size(LocalSpacing.current.space10))

        // TextField shimmer
        Box(
            modifier = Modifier
                .width(199.dp)
                .height(56.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .shimmerLoading()
        )
    }
}
