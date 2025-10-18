package com.l2loot.features.explore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.l2loot.design.LocalSpacing
import com.l2loot.designsystem.components.shimmerLoading

@Composable
fun MonsterCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(top = LocalSpacing.current.space12)
                .padding(bottom = LocalSpacing.current.space12)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LocalSpacing.current.space12)
            ) {
                // Monster name shimmer
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerLoading()
                )

                // Level shimmer
                Box(
                    modifier = Modifier
                        .width(47.dp)
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerLoading()
                )
            }

            Spacer(modifier = Modifier.size(LocalSpacing.current.space8))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = LocalSpacing.current.space1,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(LocalSpacing.current.space12))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LocalSpacing.current.space12)
            ) {
                // Average Income shimmer
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerLoading()
                )

                // HP shimmer
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerLoading()
                )
            }

            Spacer(modifier = Modifier.size(LocalSpacing.current.space6))

            Column(
                modifier = Modifier
                    .padding(horizontal = LocalSpacing.current.space12)
            ) {
                // Materials label shimmer
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(23.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerLoading()
                )
                
                Spacer(modifier = Modifier.size(LocalSpacing.current.space4))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(89.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .shimmerLoading()
                )
            }
        }
    }
}
