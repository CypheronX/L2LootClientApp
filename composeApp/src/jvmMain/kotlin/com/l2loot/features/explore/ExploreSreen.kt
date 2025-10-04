package com.l2loot.features.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.l2loot.design.LocalSpacing
import com.l2loot.features.explore.components.ExploreForm
import com.l2loot.features.explore.components.MonsterCard
import com.l2loot.features.explore.components.MonsterCardData
import com.l2loot.features.explore.components.MonsterMaterial
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExploreScreen() {
    val viewModel: ExploreViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val horizontalScrollState = rememberScrollState()
    val gridState = rememberLazyGridState()

    var filterPainter by remember {
        mutableStateOf<Painter?>(null)
    }
    var sortPainter by remember {
        mutableStateOf<Painter?>(null)
    }

    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        try {
            val filterBytes = Res.readBytes("files/svg/filter.svg")
            val sortBytes = Res.readBytes("files/svg/sort.svg")

            if (filterBytes.isNotEmpty()) {
                filterPainter = filterBytes.decodeToSvgPainter(density)
            }
            if (sortBytes.isNotEmpty()) {
                sortPainter = sortBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            println("Failed to load svg icons: ${e.message}")
        }
    }
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = LocalSpacing.current.space34,
                start = LocalSpacing.current.space34,
                end = LocalSpacing.current.space34,
            )
    ) {
        val minWidth = 930.dp
        val contentWidth = maxOf(minWidth, maxWidth)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Explore Spoil",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space20))

                ExploreForm(
                    chronicle = state.chronicle,
                    chronicleOptions = viewModel.chronicleOptions,
                    minLevel = state.minLevel,
                    maxLevel = state.maxLevel,
                    limit = state.limit,
                    limitOptions = viewModel.limitOptions,
                    showRiftMobs = state.showRiftMobs,
                    onChronicleChange = { viewModel.onEvent(ExploreScreenEvent.ChronicleChanged(it)) },
                    onMinLevelChange = { viewModel.onEvent(ExploreScreenEvent.MinLevelChanged(it)) },
                    onMaxLevelChange = { viewModel.onEvent(ExploreScreenEvent.MaxLevelChanged(it)) },
                    onLimitChange = { viewModel.onEvent(ExploreScreenEvent.LimitChanged(it)) },
                    onShowRiftMobsChange = { viewModel.onEvent(ExploreScreenEvent.ShowRiftMobsChanged(it)) },
                    onSubmit = { viewModel.onEvent(ExploreScreenEvent.Explore) },
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space1))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = LocalSpacing.current.space34)
                ) {
                    Button(
                        onClick = {},
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                        ) {
                            filterPainter?.let { filter ->
                                Image(
                                    painter = filter,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                                    modifier = Modifier
                                        .size(LocalSpacing.current.space16)
                                )
                            }
                            Spacer(modifier = Modifier.size(LocalSpacing.current.space8))
                            Text(
                                text = "Filter",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space6))

                    Button(
                        onClick = {},
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                        ) {
                            sortPainter?.let { sort ->
                                Image(
                                    painter = sort,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                                    modifier = Modifier
                                        .size(LocalSpacing.current.space16)
                                )
                            }
                            Spacer(modifier = Modifier.size(LocalSpacing.current.space8))
                            Text(
                                text = "Sort",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(LocalSpacing.current.space1))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.space12),
                        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space12),
                        modifier = Modifier.fillMaxSize()
                            .padding(end = LocalSpacing.current.space34)
                    ) {
                        items(20) { monster ->
                            MonsterCard(monsterData = MonsterCardData(
                                monsterId = 21143,
                                monsterName = "Catacomb Scavenger Bat",
                                level = 31,
                                averageIncome = 7100,
                                hpMultiplier = 4,
                                materials = listOf(
                                    MonsterMaterial(
                                        materialName = "Stone of Purity",
                                        materialCountMin = 1,
                                        materialCountMax = 1
                                    ),
                                    MonsterMaterial(
                                        materialName = "Animal Bone",
                                        materialCountMin = 1,
                                        materialCountMax = 3
                                    ),
                                    MonsterMaterial(
                                        materialName = "Scroll: Enchant Weapon (D)",
                                        materialCountMin = 1,
                                        materialCountMax = 1
                                    )
                                )
                            ))
                        }
                    }

                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(gridState),
                        style = ScrollbarStyle(
                            minimalHeight = 48.dp,
                            thickness = 13.dp,
                            shape = MaterialTheme.shapes.extraLarge,
                            hoverDurationMillis = 300,
                            unhoverColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            hoverColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .padding(start = LocalSpacing.current.space8)
                            .offset(x = -LocalSpacing.current.space10)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .pointerHoverIcon(PointerIcon.Hand)
                    )
                }

                Spacer(modifier = Modifier.size(LocalSpacing.current.space10))

                Text(
                    text = "*Average income = spoil drop income + monster drop income (including adena and other selable materials)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space8))
            }
        }
    }
}
