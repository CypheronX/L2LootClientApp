package com.l2loot.features.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.l2loot.Config
import com.l2loot.domain.model.HPMultiplier
import com.l2loot.design.LocalSpacing
import com.l2loot.features.explore.components.ExploreForm
import com.l2loot.features.explore.components.MonsterCard
import com.l2loot.features.explore.components.MonsterCardShimmer
import com.l2loot.designsystem.components.NoResultsFound
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExploreScreen() {
    val viewModel: ExploreViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val horizontalScrollState = rememberScrollState()
    val gridState = rememberLazyGridState()
    var showMenu by remember { mutableStateOf(false) }

    var filterPainter by remember {
        mutableStateOf<Painter?>(null)
    }
    
    var spoilPainter by remember {
        mutableStateOf<Painter?>(null)
    }

    var chevronPainter by remember {
        mutableStateOf<Painter?>(null)
    }

    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        try {
            val filterBytes = Res.readBytes("files/svg/filter.svg")
            val spoilBytes = Res.readBytes("files/svg/spoil.svg")
            val chevronBytes = Res.readBytes("files/svg/times.svg")

            if (filterBytes.isNotEmpty()) {
                filterPainter = filterBytes.decodeToSvgPainter(density)
            }
            if (spoilBytes.isNotEmpty()) {
                spoilPainter = spoilBytes.decodeToSvgPainter(density)
            }
            if (chevronBytes.isNotEmpty()) {
                chevronPainter = chevronBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            if (Config.IS_DEBUG) {
                println("Failed to load svg icons: ${e.message}")
            }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = LocalSpacing.current.space34)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.space8),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        state.selectedHPMultipliers.forEach { multiplier ->
                            AssistChip(
                                onClick = {
                                    viewModel.onEvent(ExploreScreenEvent.HPMultiplierToggled(multiplier))
                                },
                                label = {
                                    Text(
                                        text = "HP ${multiplier.getHPMultiplierLabel()}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    chevronPainter?.let { chevron ->
                                        Image(
                                            painter = chevron,
                                            contentDescription = "Remove filter",
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                            modifier = Modifier
                                                .size((10.5).dp)
                                        )
                                    }
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0F),
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    trailingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.size(LocalSpacing.current.space8))
                    
                    Box {
                        Button(
                            onClick = {
                                showMenu = !showMenu
                            },
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
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .width(220.dp)
                                .heightIn(max = 300.dp)
                        ) {
                            Text(
                                text = "HP Multipliers",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(horizontal = LocalSpacing.current.space12)
                                    .padding(top = LocalSpacing.current.space12)
                                    .padding(bottom = LocalSpacing.current.space8)
                            )

                            HPMultiplier.entries.forEach { multiplier ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onEvent(ExploreScreenEvent.HPMultiplierToggled(multiplier))
                                        }
                                        .padding(horizontal = LocalSpacing.current.space12)
                                        .padding(vertical = LocalSpacing.current.space4)
                                        .pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Checkbox(
                                        checked = multiplier in state.selectedHPMultipliers,
                                        onCheckedChange = {
                                            viewModel.onEvent(ExploreScreenEvent.HPMultiplierToggled(multiplier))
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                    )
                                    Spacer(modifier = Modifier.size(LocalSpacing.current.space8))
                                    Text(
                                        text = multiplier.getHPMultiplierLabel(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
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
                        if (state.isRefreshing) {
                            items(6) {
                                MonsterCardShimmer()
                            }
                        } else {
                            items(state.monsters) { monster ->
                                MonsterCard(monsterData = monster)
                            }
                        }
                    }

                    if (state.monsters.isEmpty() && !state.isRefreshing) {
                        NoResultsFound(
                            iconPainter = spoilPainter,
                            message = "These lands hold no spoils for ye by yer criteria.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    if (state.monsters.isNotEmpty()) {
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
                }

                Spacer(modifier = Modifier.size(LocalSpacing.current.space10))

                Text(
                    text = "*Average income = spoil drop income + monster drop income (including adena and other sellable materials)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space8))
            }
        }
    }
}
