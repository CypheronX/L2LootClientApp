package com.l2loot.features.sellable

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.l2loot.design.LocalSpacing
import com.l2loot.features.sellable.components.SellableItem
import com.l2loot.features.sellable.components.SellableItemData
import com.l2loot.features.sellable.components.SellableItemShimmer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SellableScreen() {
    val viewModel: SellableViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val horizontalScrollState = rememberScrollState()


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

                val filteredItems = state.items.filter {
                    it.key.lowercase() != "adena" && it.name.lowercase() != "adena"
                }
                val midpoint = (filteredItems.size + 1) / 2
                val firstColumnItems = filteredItems.take(midpoint)
                val secondColumnItems = filteredItems.drop(midpoint)
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
                                modifier = Modifier
                                    .width(154.dp)
                                    .padding(LocalSpacing.current.space16)
                            ) {
                                Text(
                                    "Use prices by AYNIX",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.size(LocalSpacing.current.space6))
                                Switch(
                                    checked = state.pricesByAynix,
                                    onCheckedChange = {
                                        viewModel.onEvent(SellableScreenEvent.TogglePriceSource(it))
                                    },
                                    modifier = Modifier
                                        .pointerHoverIcon(PointerIcon.Hand)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
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
                                        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space14),
                                        modifier = Modifier.padding(LocalSpacing.current.space20)
                                    ) {
                                        if (state.loading) {
                                            repeat(20) {
                                                SellableItemShimmer()
                                            }
                                        } else {
                                            firstColumnItems.forEach { sellable ->
                                                SellableItem(
                                                    sellableItem = SellableItemData(
                                                        key = sellable.key,
                                                        name = sellable.name
                                                    ),
                                                    price = state.prices[sellable.key] ?: "",
                                                    onPriceChange = { newPrice ->
                                                        viewModel.updatePrice(sellable.key, newPrice)
                                                    },
                                                    enabled = !state.pricesByAynix
                                                )
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
                                    )
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.space14),
                                        modifier = Modifier.padding(LocalSpacing.current.space20)
                                    ) {
                                        if (state.loading) {
                                            repeat(20) {
                                                SellableItemShimmer()
                                            }
                                        } else {
                                            secondColumnItems.forEach { sellable ->
                                                SellableItem(
                                                    sellableItem = SellableItemData(
                                                        key = sellable.key,
                                                        name = sellable.name
                                                    ),
                                                    price = state.prices[sellable.key] ?: "",
                                                    onPriceChange = { newPrice ->
                                                        viewModel.updatePrice(sellable.key, newPrice)
                                                    },
                                                    enabled = !state.pricesByAynix
                                                )
                                            }
                                        }
                                    }
                                }
                            }

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
                    Spacer(modifier = Modifier.size(LocalSpacing.current.space34))

                    val uriHandler = LocalUriHandler.current
                    val annotatedText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        ) {
                            append("*Prices by AYNIX - AYNIX is owner of Discord Channel ")
                        }

                        pushStringAnnotation(
                            tag = "URL",
                            annotation = "https://discord.gg/D75XKfS6"
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
                            append(".  Prices are relevant only for Reborn Signature Server")
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