package com.l2loot.features.explore.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.l2loot.data.monsters.strategy.DropItemInfo
import com.l2loot.data.monsters.strategy.HPMultiplier
import com.l2loot.data.monsters.strategy.MonsterResult
import com.l2loot.design.LocalSpacing
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MonsterCard(
    monsterData: MonsterResult,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val monsterUrl = "https://lineage2wiki.org/c5/monster/${monsterData.id}"

    var isHovered by remember { mutableStateOf(false) }

    var linkPainter by remember {
        mutableStateOf<Painter?>(null)
    }

    val density = LocalDensity.current

    val hpMultiplierValue = if (monsterData.hpMultiplier == HPMultiplier.X05) {
        "x1/2"
    } else {
        "x${ monsterData.hpMultiplier.value.toInt() }"
    }

    LaunchedEffect(Unit) {
        try {
            val linkBytes = Res.readBytes("files/svg/link.svg")

            if (linkBytes.isNotEmpty()) {
                linkPainter = linkBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            println("Failed to load svg icons: ${e.message}")
        }
    }

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
                val underlineOpacity by animateFloatAsState(
                    targetValue = if (isHovered) 1f else 0f,
                    animationSpec = tween(durationMillis = 150)
                )

                val textColor by animateColorAsState(
                    targetValue = if (isHovered) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(durationMillis = 150)
                )

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .onPointerEvent(PointerEventType.Enter) {
                            isHovered = true
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            isHovered = false
                        }
                        .drawWithContent {
                            drawContent()

                            val strokeWidth = 1.dp.toPx()
                            val verticalOffset = size.height - strokeWidth

                            drawLine(
                                color = textColor.copy(alpha = underlineOpacity),
                                start = Offset(0f, verticalOffset),
                                end = Offset(size.width, verticalOffset),
                                strokeWidth = strokeWidth
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            uriHandler.openUri(monsterUrl)
                        }
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text(
                        text = monsterData.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    linkPainter?.let { link ->
                        Image(
                            painter = link,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(textColor),
                            modifier = Modifier
                                .size((7.5).dp)
                                .offset(x = (7).dp)
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                Row {
                    Text(
                        text = "Lvl:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .padding(end = LocalSpacing.current.space10)
                            .alignBy(FirstBaseline)
                    )
                    Text(
                        text = monsterData.level.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .alignBy(FirstBaseline)
                    )
                }
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
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Average Income:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .padding(end = LocalSpacing.current.space10)
                            .alignBy(FirstBaseline)
                    )
                    Text(
                        text = "${monsterData.getAverageIncome()} a.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .alignBy(FirstBaseline)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "HP:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .padding(end = LocalSpacing.current.space10)
                            .alignBy(FirstBaseline)
                    )
                    Text(
                        text = hpMultiplierValue,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .alignBy(FirstBaseline)
                    )
                }
            }

            Spacer(modifier = Modifier.size(LocalSpacing.current.space6))

            Column(
                modifier = Modifier
                    .padding(horizontal = LocalSpacing.current.space12)
            ) {
                Text(
                    text = "Materials:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                MonsterMaterialsTable(
                    monsterMaterials = monsterData.spoils
                )
            }
        }
    }
}

@Composable
fun MonsterMaterialsTable(
    monsterMaterials: List<DropItemInfo>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
    ) {
        monsterMaterials.forEachIndexed { index, material ->
            val materialsCount = if (material.max != material.min) {
                "${material.min} - ${material.max}"
            } else {
                "${material.max}"
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if ((index + 1) % 2 == 0) {
                            MaterialTheme.colorScheme.onSurface.copy(0.02f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(0.08f)
                        }
                    )
                    .padding(LocalSpacing.current.space8)

            ) {
                Text(
                    text = material.itemName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = materialsCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
