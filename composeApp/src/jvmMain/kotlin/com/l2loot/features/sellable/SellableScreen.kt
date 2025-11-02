package com.l2loot.features.sellable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.l2loot.Config
import com.l2loot.design.LocalSpacing
import com.l2loot.features.sellable.components.SellableItem
import com.l2loot.features.sellable.components.SellableItemData
import com.l2loot.features.sellable.components.SellableItemShimmer
import com.l2loot.designsystem.components.NoResultsFound
import com.l2loot.designsystem.components.SearchInput
import com.l2loot.designsystem.components.SelectInput
import com.l2loot.domain.model.ServerName
import l2loot.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SellableScreen() {
    val viewModel: SellableViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val horizontalScrollState = rememberScrollState()
    
    var searchIconPainter by remember { mutableStateOf<Painter?>(null) }
    var expandedServer by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        try {
            val searchBytes = Res.readBytes("files/svg/search.svg")
            if (searchBytes.isNotEmpty()) {
                searchIconPainter = searchBytes.decodeToSvgPainter(density)
            }
        } catch (e: Exception) {
            if (Config.IS_DEBUG) {
                println("Failed to load search icon: ${e.message}")
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
                    text = "Sellable Items",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(LocalSpacing.current.space20))

                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.space24),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Card(
                            colors = CardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.padding(LocalSpacing.current.space20)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier
                                        .width(240.dp)
                                ) {
                                    Text(
                                        "Use Managed Prices",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.size(LocalSpacing.current.space6))
                                    Switch(
                                        checked = state.managedPrices,
                                        onCheckedChange = {
                                            viewModel.onEvent(SellableScreenEvent.TogglePriceSource(it))
                                        },
                                        modifier = Modifier
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = state.managedPrices,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.size(LocalSpacing.current.space16))

                                        SelectInput(
                                            value = state.server.displayName,
                                            options = ServerName.entries.map { it.displayName },
                                            expanded = expandedServer,
                                            onExpandedChange = { expandedServer = it },
                                            label = {
                                                Text(text = "Server", style = MaterialTheme.typography.bodySmall)
                                            },
                                            onValueChange = { displayName ->
                                                val server = ServerName.entries.find { it.displayName == displayName }
                                                server?.let { viewModel.onEvent(SellableScreenEvent.ServerChanged(it)) }
                                            },
                                            width = 240.dp
                                        )
                                    }
                                }
                            }
                        }


                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                SearchInput(
                                    value = state.searchValue,
                                    onSearch = { searchValue ->
                                        viewModel.onEvent(SellableScreenEvent.OnSearch(searchValue))
                                    }
                                )
                            }

                            Spacer(
                                modifier = Modifier
                                    .size(LocalSpacing.current.space16)
                            )

                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                val minHeight = maxHeight
                                
                                if (state.firstColumnItems.isEmpty() && state.secondColumnItems.isEmpty() && state.searchValue.isNotBlank() && !state.loading) {
                                    NoResultsFound(
                                        iconPainter = searchIconPainter,
                                        message = "No items match your search."
                                    )
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = minHeight)
                                            .verticalScroll(scrollState)
                                    ) {
                                        Card(
                                            colors = CardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            ),
                                            modifier = Modifier
                                                .heightIn(min = minHeight)
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space14),
                                                modifier = Modifier
                                                    .padding(LocalSpacing.current.space20)
                                                    .fillMaxHeight()
                                            ) {
                                                if (state.loading) {
                                                    repeat(20) {
                                                        SellableItemShimmer()
                                                    }
                                                } else {
                                                    state.firstColumnAllItems.forEach { sellable ->
                                                        AnimatedVisibility(
                                                            visible = state.matchesSearch(sellable),
                                                            enter = fadeIn(),
                                                            exit = fadeOut()
                                                        ) {
                                                            SellableItem(
                                                                sellableItem = SellableItemData(
                                                                    key = sellable.key,
                                                                    name = sellable.name
                                                                ),
                                                                price = state.prices[sellable.key] ?: "",
                                                                onPriceChange = { newPrice ->
                                                                    viewModel.updatePrice(sellable.key, newPrice)
                                                                },
                                                                enabled = !state.managedPrices
                                                            )
                                                        }
                                                    }

                                                    if (state.firstColumnItems.isEmpty()) {
                                                        Spacer(modifier = Modifier.weight(1f).width(253.dp))
                                                    }
                                                }
                                            }
                                        }

                                        Card(
                                            colors = CardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            ),
                                            modifier = Modifier
                                                .heightIn(min = minHeight)
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space14),
                                                modifier = Modifier
                                                    .padding(LocalSpacing.current.space20)
                                                    .fillMaxHeight()
                                            ) {
                                                if (state.loading) {
                                                    repeat(20) {
                                                        SellableItemShimmer()
                                                    }
                                                } else {
                                                    state.secondColumnAllItems.forEach { sellable ->
                                                        AnimatedVisibility(
                                                            visible = state.matchesSearch(sellable),
                                                            enter = fadeIn(),
                                                            exit = fadeOut()
                                                        ) {
                                                            SellableItem(
                                                                sellableItem = SellableItemData(
                                                                    key = sellable.key,
                                                                    name = sellable.name
                                                                ),
                                                                price = state.prices[sellable.key] ?: "",
                                                                onPriceChange = { newPrice ->
                                                                    viewModel.updatePrice(sellable.key, newPrice)
                                                                },
                                                                enabled = !state.managedPrices
                                                            )
                                                        }
                                                    }
                                                    if (state.secondColumnItems.isEmpty()) {
                                                        Spacer(modifier = Modifier.weight(1f).width(253.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (state.filteredItems.isNotEmpty()) {
                                        VerticalScrollbar(
                                            adapter = rememberScrollbarAdapter(scrollState),
                                            style = ScrollbarStyle(
                                                minimalHeight = 48.dp,
                                                thickness = 13.dp,
                                                shape = MaterialTheme.shapes.extraLarge,
                                                hoverDurationMillis = 300,
                                                unhoverColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                hoverColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            ),
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .fillMaxHeight()
                                                .padding(start = LocalSpacing.current.space8)
                                                .background(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                    shape = MaterialTheme.shapes.extraLarge
                                                )
                                                .pointerHoverIcon(PointerIcon.Hand)
                                        )
                                    }
                                }

                            }
                            }
                    }
                    Spacer(modifier = Modifier.size(LocalSpacing.current.space34))

                    val uriHandler = LocalUriHandler.current
                    val annotatedText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        ) {
                            append("*Managed Prices - are prices managed by trusted people like AYNIX from ")
                        }

                        pushStringAnnotation(
                            tag = "URL",
                            annotation = state.marketOwnersLink
                        )
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.secondary,
                                textDecoration = TextDecoration.Underline,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        ) {
                            append("Market Owners")
                        }
                        pop()

                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        ) {
                            append(". Prices are relevant only for Server that you have chosen from the selector")
                        }
                    }

                    ClickableText(
                        text = annotatedText,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                        },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    )

                    Spacer(modifier = Modifier.size(LocalSpacing.current.space16))
                }
            }
        }
    }
}