package com.l2loot.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

@Composable
fun Modifier.shimmerLoading(
    durationMillis: Int = 1500,
): Modifier {
    val transition = rememberInfiniteTransition(label = "")

    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "",
    )

    val shimmerColorStart = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val shimmerColorMiddle = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val shimmerColorEnd = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    return drawBehind {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    shimmerColorStart,
                    shimmerColorMiddle,
                    shimmerColorEnd,
                ),
                start = Offset(x = translateAnimation - 300f, y = translateAnimation - 300f),
                end = Offset(x = translateAnimation + 300f, y = translateAnimation + 300f),
            )
        )
    }
}