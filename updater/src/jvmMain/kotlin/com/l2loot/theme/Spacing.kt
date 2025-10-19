package com.l2loot.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val space1: Dp = 1.dp,
    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space6: Dp = 6.dp,
    val space8: Dp = 8.dp,
    val space10: Dp = 10.dp,
    val space12: Dp = 12.dp,
    val space14: Dp = 14.dp,
    val space16: Dp = 16.dp,
    val space18: Dp = 18.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space28: Dp = 28.dp,
    val space34: Dp = 34.dp,
    val space36: Dp = 36.dp,
    val space42: Dp = 42.dp,
    val space48: Dp = 48.dp,
    val space72: Dp = 72.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

